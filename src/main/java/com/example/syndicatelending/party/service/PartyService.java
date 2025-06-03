package com.example.syndicatelending.party.service;

import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.party.dto.*;
import com.example.syndicatelending.party.entity.*;
import com.example.syndicatelending.party.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Party管理サービス（統合サービス）。
 */
@Service
@Transactional
public class PartyService {

    private final CompanyRepository companyRepository;
    private final BorrowerRepository borrowerRepository;
    private final InvestorRepository investorRepository;

    public PartyService(CompanyRepository companyRepository,
            BorrowerRepository borrowerRepository,
            InvestorRepository investorRepository) {
        this.companyRepository = companyRepository;
        this.borrowerRepository = borrowerRepository;
        this.investorRepository = investorRepository;
    }

    // Company operations
    public Company createCompany(CreateCompanyRequest request) {
        Company company = new Company(
                request.getCompanyName(),
                request.getRegistrationNumber(),
                request.getIndustry(),
                request.getAddress(),
                request.getCountry());
        return companyRepository.save(company);
    }

    @Transactional(readOnly = true)
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Company> getAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    /**
     * このメソッドは非推奨にする。（理由は更新時の楽観的排他制御を行っていないため）
     * Companyの更新メソッド。
     * 
     * @deprecated
     *             このメソッドは楽観的排他制御を行っていないため、使用しないでください。
     *             代わりに、updateCompany(Long id, UpdateCompanyRequest request)を使用してください。
     * @param id
     * @param request
     * @return
     */
    public Company updateCompany(Long id, CreateCompanyRequest request) {
        Company company = getCompanyById(id); // ResourceNotFoundExceptionをスロー
        company.setCompanyName(request.getCompanyName());
        company.setRegistrationNumber(request.getRegistrationNumber());
        company.setIndustry(request.getIndustry());
        company.setAddress(request.getAddress());
        company.setCountry(request.getCountry());
        return companyRepository.save(company);
    }

    // ==============================================================
    // 楽観的排他制御対応の更新メソッド
    // ==============================================================

    public Company updateCompany(Long id, UpdateCompanyRequest request) {
        Company existingCompany = getCompanyById(id);

        Company entityToSave = new Company();

        entityToSave.setId(id);
        entityToSave.setVersion(request.getVersion());

        entityToSave.setCompanyName(request.getCompanyName());
        entityToSave.setRegistrationNumber(request.getRegistrationNumber());
        entityToSave.setIndustry(request.getIndustry());
        entityToSave.setAddress(request.getAddress());
        entityToSave.setCountry(request.getCountry());
        entityToSave.setCreatedAt(existingCompany.getCreatedAt());

        return companyRepository.save(entityToSave);
    }

    public void deleteCompany(Long id) {
        if (!companyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Company not found with ID: " + id);
        }
        companyRepository.deleteById(id);
    }

