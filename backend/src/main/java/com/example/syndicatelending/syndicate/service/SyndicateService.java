package com.example.syndicatelending.syndicate.service;

import com.example.syndicatelending.syndicate.entity.Syndicate;
import com.example.syndicatelending.syndicate.repository.SyndicateRepository;
import com.example.syndicatelending.syndicate.dto.UpdateSyndicateRequest;
import com.example.syndicatelending.syndicate.dto.SyndicateDetailResponseDTO;
import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.party.repository.InvestorRepository;
import com.example.syndicatelending.party.repository.BorrowerRepository;
import com.example.syndicatelending.party.entity.Investor;
import com.example.syndicatelending.party.entity.Borrower;
import com.example.syndicatelending.party.entity.InvestorType;
import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SyndicateService {
    private final SyndicateRepository syndicateRepository;
    private final InvestorRepository investorRepository;
    private final BorrowerRepository borrowerRepository;
    private final FacilityRepository facilityRepository;

    public SyndicateService(SyndicateRepository syndicateRepository, 
                           InvestorRepository investorRepository,
                           BorrowerRepository borrowerRepository,
                           FacilityRepository facilityRepository) {
        this.syndicateRepository = syndicateRepository;
        this.investorRepository = investorRepository;
        this.borrowerRepository = borrowerRepository;
        this.facilityRepository = facilityRepository;
    }

    public Syndicate createSyndicate(Syndicate syndicate) {
        if (syndicateRepository.existsByName(syndicate.getName())) {
            throw new IllegalArgumentException("Syndicate name already exists: " + syndicate.getName());
        }
        // --- LEAD_BANK資格チェック追加 ---
        Long leadBankId = syndicate.getLeadBankId();
        Investor leadBank = investorRepository.findById(leadBankId)
                .orElseThrow(() -> new BusinessRuleViolationException("指定されたリードバンクが存在しません: id=" + leadBankId));
        if (leadBank.getInvestorType() != InvestorType.LEAD_BANK) {
            throw new BusinessRuleViolationException("指定されたリードバンクはLEAD_BANKの資格を持っていません: id=" + leadBankId);
        }
        // ---
        return syndicateRepository.save(syndicate);
    }

    public Syndicate getSyndicateById(Long id) {
        return syndicateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syndicate not found with ID: " + id));
    }

    public Page<Syndicate> getAllSyndicates(Pageable pageable) {
        return syndicateRepository.findAll(pageable);
    }
    
    /**
     * 詳細情報付きシンジケート一覧を取得
     * 関連エンティティ（Borrower、Investor）の名前情報を含む
     */
    @Transactional(readOnly = true)
    public List<SyndicateDetailResponseDTO> getAllSyndicatesWithDetails() {
        List<Syndicate> syndicates = syndicateRepository.findAllForDetailResponse();
        return syndicates.stream()
                .map(this::convertToDetailResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 詳細情報付きシンジケート一覧を取得（ページング対応）
     * 関連エンティティ（Borrower、Investor）の名前情報を含む
     */
    @Transactional(readOnly = true)
    public Page<SyndicateDetailResponseDTO> getAllSyndicatesWithDetailsPageable(Pageable pageable) {
        Page<Syndicate> syndicatesPage = syndicateRepository.findAll(pageable);
        return syndicatesPage.map(this::convertToDetailResponseDTO);
    }
    
    /**
     * 特定シンジケートの詳細情報を取得
     * 関連エンティティ（Borrower、Investor）の名前情報を含む
     */
    @Transactional(readOnly = true)
    public SyndicateDetailResponseDTO getSyndicateWithDetails(Long id) {
        Syndicate syndicate = syndicateRepository.findByIdForDetailResponse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syndicate not found with ID: " + id));
        return convertToDetailResponseDTO(syndicate);
    }
    
    /**
     * SyndicateエンティティをSyndicateDetailResponseDTOに変換
     */
    private SyndicateDetailResponseDTO convertToDetailResponseDTO(Syndicate syndicate) {
        // Borrower名を取得
        String borrowerName = borrowerRepository.findById(syndicate.getBorrowerId())
                .map(Borrower::getName)
                .orElse("Unknown Borrower");
        
        // Lead Bank名を取得
        String leadBankName = investorRepository.findById(syndicate.getLeadBankId())
                .map(Investor::getName)
                .orElse("Unknown Lead Bank");
        
        // Member Investor名のリストを取得
        List<String> memberInvestorNames = syndicate.getMemberInvestorIds().stream()
                .map(investorId -> investorRepository.findById(investorId)
                        .map(Investor::getName)
                        .orElse("Unknown Investor"))
                .collect(Collectors.toList());
        
        return new SyndicateDetailResponseDTO(
                syndicate.getId(),
                syndicate.getName(),
                syndicate.getBorrowerId(),
                borrowerName,
                syndicate.getLeadBankId(),
                leadBankName,
                syndicate.getMemberInvestorIds(),
                memberInvestorNames,
                syndicate.getCreatedAt(),
                syndicate.getUpdatedAt(),
                syndicate.getVersion()
        );
    }

    /**
     * Syndicateの更新メソッド。
     * 
     * @param id
     * @param updatedSyndicate
     * @return
     */
    public Syndicate updateSyndicate(Long id, UpdateSyndicateRequest request) {
        Syndicate existingSyndicate = getSyndicateById(id);

        // LEAD_BANK資格チェック
        Long leadBankId = request.getLeadBankId();
        Investor leadBank = investorRepository.findById(leadBankId)
                .orElseThrow(() -> new BusinessRuleViolationException("指定されたリードバンクが存在しません: id=" + leadBankId));
        if (leadBank.getInvestorType() != InvestorType.LEAD_BANK) {
            throw new BusinessRuleViolationException("指定されたリードバンクはLEAD_BANKの資格を持っていません: id=" + leadBankId);
        }

        // 名前変更時の重複チェック
        if (!existingSyndicate.getName().equals(request.getName()) &&
                syndicateRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Syndicate name already exists: " + request.getName());
        }

        Syndicate entityToSave = new Syndicate();

        entityToSave.setId(id);
        entityToSave.setVersion(request.getVersion());

        entityToSave.setName(request.getName());
        entityToSave.setLeadBankId(request.getLeadBankId());
        entityToSave.setBorrowerId(request.getBorrowerId());
        entityToSave.setMemberInvestorIds(request.getMemberInvestorIds());
        entityToSave.setCreatedAt(existingSyndicate.getCreatedAt());

        return syndicateRepository.save(entityToSave);
    }

    public void deleteSyndicate(Long id) {
        if (!syndicateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Syndicate not found with ID: " + id);
        }
        
        // Facilityでの使用チェック
        if (facilityRepository.existsBySyndicateId(id)) {
            throw new BusinessRuleViolationException("使用中のFacilityがあるため、Syndicateを削除できません。Facilityを削除してから削除してください。");
        }
        
        syndicateRepository.deleteById(id);
    }
}
