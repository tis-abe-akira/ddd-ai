package com.example.syndicatelending.facility.repository;

import com.example.syndicatelending.facility.entity.SharePieEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharePieRepository extends JpaRepository<SharePieEntity, Long> {
    List<SharePieEntity> findByFacilityId(Long facilityId);
}
