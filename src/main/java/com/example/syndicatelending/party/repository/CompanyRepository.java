package com.example.syndicatelending.party.repository;

import com.example.syndicatelending.party.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Company Spring Data JPA Repository。
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByBusinessId(String businessId);

    List<Company> findByCompanyNameContainingIgnoreCase(String companyName);

    Optional<Company> findByRegistrationNumber(String registrationNumber);

    List<Company> findByIndustry(String industry);

    List<Company> findByCountry(String country);

    boolean existsByBusinessId(String businessId);
}