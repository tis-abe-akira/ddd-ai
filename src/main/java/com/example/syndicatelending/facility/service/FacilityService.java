package com.example.syndicatelending.facility.service;

import com.example.syndicatelending.facility.dto.CreateFacilityRequest;
import com.example.syndicatelending.facility.entity.FacilityEntity;
import com.example.syndicatelending.facility.entity.SharePieEntity;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.facility.repository.SharePieRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FacilityService {
    private final FacilityRepository facilityRepository;
    private final SharePieRepository sharePieRepository;

    public FacilityService(FacilityRepository facilityRepository, SharePieRepository sharePieRepository) {
        this.facilityRepository = facilityRepository;
        this.sharePieRepository = sharePieRepository;
    }

    @Transactional
    public FacilityEntity createFacility(CreateFacilityRequest request) {
        FacilityEntity facility = new FacilityEntity(
                request.getSyndicateId(),
                request.getCommitment(),
                request.getCurrency(),
                request.getStartDate(),
                request.getEndDate(),
                request.getInterestTerms()
        );
        FacilityEntity saved = facilityRepository.save(facility);
        // SharePieEntity生成
        for (CreateFacilityRequest.SharePieRequest pie : request.getSharePies()) {
            SharePieEntity entity = new SharePieEntity();
            entity.setFacilityId(saved.getId());
            entity.setInvestorId(pie.getInvestorId());
            entity.setShare(pie.getShare().getValue().toString());
            sharePieRepository.save(entity);
        }
        return saved;
    }
}
