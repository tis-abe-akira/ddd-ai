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
import com.example.syndicatelending.common.statemachine.facility.FacilityState;
import com.example.syndicatelending.common.statemachine.facility.FacilityEvent;
import com.example.syndicatelending.common.statemachine.EntityStateService;
import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import org.springframework.statemachine.StateMachine;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final EntityStateService entityStateService;
    
    @Autowired
    private StateMachine<FacilityState, FacilityEvent> stateMachine;

    public FacilityService(FacilityRepository facilityRepository, FacilityValidator facilityValidator,
            SharePieRepository sharePieRepository, FacilityInvestmentRepository facilityInvestmentRepository,
            SyndicateRepository syndicateRepository, EntityStateService entityStateService) {
        this.facilityRepository = facilityRepository;
        this.facilityValidator = facilityValidator;
        this.sharePieRepository = sharePieRepository;
        this.facilityInvestmentRepository = facilityInvestmentRepository;
        this.syndicateRepository = syndicateRepository;
        this.entityStateService = entityStateService;
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

        // 【重要】Facility組成時のBorrower/Investor状態遷移実行
        entityStateService.onFacilityCreated(savedFacility);

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

        // 状態チェック
        if (!existingFacility.canBeModified()) {
            throw new BusinessRuleViolationException(
                "FIXED状態のFacilityは変更できません。現在の状態: " + existingFacility.getStatus());
        }

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

        Facility savedFacility = facilityRepository.save(entityToSave);

        // 既存のFacilityInvestmentを削除
        facilityInvestmentRepository.deleteByFacilityId(id);

        // 新しいFacilityInvestmentを生成・保存
        List<FacilityInvestment> newInvestments = new ArrayList<>();
        Money newCommitment = savedFacility.getCommitment();
        
        // Facility → Syndicate → BorrowerIdを取得
        Syndicate syndicate = syndicateRepository.findById(savedFacility.getSyndicateId())
                .orElseThrow(() -> new ResourceNotFoundException("Syndicate not found with id: " + savedFacility.getSyndicateId()));
        Long borrowerId = syndicate.getBorrowerId();
        
        for (SharePie pie : savedFacility.getSharePies()) {
            FacilityInvestment investment = new FacilityInvestment();
            investment.setFacilityId(savedFacility.getId());
            investment.setInvestorId(pie.getInvestorId());
            investment.setBorrowerId(borrowerId);
            // 按分金額計算: Money × Percentage.value → Money
            investment.setAmount(newCommitment.multiply(pie.getShare().getValue()));
            investment.setTransactionType("FACILITY_INVESTMENT");
            investment.setTransactionDate(LocalDate.now());
            newInvestments.add(investment);
        }
        facilityInvestmentRepository.saveAll(newInvestments);

        return savedFacility;
    }

    @Transactional
    public void deleteFacility(Long id) {
        if (!facilityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Facility not found with id: " + id);
        }
        facilityRepository.deleteById(id);
    }

    @Transactional
    public void fixFacility(Long facilityId) {
        Facility facility = getFacilityById(facilityId);
        
        // 既にFIXED状態の場合はBusinessRuleViolationExceptionをスロー
        if (facility.getStatus() == FacilityState.FIXED) {
            throw new BusinessRuleViolationException(
                "FIXED状態のFacilityに対して2度目のドローダウンはできません。現在の状態: " + facility.getStatus());
        }
        
        if (facility.getStatus() != FacilityState.DRAFT) {
            throw new BusinessRuleViolationException(
                "DRAFT状態のFacilityのみFIXEDに変更できます。現在の状態: " + facility.getStatus());
        }
        
        // State Machine実行 - 現在の状態を設定してからイベント送信
        stateMachine.getExtendedState().getVariables().put("facilityId", facilityId);
        
        // State Machineを現在の状態に同期
        stateMachine.getStateMachineAccessor().doWithAllRegions(access -> {
            access.resetStateMachine(null);
        });
        
        // DRAFT状態からのイベント送信
        boolean result = stateMachine.sendEvent(FacilityEvent.DRAWDOWN_EXECUTED);
        if (!result) {
            throw new BusinessRuleViolationException(
                "状態遷移が失敗しました。DRAFT状態からのみドローダウンが可能です。");
        }
        
        // 状態更新
        facility.setStatus(FacilityState.FIXED);
        facilityRepository.save(facility);
    }
}
