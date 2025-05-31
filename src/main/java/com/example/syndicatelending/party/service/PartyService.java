package com.example.syndicatelending.party.service;

import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.party.dto.*;
import com.example.syndicatelending.party.entity.*;
import com.example.syndicatelending.party.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Party管理サービス（統合サービス）。
 */
@Service
@Transactional
public class PartyService {

    private final CompanyRepository companyRepository;
    private final BorrowerRepository borrowerRepository;
    private final InvestorRepository investorRepository;

    public PartyService(CompanyRepository companyRepository,
                        BorrowerRepository borrowerRepository,
                        InvestorRepository investorRepository) {
        this.companyRepository = companyRepository;
        this.borrowerRepository = borrowerRepository;
        this.investorRepository = investorRepository;
    }

    // Company operations
    public Company createCompany(CreateCompanyRequest request) {
        Company company = new Company(
                request.getCompanyName(),
                request.getRegistrationNumber(),
                request.getIndustry(),
                request.getAddress(),
                request.getCountry()
        );
        return companyRepository.save(company);
    }

    @Transactional(readOnly = true)
    public Company getCompanyById(String businessId) {
        return companyRepository.findByBusinessId(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + businessId));
    }

    @Transactional(readOnly = true)
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    // Borrower operations
    public Borrower createBorrower(CreateBorrowerRequest request) {
        if (request.getCompanyId() != null && !request.getCompanyId().trim().isEmpty()) {
            if (!companyRepository.existsByBusinessId(request.getCompanyId())) {
                throw new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId());
            }
        }

        Borrower borrower = new Borrower(
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getCompanyId(),
                request.getCreditLimit(),
                request.getCreditRating()
        );
        return borrowerRepository.save(borrower);
    }

    @Transactional(readOnly = true)
    public Borrower getBorrowerById(String businessId) {
        return borrowerRepository.findByBusinessId(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found with ID: " + businessId));
    }

    @Transactional(readOnly = true)
    public List<Borrower> getAllBorrowers() {
        return borrowerRepository.findAll();
    }

    // Investor operations
    public Investor createInvestor(CreateInvestorRequest request) {
        if (request.getCompanyId() != null && !request.getCompanyId().trim().isEmpty()) {
            if (!companyRepository.existsByBusinessId(request.getCompanyId())) {
                throw new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId());
            }
        }

        Investor investor = new Investor(
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getCompanyId(),
                request.getInvestmentCapacity(),
                request.getInvestorType()
        );
        return investorRepository.save(investor);
    }

    @Transactional(readOnly = true)
    public Investor getInvestorById(String businessId) {
        return investorRepository.findByBusinessId(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Investor not found with ID: " + businessId));
    }

    @Transactional(readOnly = true)
    public List<Investor> getAllInvestors() {
        return investorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Investor> getActiveInvestors() {
        return investorRepository.findByIsActiveTrue();
    }
}