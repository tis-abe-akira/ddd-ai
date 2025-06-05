package com.example.syndicatelending.loan.service;

import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.loan.dto.CreateDrawdownRequest;
import com.example.syndicatelending.loan.entity.Drawdown;
import com.example.syndicatelending.loan.entity.Loan;
import com.example.syndicatelending.loan.repository.DrawdownRepository;
import com.example.syndicatelending.loan.repository.LoanRepository;
import com.example.syndicatelending.party.repository.BorrowerRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DrawdownService {
    private final DrawdownRepository drawdownRepository;
    private final LoanRepository loanRepository;
    private final FacilityRepository facilityRepository;
    private final BorrowerRepository borrowerRepository;

    public DrawdownService(DrawdownRepository drawdownRepository,
            LoanRepository loanRepository,
            FacilityRepository facilityRepository,
            BorrowerRepository borrowerRepository) {
        this.drawdownRepository = drawdownRepository;
        this.loanRepository = loanRepository;
        this.facilityRepository = facilityRepository;
        this.borrowerRepository = borrowerRepository;
    }

    @Transactional
    public Drawdown createDrawdown(CreateDrawdownRequest request) {
        // 1. バリデーション
        validateDrawdownRequest(request);

        // 2. Loanエンティティの作成
        Loan loan = createLoan(request);
        Loan savedLoan = loanRepository.save(loan);

        // 3. Drawdownエンティティの作成
        Drawdown drawdown = new Drawdown();
        drawdown.setFacilityId(request.getFacilityId());
        drawdown.setBorrowerId(request.getBorrowerId());
        drawdown.setTransactionDate(request.getDrawdownDate());
        drawdown.setAmount(Money.of(request.getAmount()));
        drawdown.setLoanId(savedLoan.getId());
        drawdown.setCurrency(request.getCurrency());
        drawdown.setPurpose(request.getPurpose());

        return drawdownRepository.save(drawdown);
    }

    @Transactional(readOnly = true)
    public List<Drawdown> getAllDrawdowns() {
        return drawdownRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Drawdown> getAllDrawdowns(Pageable pageable) {
        return drawdownRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Drawdown getDrawdownById(Long id) {
        return drawdownRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drawdown not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Drawdown> getDrawdownsByFacilityId(Long facilityId) {
        return drawdownRepository.findByFacilityId(facilityId);
    }

    private void validateDrawdownRequest(CreateDrawdownRequest request) {
        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Facility not found with id: " + request.getFacilityId()));

        if (!borrowerRepository.existsById(request.getBorrowerId())) {
            throw new ResourceNotFoundException("Borrower not found with id: " + request.getBorrowerId());
        }

        // 金額の妥当性チェック
        Money amount = Money.of(request.getAmount());
        if (amount.isZero() || !amount.isPositiveOrZero()) {
            throw new BusinessRuleViolationException("Drawdown amount must be positive");
        }

        // Facilityの利用可能残高チェック（簡易版）
        Money facilityCommitment = facility.getCommitment();
        if (amount.isGreaterThan(facilityCommitment)) {
            throw new BusinessRuleViolationException("Drawdown amount exceeds facility commitment");
        }

        // 金利の妥当性チェック
        Percentage interestRate = Percentage.of(request.getAnnualInterestRate());
        if (interestRate.getValue().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("Interest rate cannot be negative");
        }

        // 返済期間の妥当性チェック
        if (request.getRepaymentPeriodMonths() <= 0) {
            throw new BusinessRuleViolationException("Repayment period must be positive");
        }
    }

    private Loan createLoan(CreateDrawdownRequest request) {
        return new Loan(
                request.getFacilityId(),
                request.getBorrowerId(),
                Money.of(request.getAmount()),
                Percentage.of(request.getAnnualInterestRate()),
                request.getDrawdownDate(),
                request.getRepaymentPeriodMonths(),
                request.getRepaymentCycle(),
                request.getRepaymentMethod(),
                request.getCurrency());
    }
}
