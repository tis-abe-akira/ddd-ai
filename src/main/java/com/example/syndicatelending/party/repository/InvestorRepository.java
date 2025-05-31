package com.example.syndicatelending.party.repository;

import com.example.syndicatelending.party.entity.Investor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Investor Spring Data JPA Repository。
 */
@Repository
public interface InvestorRepository extends JpaRepository<Investor, Long> {

    Optional<Investor> findByBusinessId(String businessId);

    List<Investor> findByNameContainingIgnoreCase(String name);

    List<Investor> findByInvestorType(String investorType);

    List<Investor> findByIsActiveTrue();

    boolean existsByBusinessId(String businessId);
}