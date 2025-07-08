package com.example.syndicatelending.party.service;

import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.party.dto.*;
import com.example.syndicatelending.party.entity.*;
import com.example.syndicatelending.party.repository.*;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.common.statemachine.party.InvestorState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Party管理サービス（統合サービス）。
 */
@Service
@Transactional
public class PartyService {

    private final CompanyRepository companyRepository;
    private final BorrowerRepository borrowerRepository;
    private final InvestorRepository investorRepository;
    // Note: FacilityRepository is used only for read-only calculation of current facility amounts
    // This is acceptable cross-context dependency for data aggregation purposes
    private final FacilityRepository facilityRepository;

    public PartyService(CompanyRepository companyRepository,
            BorrowerRepository borrowerRepository,
            InvestorRepository investorRepository,
            FacilityRepository facilityRepository) {
        this.companyRepository = companyRepository;
        this.borrowerRepository = borrowerRepository;
        this.investorRepository = investorRepository;
        this.facilityRepository = facilityRepository;
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
        // Validate company existence if companyId is provided
        if (request.getCompanyId() != null && !request.getCompanyId().isEmpty()) {
            try {
                Long companyIdLong = Long.parseLong(request.getCompanyId());
                if (!companyRepository.existsById(companyIdLong)) {
                    throw new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId());
                }
            } catch (NumberFormatException e) {
                throw new ResourceNotFoundException("Invalid company ID format: " + request.getCompanyId());
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
        Borrower borrower = borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found with ID: " + id));
        
        // 既存Facility総額を動的計算して設定
        BigDecimal totalFacilityAmount = facilityRepository.getTotalFacilityAmountByBorrowerId(id);
        borrower.setCurrentFacilityAmount(totalFacilityAmount != null ? totalFacilityAmount.doubleValue() : 0.0);
        
        return borrower;
    }

    @Transactional(readOnly = true)
    public Page<Borrower> getAllBorrowers(Pageable pageable) {
        return borrowerRepository.findAll(pageable);
    }

    // ==============================================================
    // 楽観的排他制御対応の更新メソッド
    // ==============================================================

    public Borrower updateBorrower(Long id, UpdateBorrowerRequest request) {
        Borrower existingBorrower = getBorrowerById(id);

        // 【新規追加】Facility組成後の制約フィールド変更チェック
        validateBorrowerRestrictedFields(id, existingBorrower, request);

        // ビジネスバリデーション: 信用限度額の妥当性チェック
        if (request.getCreditLimit() != null && request.getCreditRating() != null) {
            if (request.getCreditLimit().isGreaterThan(request.getCreditRating().getLimit())) {
                throw new BusinessRuleViolationException(
                        "Credit limit cannot exceed rating limit (creditLimit: " + request.getCreditLimit()
                                + ", ratingLimit: " + request.getCreditRating().getLimit() + ")");
            }
        }

        // Skip company validation - companyId is now treated as optional reference string
        // No longer validate against actual Company entities

        Borrower entityToSave = new Borrower();

        entityToSave.setId(id);
        entityToSave.setVersion(request.getVersion());

        entityToSave.setName(request.getName());
        entityToSave.setEmail(request.getEmail());
        entityToSave.setPhoneNumber(request.getPhoneNumber());
        entityToSave.setCompanyId(request.getCompanyId());
        entityToSave.setCreditLimit(request.getCreditLimit());
        entityToSave.setCreditRating(request.getCreditRating());
        entityToSave.setCreatedAt(existingBorrower.getCreatedAt());

        return borrowerRepository.save(entityToSave);
    }

    public void deleteBorrower(Long id) {
        Borrower borrower = borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found with ID: " + id));
        
        // RESTRICTED状態の場合は削除禁止（Facility参加中のため）
        if (borrower.isRestricted()) {
            throw new BusinessRuleViolationException(
                "Cannot delete Borrower because they are in RESTRICTED state due to Facility participation. " +
                "Complete or cancel the Facility first.");
        }
        
        borrowerRepository.deleteById(id);
    }

    // Investor operations
    public Investor createInvestor(CreateInvestorRequest request) {
        // Skip company validation - companyId is now treated as optional reference string
        // No longer validate against actual Company entities
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
        return investorRepository.findAll((root, query, cb) -> cb.equal(root.get("status"), InvestorState.ACTIVE), pageable);
    }

    // ==============================================================
    // 楽観的排他制御対応の更新メソッド
    // ==============================================================

    public Investor updateInvestor(Long id, UpdateInvestorRequest request) {
        Investor existingInvestor = getInvestorById(id);

        // 【新規追加】Facility組成後の制約フィールド変更チェック
        validateInvestorRestrictedFields(id, existingInvestor, request);

        // Skip company validation - companyId is now treated as optional reference string
        // No longer validate against actual Company entities

        Investor entityToSave = new Investor();

        entityToSave.setId(id);
        entityToSave.setVersion(request.getVersion());

        entityToSave.setName(request.getName());
        entityToSave.setEmail(request.getEmail());
        entityToSave.setPhoneNumber(request.getPhoneNumber());
        entityToSave.setCompanyId(request.getCompanyId());
        entityToSave.setInvestmentCapacity(request.getInvestmentCapacity());
        entityToSave.setInvestorType(request.getInvestorType());
        entityToSave.setCreatedAt(existingInvestor.getCreatedAt());

        return investorRepository.save(entityToSave);
    }

    public void deleteInvestor(Long id) {
        Investor investor = investorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investor not found with ID: " + id));
        
        // RESTRICTED状態の場合は削除禁止（Facility参加中のため）
        if (investor.isRestricted()) {
            throw new BusinessRuleViolationException(
                "Cannot delete Investor because they are in RESTRICTED state due to Facility participation. " +
                "Complete or cancel the Facility first.");
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

    // ==============================================================
    // Facility組成後の制約チェックメソッド
    // ==============================================================

    /**
     * Borrower制約フィールドの変更チェック
     * 
     * RESTRICTED状態では以下フィールドの変更を禁止：
     * - companyId
     * - creditRating  
     * - creditLimit
     * 
     * @param borrowerId Borrower ID
     * @param existing 既存のBorrowerエンティティ
     * @param request 更新リクエスト
     * @throws BusinessRuleViolationException 制約フィールド変更時
     */
    private void validateBorrowerRestrictedFields(Long borrowerId, Borrower existing, UpdateBorrowerRequest request) {
        // エンティティの状態チェック（StateMachine準拠）
        if (existing.isRestricted()) {
            // companyId変更チェック
            if (!Objects.equals(existing.getCompanyId(), request.getCompanyId())) {
                throw new BusinessRuleViolationException(
                    "Cannot modify companyId for RESTRICTED Borrower. Current value: " + existing.getCompanyId() + 
                    ", Requested value: " + request.getCompanyId() + ", Current status: " + existing.getStatus());
            }
            
            // creditRating変更チェック
            if (!Objects.equals(existing.getCreditRating(), request.getCreditRating())) {
                throw new BusinessRuleViolationException(
                    "Cannot modify creditRating for RESTRICTED Borrower. Current value: " + existing.getCreditRating() + 
                    ", Requested value: " + request.getCreditRating() + ", Current status: " + existing.getStatus());
            }
            
            // creditLimit変更チェック
            if (!Objects.equals(existing.getCreditLimit(), request.getCreditLimit())) {
                throw new BusinessRuleViolationException(
                    "Cannot modify creditLimit for RESTRICTED Borrower. Current value: " + existing.getCreditLimit() + 
                    ", Requested value: " + request.getCreditLimit() + ", Current status: " + existing.getStatus());
            }
        }
    }

    /**
     * Investor制約フィールドの変更チェック
     * 
     * RESTRICTED状態では以下フィールドの変更を禁止：
     * - companyId
     * - investmentCapacity
     * 
     * @param investorId Investor ID
     * @param existing 既存のInvestorエンティティ
     * @param request 更新リクエスト
     * @throws BusinessRuleViolationException 制約フィールド変更時
     */
    private void validateInvestorRestrictedFields(Long investorId, Investor existing, UpdateInvestorRequest request) {
        // エンティティの状態チェック（StateMachine準拠）
        if (existing.isRestricted()) {
            // companyId変更チェック
            if (!Objects.equals(existing.getCompanyId(), request.getCompanyId())) {
                throw new BusinessRuleViolationException(
                    "Cannot modify companyId for RESTRICTED Investor. Current value: " + existing.getCompanyId() + 
                    ", Requested value: " + request.getCompanyId() + ", Current status: " + existing.getStatus());
            }
            
            // investmentCapacity変更チェック  
            if (!Objects.equals(existing.getInvestmentCapacity(), request.getInvestmentCapacity())) {
                throw new BusinessRuleViolationException(
                    "Cannot modify investmentCapacity for RESTRICTED Investor. Current value: " + existing.getInvestmentCapacity() + 
                    ", Requested value: " + request.getInvestmentCapacity() + ", Current status: " + existing.getStatus());
            }
        }
    }

    // ==============================================================
    // canUpdate API メソッド
    // ==============================================================

    /**
     * Borrowerの更新可能性をチェック
     * 
     * @param id Borrower ID
     * @return 更新可能性情報
     */
    @Transactional(readOnly = true)
    public CanUpdateResponse canUpdateBorrower(Long id) {
        // Borrowerの存在チェックと取得
        Borrower borrower = borrowerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Borrower not found with ID: " + id));
        
        // エンティティ状態チェック（StateMachine準拠）
        if (borrower.isRestricted()) {
            // 制約あり：一部フィールドのみ変更可能
            CanUpdateResponse.BorrowerFieldUpdateability fields = 
                new CanUpdateResponse.BorrowerFieldUpdateability(
                    false, // companyId: 変更不可
                    false, // creditRating: 変更不可
                    false  // creditLimit: 変更不可
                );
            
            return new CanUpdateResponse(
                true, // 一部フィールドは更新可能
                "Cannot modify companyId, creditRating, creditLimit due to RESTRICTED status. Current status: " + borrower.getStatus(), 
                fields
            );
        } else {
            // 制約なし：全フィールド変更可能
            CanUpdateResponse.BorrowerFieldUpdateability fields = 
                new CanUpdateResponse.BorrowerFieldUpdateability(
                    true, // companyId: 変更可能
                    true, // creditRating: 変更可能
                    true  // creditLimit: 変更可能
                );
            
            return new CanUpdateResponse(true, null, fields);
        }
    }

    /**
     * Investorの更新可能性をチェック
     * 
     * @param id Investor ID
     * @return 更新可能性情報
     */
    @Transactional(readOnly = true)
    public CanUpdateResponse canUpdateInvestor(Long id) {
        // Investorの存在チェックと取得
        Investor investor = investorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Investor not found with ID: " + id));
        
        // エンティティ状態チェック（StateMachine準拠）
        if (investor.isRestricted()) {
            // 制約あり：一部フィールドのみ変更可能
            CanUpdateResponse.InvestorFieldUpdateability fields = 
                new CanUpdateResponse.InvestorFieldUpdateability(
                    false, // companyId: 変更不可
                    false  // investmentCapacity: 変更不可
                );
            
            return new CanUpdateResponse(
                true, // 一部フィールドは更新可能
                "Cannot modify companyId, investmentCapacity due to RESTRICTED status. Current status: " + investor.getStatus(), 
                fields
            );
        } else {
            // 制約なし：全フィールド変更可能
            CanUpdateResponse.InvestorFieldUpdateability fields = 
                new CanUpdateResponse.InvestorFieldUpdateability(
                    true, // companyId: 変更可能
                    true  // investmentCapacity: 変更可能
                );
            
            return new CanUpdateResponse(true, null, fields);
        }
    }
}
