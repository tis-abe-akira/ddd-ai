package com.example.syndicatelending.party.service;

import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.party.dto.*;
import com.example.syndicatelending.party.entity.*;
import com.example.syndicatelending.party.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
                request.getCountry());
        return companyRepository.save(company);
    }

    @Transactional(readOnly = true)
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Company> getAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    // Borrower operations
    public Borrower createBorrower(CreateBorrowerRequest request) {
        if (request.getCompanyId() != null && !request.getCompanyId().trim().isEmpty()) {
            Long companyId;
            try {
                companyId = Long.parseLong(request.getCompanyId());
            } catch (NumberFormatException e) {
                throw new ResourceNotFoundException("Invalid company ID: " + request.getCompanyId());
            }
            if (!companyRepository.existsById(companyId)) {
                throw new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId());
            }
        }
        Borrower borrower = new Borrower(
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getCompanyId(),
                request.getCreditLimit(),
                request.getCreditRating());
        return borrowerRepository.save(borrower);
    }

    @Transactional(readOnly = true)
    public Borrower getBorrowerById(Long id) {
        return borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Borrower> getAllBorrowers() {
        return borrowerRepository.findAll();
    }

    // Investor operations
    public Investor createInvestor(CreateInvestorRequest request) {
        if (request.getCompanyId() != null && !request.getCompanyId().trim().isEmpty()) {
            Long companyId;
            try {
                companyId = Long.parseLong(request.getCompanyId());
            } catch (NumberFormatException e) {
                throw new ResourceNotFoundException("Invalid company ID: " + request.getCompanyId());
            }
            if (!companyRepository.existsById(companyId)) {
                throw new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId());
            }
        }
        Investor investor = new Investor(
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getCompanyId(),
                request.getInvestmentCapacity(),
                request.getInvestorType());
        return investorRepository.save(investor);
    }

    @Transactional(readOnly = true)
    public Investor getInvestorById(Long id) {
        return investorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investor not found with ID: " + id));
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
