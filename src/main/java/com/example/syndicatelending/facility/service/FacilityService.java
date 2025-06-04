package com.example.syndicatelending.facility.service;

import com.example.syndicatelending.facility.dto.CreateFacilityRequest;
import com.example.syndicatelending.facility.dto.UpdateFacilityRequest;
import com.example.syndicatelending.facility.domain.FacilityValidator;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.entity.SharePie;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.facility.repository.SharePieRepository;
import com.example.syndicatelending.facility.repository.FacilityInvestmentRepository;
import com.example.syndicatelending.facility.entity.FacilityInvestment;
import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.syndicate.repository.SyndicateRepository;
import com.example.syndicatelending.syndicate.entity.Syndicate;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class FacilityService {
    private final FacilityRepository facilityRepository;
    private final FacilityValidator facilityValidator;
    private final SharePieRepository sharePieRepository;
    private final FacilityInvestmentRepository facilityInvestmentRepository;
    private final SyndicateRepository syndicateRepository;

    public FacilityService(FacilityRepository facilityRepository, FacilityValidator facilityValidator,
            SharePieRepository sharePieRepository, FacilityInvestmentRepository facilityInvestmentRepository,
            SyndicateRepository syndicateRepository) {
        this.facilityRepository = facilityRepository;
        this.facilityValidator = facilityValidator;
        this.sharePieRepository = sharePieRepository;
        this.facilityInvestmentRepository = facilityInvestmentRepository;
        this.syndicateRepository = syndicateRepository;
    }

    @Transactional
    public Facility createFacility(CreateFacilityRequest request) {
        // 1. Facility作成
        Facility facility = new Facility(
                request.getSyndicateId(),
                request.getCommitment(),
                request.getCurrency(),
                request.getStartDate(),
                request.getEndDate(),
                request.getInterestTerms());

        // 2. SharePie作成・設定
        List<SharePie> sharePies = new ArrayList<>();
        for (CreateFacilityRequest.SharePieRequest pie : request.getSharePies()) {
            SharePie entity = new SharePie();
            entity.setInvestorId(pie.getInvestorId());
            entity.setShare(pie.getShare());
            entity.setFacility(facility);
            sharePies.add(entity);
        }

        facility.setSharePies(sharePies);

        // 3. バリデーション実行
        facilityValidator.validateCreateFacilityRequest(request);

        // 4. Facility保存（cascadeによりSharePieも一緒に保存される）
        Facility savedFacility = facilityRepository.save(facility);

        // 5. FacilityInvestment生成・保存
        List<FacilityInvestment> investments = new ArrayList<>();
        Money commitment = savedFacility.getCommitment();
        
        // Facility → Syndicate → BorrowerIdを取得
        Syndicate syndicate = syndicateRepository.findById(savedFacility.getSyndicateId())
                .orElseThrow(() -> new ResourceNotFoundException("Syndicate not found with id: " + savedFacility.getSyndicateId()));
        Long borrowerId = syndicate.getBorrowerId();
        
        for (SharePie pie : savedFacility.getSharePies()) {
            FacilityInvestment investment = new FacilityInvestment();
            investment.setFacilityId(savedFacility.getId());
            investment.setInvestorId(pie.getInvestorId());
            investment.setBorrowerId(borrowerId); // Syndicateから取得したborrowerIdを設定
            // 按分金額計算: Money × Percentage.value → Money
            investment.setAmount(commitment.multiply(pie.getShare().getValue()));
            investment.setTransactionType("FACILITY_INVESTMENT");
            investment.setTransactionDate(LocalDate.now());
            investments.add(investment);
        }
        facilityInvestmentRepository.saveAll(investments);

        return savedFacility;
    }

    public List<Facility> getAllFacilities() {
        return facilityRepository.findAll();
    }

    public Page<Facility> getAllFacilities(Pageable pageable) {
        return facilityRepository.findAll(pageable);
    }

    public Facility getFacilityById(Long id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with id: " + id));
    }

    @Transactional
    public Facility updateFacility(Long id, UpdateFacilityRequest request) {
        Facility existingFacility = getFacilityById(id);

        // バリデーション実行（UpdateFacilityRequestを直接使用）
        facilityValidator.validateUpdateFacilityRequest(request, id);

        Facility entityToSave = new Facility();

        entityToSave.setId(id);
        entityToSave.setVersion(request.getVersion());

        // 基本情報を設定
        entityToSave.setSyndicateId(request.getSyndicateId());
        entityToSave.setCommitment(request.getCommitment());
        entityToSave.setCurrency(request.getCurrency());
        entityToSave.setStartDate(request.getStartDate());
        entityToSave.setEndDate(request.getEndDate());
        entityToSave.setInterestTerms(request.getInterestTerms());
        entityToSave.setCreatedAt(existingFacility.getCreatedAt());

        // 既存のSharePieエンティティをクリア
        existingFacility.getSharePies().clear();
        // SharePieRepositoryを用いて、既存のSharePie(Facilityに紐づく)を明示的に削除する。
        sharePieRepository.deleteByFacility_Id(id);

        // 新しいSharePieエンティティを作成して追加
        List<SharePie> newSharePies = new ArrayList<>();
        for (UpdateFacilityRequest.SharePieRequest pie : request.getSharePies()) {
            SharePie entity = new SharePie();
            entity.setInvestorId(pie.getInvestorId());
            entity.setShare(pie.getShare());
            entity.setFacility(entityToSave);
            newSharePies.add(entity);
        }
        entityToSave.setSharePies(newSharePies);

        return facilityRepository.save(entityToSave);
    }

    @Transactional
    public void deleteFacility(Long id) {
        if (!facilityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Facility not found with id: " + id);
        }
        facilityRepository.deleteById(id);
    }
}
