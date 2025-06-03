package com.example.syndicatelending.facility.service;

import com.example.syndicatelending.facility.dto.CreateFacilityRequest;
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
import java.util.Optional;

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
    public Facility updateFacility(Long id, CreateFacilityRequest request) {
        Optional<Facility> existingFacility = facilityRepository.findById(id);
        if (existingFacility.isEmpty()) {
            throw new ResourceNotFoundException("Facility not found with id: " + id);
        }

        Facility facility = existingFacility.get();

        // 基本情報を更新
        facility.setSyndicateId(request.getSyndicateId());
        facility.setCommitment(request.getCommitment());
        facility.setCurrency(request.getCurrency());
        facility.setStartDate(request.getStartDate());
        facility.setEndDate(request.getEndDate());
        facility.setInterestTerms(request.getInterestTerms());

        // SharePieの更新（既存を削除して新規作成）
        facility.getSharePies().clear();
        List<SharePie> newSharePies = new ArrayList<>();
        for (CreateFacilityRequest.SharePieRequest pie : request.getSharePies()) {
            SharePie entity = new SharePie();
            entity.setInvestorId(pie.getInvestorId());
            entity.setShare(pie.getShare());
            entity.setFacility(facility);
            newSharePies.add(entity);
        }
        facility.setSharePies(newSharePies);

        // バリデーション実行
        facilityValidator.validateCreateFacilityRequest(request);

        return facilityRepository.save(facility);
    }

    @Transactional
    public void deleteFacility(Long id) {
        if (!facilityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Facility not found with id: " + id);
        }
        facilityRepository.deleteById(id);
    }
}
