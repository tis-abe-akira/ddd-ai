package com.example.syndicatelending.facility.service;

import com.example.syndicatelending.facility.entity.FacilityEntity;
import com.example.syndicatelending.facility.entity.SharePieEntity;
import com.example.syndicatelending.position.domain.Facility;
import com.example.syndicatelending.position.domain.SharePie;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;

import java.util.List;
import java.util.stream.Collectors;

public class FacilityMapper {
    // Entity → Domain
    public static Facility toDomain(FacilityEntity entity, List<SharePieEntity> sharePieEntities) {
        List<SharePie> sharePies = sharePieEntities.stream()
                .map(FacilityMapper::toDomainSharePie)
                .collect(Collectors.toList());
        return new Facility(
                entity.getSyndicateId(),
                Money.of(new java.math.BigDecimal(entity.getCommitment())),
                entity.getCurrency(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getInterestTerms(),
                sharePies);
    }

    public static SharePie toDomainSharePie(SharePieEntity entity) {
        return new SharePie(
                entity.getInvestorId(),
                Percentage.of(new java.math.BigDecimal(entity.getShare())));
    }

    // Domain → Entity
    public static FacilityEntity toEntity(Facility domain) {
        FacilityEntity entity = new FacilityEntity();
        entity.setSyndicateId(domain.getSyndicateId());
        entity.setCommitment(domain.getCommitment().getAmount().toString());
        entity.setCurrency(domain.getCurrency());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setInterestTerms(domain.getInterestTerms());
        return entity;
    }

    public static SharePieEntity toEntitySharePie(SharePie domain, Long facilityId) {
        SharePieEntity entity = new SharePieEntity();
        entity.setFacilityId(facilityId);
        entity.setInvestorId(domain.getInvestorId());
        entity.setShare(domain.getShare().getValue().toString());
        return entity;
    }
}
