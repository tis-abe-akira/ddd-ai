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
import com.example.syndicatelending.loan.repository.PaymentRepository;
import com.example.syndicatelending.loan.repository.LoanRepository;
import com.example.syndicatelending.loan.repository.AmountPieRepository;
import com.example.syndicatelending.party.entity.Investor;
import com.example.syndicatelending.party.repository.InvestorRepository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.statemachine.StateMachine;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final LoanRepository loanRepository;
    private final AmountPieRepository amountPieRepository;
    private final InvestorRepository investorRepository;
    private final StateMachine<LoanState, LoanEvent> loanStateMachine;

    public PaymentService(PaymentRepository paymentRepository,
                         LoanRepository loanRepository,
                         AmountPieRepository amountPieRepository,
                         InvestorRepository investorRepository,
                         @Qualifier("loanStateMachine") StateMachine<LoanState, LoanEvent> loanStateMachine) {
        this.paymentRepository = paymentRepository;
        this.loanRepository = loanRepository;
        this.amountPieRepository = amountPieRepository;
        this.investorRepository = investorRepository;
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
}