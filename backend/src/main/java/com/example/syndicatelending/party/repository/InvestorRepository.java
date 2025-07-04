package com.example.syndicatelending.party.repository;

import com.example.syndicatelending.party.entity.Investor;
import com.example.syndicatelending.party.entity.InvestorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Investor Spring Data JPA Repository。
 */
@Repository
public interface InvestorRepository extends JpaRepository<Investor, Long>, JpaSpecificationExecutor<Investor> {

    List<Investor> findByNameContainingIgnoreCase(String name);

    List<Investor> findByInvestorType(String investorType);

    List<Investor> findByIsActiveTrue();

    Page<Investor> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Investor> findByInvestorType(InvestorType investorType, Pageable pageable);
}