    // Borrower operations
    public Borrower createBorrower(CreateBorrowerRequest request) {
        if (request.getCompanyId() != null && !request.getCompanyId().trim().isEmpty()) {
            Long companyId;
            try {
                companyId = Long.parseLong(request.getCompanyId());
            } catch (NumberFormatException e) {
                throw new ResourceNotFoundException("Invalid company ID: " + request.getCompanyId());
            }
            if (!companyRepository.existsById(companyId)) {
                throw new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId());
            }
        }
        // CreditRatingのバリデーション振る舞いを利用
        if (!request.isCreditLimitOverride()) {
            if (request.getCreditRating() == null || request.getCreditLimit() == null ||
                    !request.getCreditRating().isLimitSatisfied(request.getCreditLimit())) {
                throw new com.example.syndicatelending.common.application.exception.BusinessRuleViolationException(
                        "creditLimit exceeds allowed maximum for creditRating " + request.getCreditRating() +
                                " (max: "
                                + (request.getCreditRating() != null ? request.getCreditRating().getLimit() : null)
                                + ")");
            }
        }
        Borrower borrower = new Borrower(
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getCompanyId(),
                request.getCreditLimit(),
                request.getCreditRating());
        return borrowerRepository.save(borrower);
    }

    @Transactional(readOnly = true)
    public Borrower getBorrowerById(Long id) {
        return borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Borrower> getAllBorrowers(Pageable pageable) {
        return borrowerRepository.findAll(pageable);
    }

    public Borrower updateBorrower(Long id, CreateBorrowerRequest request) {
        Borrower borrower = getBorrowerById(id); // ResourceNotFoundExceptionをスロー

        // Company存在確認
        if (request.getCompanyId() != null && !request.getCompanyId().trim().isEmpty()) {
            Long companyId;
            try {
                companyId = Long.parseLong(request.getCompanyId());
            } catch (NumberFormatException e) {
                throw new ResourceNotFoundException("Invalid company ID: " + request.getCompanyId());
            }
            if (!companyRepository.existsById(companyId)) {
                throw new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId());
            }
        }

        // CreditRatingバリデーション
        if (!request.isCreditLimitOverride()) {
            if (request.getCreditRating() == null || request.getCreditLimit() == null ||
                    !request.getCreditRating().isLimitSatisfied(request.getCreditLimit())) {
                throw new com.example.syndicatelending.common.application.exception.BusinessRuleViolationException(
                        "creditLimit exceeds allowed maximum for creditRating " + request.getCreditRating() +
                                " (max: "
                                + (request.getCreditRating() != null ? request.getCreditRating().getLimit() : null)
                                + ")");
            }
        }

        borrower.setName(request.getName());
        borrower.setEmail(request.getEmail());
        borrower.setPhoneNumber(request.getPhoneNumber());
        borrower.setCompanyId(request.getCompanyId());
        borrower.setCreditLimit(request.getCreditLimit());
        borrower.setCreditRating(request.getCreditRating());
        return borrowerRepository.save(borrower);
    }

    // ==============================================================
    // 楽観的排他制御対応の更新メソッド
    // ==============================================================

    public Borrower updateBorrower(Long id, UpdateBorrowerRequest request) {
        Borrower existingBorrower = getBorrowerById(id);

        // Spring Data JPAが自動的に楽観的ロックをチェック
        existingBorrower.setVersion(request.getVersion());

        // ビジネスバリデーション: 信用限度額の妥当性チェック
        if (request.getCreditLimit() != null && request.getCreditRating() != null) {
            if (request.getCreditLimit().isGreaterThan(request.getCreditRating().getLimit())) {
                throw new BusinessRuleViolationException(
                        "Credit limit cannot exceed rating limit (creditLimit: " + request.getCreditLimit()
                                + ", ratingLimit: " + request.getCreditRating().getLimit() + ")");
            }
        }

        existingBorrower.setName(request.getName());
        existingBorrower.setEmail(request.getEmail());
        existingBorrower.setPhoneNumber(request.getPhoneNumber());
        existingBorrower.setCompanyId(request.getCompanyId());
        existingBorrower.setCreditLimit(request.getCreditLimit());
        existingBorrower.setCreditRating(request.getCreditRating());

        return borrowerRepository.save(existingBorrower);
    }

    public void deleteBorrower(Long id) {
        if (!borrowerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Borrower not found with ID: " + id);
        }
        borrowerRepository.deleteById(id);
    }

    // Investor operations
    public Investor createInvestor(CreateInvestorRequest request) {
        if (request.getCompanyId() != null && !request.getCompanyId().trim().isEmpty()) {
            Long companyId;
            try {
                companyId = Long.parseLong(request.getCompanyId());
            } catch (NumberFormatException e) {
                throw new ResourceNotFoundException("Invalid company ID: " + request.getCompanyId());
            }
            if (!companyRepository.existsById(companyId)) {
                throw new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId());
            }
        }
        Investor investor = new Investor(
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getCompanyId(),
                request.getInvestmentCapacity(),
                request.getInvestorType());
        return investorRepository.save(investor);
    }

    @Transactional(readOnly = true)
    public Investor getInvestorById(Long id) {
        return investorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investor not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Investor> getAllInvestors(Pageable pageable) {
        return investorRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Investor> getActiveInvestors(Pageable pageable) {
        return investorRepository.findAll((root, query, cb) -> cb.isTrue(root.get("isActive")), pageable);
    }

    public Investor updateInvestor(Long id, CreateInvestorRequest request) {
        Investor investor = getInvestorById(id); // ResourceNotFoundExceptionをスロー

        // Company存在確認
        if (request.getCompanyId() != null && !request.getCompanyId().trim().isEmpty()) {
            Long companyId;
            try {
                companyId = Long.parseLong(request.getCompanyId());
            } catch (NumberFormatException e) {
                throw new ResourceNotFoundException("Invalid company ID: " + request.getCompanyId());
            }
            if (!companyRepository.existsById(companyId)) {
                throw new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId());
            }
        }

        investor.setName(request.getName());
        investor.setEmail(request.getEmail());
        investor.setPhoneNumber(request.getPhoneNumber());
        investor.setCompanyId(request.getCompanyId());
        investor.setInvestmentCapacity(request.getInvestmentCapacity());
        investor.setInvestorType(request.getInvestorType());
        return investorRepository.save(investor);
    }

    // ==============================================================
    // 楽観的排他制御対応の更新メソッド
    // ==============================================================

    public Investor updateInvestor(Long id, UpdateInvestorRequest request) {
        Investor existingInvestor = getInvestorById(id);

        // Spring Data JPAが自動的に楽観的ロックをチェック
        existingInvestor.setVersion(request.getVersion());

        // 企業IDが指定されている場合の存在チェック
        if (request.getCompanyId() != null && !request.getCompanyId().trim().isEmpty()) {
            Long companyId;
            try {
                companyId = Long.parseLong(request.getCompanyId());
            } catch (NumberFormatException e) {
                throw new ResourceNotFoundException("Invalid company ID: " + request.getCompanyId());
            }
            if (!companyRepository.existsById(companyId)) {
                throw new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId());
            }
        }

        existingInvestor.setName(request.getName());
        existingInvestor.setEmail(request.getEmail());
        existingInvestor.setPhoneNumber(request.getPhoneNumber());
        existingInvestor.setCompanyId(request.getCompanyId());
        existingInvestor.setInvestmentCapacity(request.getInvestmentCapacity());
        existingInvestor.setInvestorType(request.getInvestorType());

        return investorRepository.save(existingInvestor);
    }

    public void deleteInvestor(Long id) {
        if (!investorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Investor not found with ID: " + id);
        }
        investorRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Borrower> searchBorrowers(String name, CreditRating creditRating, Pageable pageable) {
        if (name != null && !name.isBlank() && creditRating != null) {
            Specification<Borrower> spec = (root, query, cb) -> cb.and(
                    cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"),
                    cb.equal(root.get("creditRating"), creditRating));
            return borrowerRepository.findAll(spec, pageable);
        } else if (name != null && !name.isBlank()) {
            return borrowerRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (creditRating != null) {
            return borrowerRepository.findByCreditRating(creditRating, pageable);
        } else {
            return borrowerRepository.findAll(pageable);
        }
    }

    @Transactional(readOnly = true)
    public Page<Company> searchCompanies(String name, Industry industry, Pageable pageable) {
        if (name != null && !name.isBlank() && industry != null) {
            Specification<Company> spec = (root, query, cb) -> cb.and(
                    cb.like(cb.lower(root.get("companyName")), "%" + name.toLowerCase() + "%"),
                    cb.equal(root.get("industry"), industry));
            return companyRepository.findAll(spec, pageable);
        } else if (name != null && !name.isBlank()) {
            return companyRepository.findByCompanyNameContainingIgnoreCase(name, pageable);
        } else if (industry != null) {
            return companyRepository.findByIndustry(industry, pageable);
        } else {
            return companyRepository.findAll(pageable);
        }
    }

    @Transactional(readOnly = true)
    public Page<Investor> searchInvestors(String name, InvestorType investorType, Pageable pageable) {
        if (name != null && !name.isBlank() && investorType != null) {
            Specification<Investor> spec = (root, query, cb) -> cb.and(
                    cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"),
                    cb.equal(root.get("investorType"), investorType));
            return investorRepository.findAll(spec, pageable);
        } else if (name != null && !name.isBlank()) {
            return investorRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (investorType != null) {
            return investorRepository.findByInvestorType(investorType, pageable);
        } else {
            return investorRepository.findAll(pageable);
        }
    }
}
