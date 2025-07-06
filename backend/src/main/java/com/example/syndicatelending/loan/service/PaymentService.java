package com.example.syndicatelending.loan.service;

import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.statemachine.loan.LoanState;
import com.example.syndicatelending.common.statemachine.loan.LoanEvent;
import com.example.syndicatelending.loan.dto.CreatePaymentRequest;
import com.example.syndicatelending.loan.entity.Payment;
import com.example.syndicatelending.loan.entity.PaymentDistribution;
import com.example.syndicatelending.loan.entity.Loan;
import com.example.syndicatelending.loan.entity.AmountPie;
import com.example.syndicatelending.loan.entity.PaymentDetail;
import com.example.syndicatelending.loan.entity.PaymentStatus;
import com.example.syndicatelending.loan.repository.PaymentRepository;
import com.example.syndicatelending.loan.repository.LoanRepository;
import com.example.syndicatelending.loan.repository.AmountPieRepository;
import com.example.syndicatelending.loan.repository.PaymentDetailRepository;
import com.example.syndicatelending.party.entity.Investor;
import com.example.syndicatelending.party.repository.InvestorRepository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.statemachine.StateMachine;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final LoanRepository loanRepository;
    private final AmountPieRepository amountPieRepository;
    private final InvestorRepository investorRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final StateMachine<LoanState, LoanEvent> loanStateMachine;

    public PaymentService(PaymentRepository paymentRepository,
                         LoanRepository loanRepository,
                         AmountPieRepository amountPieRepository,
                         InvestorRepository investorRepository,
                         PaymentDetailRepository paymentDetailRepository,
                         @Qualifier("loanStateMachine") StateMachine<LoanState, LoanEvent> loanStateMachine) {
        this.paymentRepository = paymentRepository;
        this.loanRepository = loanRepository;
        this.amountPieRepository = amountPieRepository;
        this.investorRepository = investorRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.loanStateMachine = loanStateMachine;
    }

    @Transactional
    public Payment processPayment(CreatePaymentRequest request) {
        // 1. バリデーション
        validatePaymentRequest(request);

        // 2. Loanエンティティの取得
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + request.getLoanId()));

        // 3. Paymentエンティティの作成
        Money principalAmount = Money.of(request.getPrincipalAmount());
        Money interestAmount = Money.of(request.getInterestAmount());
        Money totalAmount = principalAmount.add(interestAmount);

        Payment payment = new Payment(
                request.getLoanId(),
                request.getPaymentDate(),
                totalAmount,
                principalAmount,
                interestAmount,
                request.getCurrency()
        );
        
        // Transaction基底クラスのフィールドを設定
        payment.setFacilityId(loan.getFacilityId());
        payment.setBorrowerId(loan.getBorrowerId());

        // 4. Payment保存
        Payment savedPayment = paymentRepository.save(payment);

        // 5. PaymentDistributionの生成と保存（ドローダウン時のAmountPieベース）
        List<PaymentDistribution> paymentDistributions = createPaymentDistributions(
                savedPayment, loan, principalAmount, interestAmount, request.getCurrency());
        savedPayment.setPaymentDistributions(paymentDistributions);

        // 6. Investor投資額の減少（元本部分のみ）
        updateInvestorAmountsForPayment(paymentDistributions);

        // 7. Loan状態管理（初回返済時のDRAFT→ACTIVE遷移）
        updateLoanStateForFirstPayment(loan);

        return savedPayment;
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByLoanId(Long loanId) {
        return paymentRepository.findByLoanIdOrderByPaymentDateDesc(loanId);
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    private void validatePaymentRequest(CreatePaymentRequest request) {
        if (!loanRepository.existsById(request.getLoanId())) {
            throw new ResourceNotFoundException("Loan not found with id: " + request.getLoanId());
        }

        Money principalAmount = Money.of(request.getPrincipalAmount());
        Money interestAmount = Money.of(request.getInterestAmount());

        if (!principalAmount.isPositiveOrZero()) {
            throw new BusinessRuleViolationException("Principal amount must be positive or zero");
        }

        if (!interestAmount.isPositiveOrZero()) {
            throw new BusinessRuleViolationException("Interest amount must be positive or zero");
        }

        if (principalAmount.isZero() && interestAmount.isZero()) {
            throw new BusinessRuleViolationException("Payment amount cannot be zero");
        }
    }

    private List<PaymentDistribution> createPaymentDistributions(Payment payment, Loan loan, Money principalAmount, 
                                                                Money interestAmount, String currency) {
        // ドローダウン時のAmountPieから投資家の持分比率を取得
        List<AmountPie> amountPies = amountPieRepository.findByDrawdown_LoanId(loan.getId());
        if (amountPies.isEmpty()) {
            throw new BusinessRuleViolationException("No amount pies found for loan: " + loan.getId());
        }

        // 総投資額を計算
        BigDecimal totalInvestmentAmount = amountPies.stream()
                .map(AmountPie::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PaymentDistribution> distributions = new ArrayList<>();
        BigDecimal totalDistributedPrincipal = BigDecimal.ZERO;
        BigDecimal totalDistributedInterest = BigDecimal.ZERO;

        for (int i = 0; i < amountPies.size(); i++) {
            AmountPie amountPie = amountPies.get(i);
            
            // 投資家の持分比率を計算
            BigDecimal shareRatio = amountPie.getAmount().divide(totalInvestmentAmount, 10, java.math.RoundingMode.HALF_UP);

            Money investorPrincipal;
            Money investorInterest;

            if (i == amountPies.size() - 1) {
                // 最後の投資家には端数調整
                investorPrincipal = principalAmount.subtract(Money.of(totalDistributedPrincipal));
                investorInterest = interestAmount.subtract(Money.of(totalDistributedInterest));
            } else {
                investorPrincipal = principalAmount.multiply(shareRatio);
                investorInterest = interestAmount.multiply(shareRatio);
                totalDistributedPrincipal = totalDistributedPrincipal.add(investorPrincipal.getAmount());
                totalDistributedInterest = totalDistributedInterest.add(investorInterest.getAmount());
            }

            PaymentDistribution distribution = new PaymentDistribution(
                    amountPie.getInvestorId(),
                    investorPrincipal,
                    investorInterest,
                    currency
            );
            distribution.setPayment(payment);
            distributions.add(distribution);
        }

        return distributions;
    }

    private void updateInvestorAmountsForPayment(List<PaymentDistribution> paymentDistributions) {
        for (PaymentDistribution distribution : paymentDistributions) {
            Investor investor = investorRepository.findById(distribution.getInvestorId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Investor not found with id: " + distribution.getInvestorId()));

            // 元本部分のみ投資額から減算（利息は投資額に影響しない）
            investor.decreaseInvestmentAmount(distribution.getPrincipalAmount());
            investorRepository.save(investor);
        }
    }

    /**
     * 初回返済時のLoan状態管理
     * DRAFT状態のLoanに対してFIRST_PAYMENTイベントを送信し、ACTIVE状態に遷移させる
     * 
     * @param loan 対象のLoanエンティティ
     */
    private void updateLoanStateForFirstPayment(Loan loan) {
        // DRAFT状態の場合のみ初回返済として扱う
        if (loan.getStatus() == LoanState.DRAFT) {
            // 既存の支払い履歴を確認（今回の支払いを除く）
            List<Payment> existingPayments = paymentRepository.findByLoanIdOrderByPaymentDateDesc(loan.getId());
            
            // 今回が初回返済（既存の支払いが1件以下）の場合
            if (existingPayments.size() <= 1) {
                if (executeLoanStateTransition(loan, LoanEvent.FIRST_PAYMENT)) {
                    // エンティティ状態を更新
                    loan.setStatus(LoanState.ACTIVE);
                    loanRepository.save(loan);
                }
            }
        }
    }

    /**
     * Loan StateMachine遷移実行
     * 
     * @param loan Loanエンティティ
     * @param event 発火イベント
     * @return 遷移成功時 true
     */
    private boolean executeLoanStateTransition(Loan loan, LoanEvent event) {
        try {
            // StateMachineを現在状態に設定
            loanStateMachine.getStateMachineAccessor().doWithAllRegions(access -> {
                access.resetStateMachine(null);
            });

            // 現在状態を設定
            loanStateMachine.getExtendedState().getVariables().put("loanId", loan.getId());
            
            // イベント送信
            return loanStateMachine.sendEvent(event);
        } catch (Exception e) {
            // 状態遷移失敗時はログに記録し、処理を継続
            // （支払い処理自体は成功させる）
            System.err.println("Loan state transition failed for ID: " + loan.getId() + ", error: " + e.getMessage());
            return false;
        }
    }

    /**
     * PaymentDetailに対する期日通り満額支払いを処理する
     * 
     * @param paymentDetailId PaymentDetailのID
     * @return 作成されたPayment
     * @throws ResourceNotFoundException PaymentDetailが存在しない場合
     * @throws BusinessRuleViolationException 支払い条件が満たされない場合
     */
    @Transactional
    public Payment processScheduledPayment(Long paymentDetailId) {
        // 1. PaymentDetailの取得と検証
        PaymentDetail paymentDetail = paymentDetailRepository.findById(paymentDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentDetail not found with id: " + paymentDetailId));

        // 2. 支払い可能状態の確認
        if (!paymentDetail.isPayable()) {
            throw new BusinessRuleViolationException(
                    "PaymentDetail is not payable. Current status: " + paymentDetail.getPaymentStatus());
        }

        // 3. 関連するLoanの取得
        Loan loan = paymentDetail.getLoan();
        if (loan == null) {
            throw new BusinessRuleViolationException("PaymentDetail is not associated with a loan");
        }

        // 4. Paymentエンティティの作成
        Payment payment = new Payment();
        payment.setLoanId(loan.getId());
        
        LocalDate paymentDate = LocalDate.now(); // 今日の日付で支払い
        payment.setPaymentDate(paymentDate);
        payment.setTransactionDate(paymentDate); // Transaction基底クラスのフィールドも設定
        
        payment.setPrincipalAmount(paymentDetail.getPrincipalPayment());
        payment.setInterestAmount(paymentDetail.getInterestPayment());
        payment.setTotalAmount(paymentDetail.getTotalPayment());
        payment.setCurrency(loan.getCurrency());
        
        // Transaction基底クラスのフィールド設定
        payment.setAmount(paymentDetail.getTotalPayment());
        payment.setTransactionType(com.example.syndicatelending.transaction.entity.TransactionType.PAYMENT);
        payment.setFacilityId(loan.getFacilityId());
        payment.setBorrowerId(loan.getBorrowerId());

        // 5. PaymentDistributionの生成（投資家別配分）
        List<PaymentDistribution> distributions = generatePaymentDistributions(payment, loan, paymentDetail);
        payment.setPaymentDistributions(distributions);

        // 6. Paymentの保存
        Payment savedPayment = paymentRepository.save(payment);

        // 7. PaymentDetailを支払い済みにマーク
        paymentDetail.markAsPaid(payment.getPaymentDate(), savedPayment.getId());
        paymentDetailRepository.save(paymentDetail);

        // 8. Loanの残高更新
        updateLoanBalanceAndStatus(loan, paymentDetail);

        // 9. 投資家の投資額更新（元本返済分のみ）
        updateInvestorInvestmentAmounts(distributions, paymentDetail.getPrincipalPayment());

        return savedPayment;
    }

    /**
     * PaymentDistributionを生成する（投資家別配分）
     */
    private List<PaymentDistribution> generatePaymentDistributions(Payment payment, Loan loan, PaymentDetail paymentDetail) {
        List<PaymentDistribution> distributions = new ArrayList<>();
        
        // ローンに関連するAmountPie（投資家別の出資比率）を取得
        List<AmountPie> amountPies = amountPieRepository.findByDrawdown_LoanId(loan.getId());
        
        if (amountPies.isEmpty()) {
            throw new BusinessRuleViolationException("No AmountPies found for loan: " + loan.getId());
        }

        // 総出資額を計算
        BigDecimal totalAmount = amountPies.stream()
                .map(AmountPie::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 各投資家への配分を計算
        BigDecimal principalTotal = BigDecimal.ZERO;
        BigDecimal interestTotal = BigDecimal.ZERO;

        for (AmountPie amountPie : amountPies) {
            // 投資家の出資比率を計算
            BigDecimal ratio = amountPie.getAmount().divide(totalAmount, 4, java.math.RoundingMode.HALF_UP);
            
            // 元本と利息の配分額を計算
            BigDecimal principalShare = paymentDetail.getPrincipalPayment().getAmount()
                    .multiply(ratio).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal interestShare = paymentDetail.getInterestPayment().getAmount()
                    .multiply(ratio).setScale(2, java.math.RoundingMode.HALF_UP);

            PaymentDistribution distribution = new PaymentDistribution();
            distribution.setPayment(payment);
            distribution.setInvestorId(amountPie.getInvestorId());
            distribution.setPrincipalAmount(Money.of(principalShare));
            distribution.setInterestAmount(Money.of(interestShare));
            distribution.setCurrency(loan.getCurrency()); // 通貨設定を追加
            // totalAmountは計算プロパティなので設定不要

            distributions.add(distribution);
            
            principalTotal = principalTotal.add(principalShare);
            interestTotal = interestTotal.add(interestShare);
        }

        // 端数調整（最後の投資家で調整）
        if (!distributions.isEmpty()) {
            PaymentDistribution lastDistribution = distributions.get(distributions.size() - 1);
            
            BigDecimal principalDiff = paymentDetail.getPrincipalPayment().getAmount().subtract(principalTotal);
            BigDecimal interestDiff = paymentDetail.getInterestPayment().getAmount().subtract(interestTotal);
            
            Money adjustedPrincipal = lastDistribution.getPrincipalAmount().add(Money.of(principalDiff));
            Money adjustedInterest = lastDistribution.getInterestAmount().add(Money.of(interestDiff));
            
            lastDistribution.setPrincipalAmount(adjustedPrincipal);
            lastDistribution.setInterestAmount(adjustedInterest);
            // totalAmountは計算プロパティなので設定不要
        }

        return distributions;
    }

    /**
     * Loanの残高と状態を更新する
     */
    private void updateLoanBalanceAndStatus(Loan loan, PaymentDetail paymentDetail) {
        // 未払い残高を更新
        Money newBalance = loan.getOutstandingBalance().subtract(paymentDetail.getPrincipalPayment());
        loan.setOutstandingBalance(newBalance);

        // ローンの状態遷移
        if (loan.getStatus() == LoanState.DRAFT) {
            // 初回支払いでACTIVE状態に遷移
            executeLoanStateTransition(loan, LoanEvent.FIRST_PAYMENT);
            loan.setStatus(LoanState.ACTIVE);
        } else if (newBalance.isZero()) {
            // 完済時にCOMPLETED状態に遷移
            executeLoanStateTransition(loan, LoanEvent.FINAL_PAYMENT);
            loan.setStatus(LoanState.COMPLETED);
        }

        loanRepository.save(loan);
    }

    /**
     * 投資家の投資額を更新する（元本返済分のみ）
     */
    private void updateInvestorInvestmentAmounts(List<PaymentDistribution> distributions, Money principalPayment) {
        for (PaymentDistribution distribution : distributions) {
            Investor investor = investorRepository.findById(distribution.getInvestorId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Investor not found with id: " + distribution.getInvestorId()));
            
            // 元本返済分だけ投資額を減額（利息は投資額に影響しない）
            Money principalReduction = distribution.getPrincipalAmount();
            investor.decreaseInvestmentAmount(principalReduction);
            investorRepository.save(investor);
        }
    }
}