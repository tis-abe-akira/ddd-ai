package com.example.syndicatelending.loan.service;

import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.common.domain.model.Money;
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

    public PaymentService(PaymentRepository paymentRepository,
                         LoanRepository loanRepository,
                         AmountPieRepository amountPieRepository,
                         InvestorRepository investorRepository) {
        this.paymentRepository = paymentRepository;
        this.loanRepository = loanRepository;
        this.amountPieRepository = amountPieRepository;
        this.investorRepository = investorRepository;
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

        // 4. Payment保存
        Payment savedPayment = paymentRepository.save(payment);

        // 5. PaymentDistributionの生成と保存（ドローダウン時のAmountPieベース）
        List<PaymentDistribution> paymentDistributions = createPaymentDistributions(
                savedPayment, loan, principalAmount, interestAmount, request.getCurrency());
        savedPayment.setPaymentDistributions(paymentDistributions);

        // 6. Investor投資額の減少（元本部分のみ）
        updateInvestorAmountsForPayment(paymentDistributions);

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
}