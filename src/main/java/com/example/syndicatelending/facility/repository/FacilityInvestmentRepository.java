package com.example.syndicatelending.facility.repository;

import com.example.syndicatelending.facility.entity.FacilityInvestment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityInvestmentRepository extends JpaRepository<FacilityInvestment, Long> {
}
