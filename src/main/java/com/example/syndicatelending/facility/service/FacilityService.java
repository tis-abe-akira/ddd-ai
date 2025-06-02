package com.example.syndicatelending.facility.service;

import com.example.syndicatelending.facility.dto.CreateFacilityRequest;
import com.example.syndicatelending.facility.domain.FacilityValidator;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.entity.SharePie;
import com.example.syndicatelending.facility.repository.FacilityRepository;

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
}
