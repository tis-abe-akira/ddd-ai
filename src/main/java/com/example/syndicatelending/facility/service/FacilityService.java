package com.example.syndicatelending.facility.service;

import com.example.syndicatelending.facility.dto.CreateFacilityRequest;
import com.example.syndicatelending.facility.dto.UpdateFacilityRequest;
import com.example.syndicatelending.facility.domain.FacilityValidator;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.entity.SharePie;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;

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

    public FacilityService(FacilityRepository facilityRepository, FacilityValidator facilityValidator) {
        this.facilityRepository = facilityRepository;
        this.facilityValidator = facilityValidator;
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

        // 4. 保存（cascadeによりSharePieも一緒に保存される）
        return facilityRepository.save(facility);
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

        // バリデーション実行（CreateFacilityRequestに変換）
        CreateFacilityRequest validationRequest = convertToCreateRequest(request);
        facilityValidator.validateCreateFacilityRequest(validationRequest);

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

        // SharePieの設定
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

    /**
     * UpdateFacilityRequestをCreateFacilityRequestに変換（バリデーション用）
     */
    private CreateFacilityRequest convertToCreateRequest(UpdateFacilityRequest updateRequest) {
        CreateFacilityRequest createRequest = new CreateFacilityRequest();
        createRequest.setSyndicateId(updateRequest.getSyndicateId());
        createRequest.setCommitment(updateRequest.getCommitment());
        createRequest.setCurrency(updateRequest.getCurrency());
        createRequest.setStartDate(updateRequest.getStartDate());
        createRequest.setEndDate(updateRequest.getEndDate());
        createRequest.setInterestTerms(updateRequest.getInterestTerms());

        // SharePieRequestの変換
        List<CreateFacilityRequest.SharePieRequest> sharePies = new ArrayList<>();
        for (UpdateFacilityRequest.SharePieRequest updatePie : updateRequest.getSharePies()) {
            CreateFacilityRequest.SharePieRequest createPie = new CreateFacilityRequest.SharePieRequest();
            createPie.setInvestorId(updatePie.getInvestorId());
            createPie.setShare(updatePie.getShare());
            sharePies.add(createPie);
        }
        createRequest.setSharePies(sharePies);

        return createRequest;
    }

    @Transactional
    public void deleteFacility(Long id) {
        if (!facilityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Facility not found with id: " + id);
        }
        facilityRepository.deleteById(id);
    }
}
