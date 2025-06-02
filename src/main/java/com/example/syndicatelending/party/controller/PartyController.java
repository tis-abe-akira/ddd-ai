package com.example.syndicatelending.party.controller;

import com.example.syndicatelending.party.dto.*;
import com.example.syndicatelending.party.entity.*;
import com.example.syndicatelending.party.service.PartyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Party REST Controller（統合コントローラー）。
 */
@RestController
@RequestMapping("/api/v1/parties")
@Tag(name = "Party Management", description = "APIs for managing borrowers, investors, and companies")
public class PartyController {

    private final PartyService partyService;

    public PartyController(PartyService partyService) {
        this.partyService = partyService;
    }

    // Company endpoints
    @PostMapping("/companies")
    @Operation(summary = "Create a new company")
    public ResponseEntity<Company> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        Company company = partyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(company);
    }

    @GetMapping("/companies/{id}")
    @Operation(summary = "Get company by ID")
    public ResponseEntity<Company> getCompany(@PathVariable Long id) {
        Company company = partyService.getCompanyById(id);
        return ResponseEntity.ok(company);
    }

    @GetMapping("/companies")
    @Operation(summary = "Get all companies")
    public ResponseEntity<Page<Company>> getAllCompanies(Pageable pageable) {
        Page<Company> companies = partyService.getAllCompanies(pageable);
        return ResponseEntity.ok(companies);
    }

    @PutMapping("/companies/{id}")
    @Operation(summary = "Update company by ID")
    public ResponseEntity<Company> updateCompany(@PathVariable Long id,
            @Valid @RequestBody CreateCompanyRequest request) {
        try {
            Company updatedCompany = partyService.updateCompany(id, request);
            return ResponseEntity.ok(updatedCompany);
        } catch (com.example.syndicatelending.common.application.exception.ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/companies/{id}")
    @Operation(summary = "Delete company by ID")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        try {
            partyService.deleteCompany(id);
            return ResponseEntity.noContent().build();
        } catch (com.example.syndicatelending.common.application.exception.ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Borrower endpoints
    @PostMapping("/borrowers")
    @Operation(summary = "Create a new borrower")
    public ResponseEntity<Borrower> createBorrower(@Valid @RequestBody CreateBorrowerRequest request) {
        Borrower borrower = partyService.createBorrower(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(borrower);
    }

    @GetMapping("/borrowers/{id}")
    @Operation(summary = "Get borrower by ID")
    public ResponseEntity<Borrower> getBorrower(@PathVariable Long id) {
        Borrower borrower = partyService.getBorrowerById(id);
        return ResponseEntity.ok(borrower);
    }

    @GetMapping("/borrowers")
    @Operation(summary = "Get all borrowers")
    public ResponseEntity<Page<Borrower>> getAllBorrowers(Pageable pageable) {
        Page<Borrower> borrowers = partyService.getAllBorrowers(pageable);
        return ResponseEntity.ok(borrowers);
    }

    @PutMapping("/borrowers/{id}")
    @Operation(summary = "Update borrower by ID")
    public ResponseEntity<Borrower> updateBorrower(@PathVariable Long id,
            @Valid @RequestBody CreateBorrowerRequest request) {
        try {
            Borrower updatedBorrower = partyService.updateBorrower(id, request);
            return ResponseEntity.ok(updatedBorrower);
        } catch (com.example.syndicatelending.common.application.exception.ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (com.example.syndicatelending.common.application.exception.BusinessRuleViolationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/borrowers/{id}")
    @Operation(summary = "Delete borrower by ID")
    public ResponseEntity<Void> deleteBorrower(@PathVariable Long id) {
        try {
            partyService.deleteBorrower(id);
            return ResponseEntity.noContent().build();
        } catch (com.example.syndicatelending.common.application.exception.ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Investor endpoints
    @PostMapping("/investors")
    @Operation(summary = "Create a new investor")
    public ResponseEntity<Investor> createInvestor(@Valid @RequestBody CreateInvestorRequest request) {
        Investor investor = partyService.createInvestor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(investor);
    }

    @GetMapping("/investors/{id}")
    @Operation(summary = "Get investor by ID")
    public ResponseEntity<Investor> getInvestor(@PathVariable Long id) {
        Investor investor = partyService.getInvestorById(id);
        return ResponseEntity.ok(investor);
    }

    @GetMapping("/investors")
    @Operation(summary = "Get all investors")
    public ResponseEntity<Page<Investor>> getAllInvestors(Pageable pageable) {
        Page<Investor> investors = partyService.getAllInvestors(pageable);
        return ResponseEntity.ok(investors);
    }

    @GetMapping("/investors/active")
    @Operation(summary = "Get all active investors (paged)")
    public ResponseEntity<Page<Investor>> getActiveInvestors(Pageable pageable) {
        Page<Investor> activeInvestors = partyService.getActiveInvestors(pageable);
        return ResponseEntity.ok(activeInvestors);
    }

    @PutMapping("/investors/{id}")
    @Operation(summary = "Update investor by ID")
    public ResponseEntity<Investor> updateInvestor(@PathVariable Long id,
            @Valid @RequestBody CreateInvestorRequest request) {
        try {
            Investor updatedInvestor = partyService.updateInvestor(id, request);
            return ResponseEntity.ok(updatedInvestor);
        } catch (com.example.syndicatelending.common.application.exception.ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/investors/{id}")
    @Operation(summary = "Delete investor by ID")
    public ResponseEntity<Void> deleteInvestor(@PathVariable Long id) {
        try {
            partyService.deleteInvestor(id);
            return ResponseEntity.noContent().build();
        } catch (com.example.syndicatelending.common.application.exception.ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/investors/search")
    @Operation(summary = "Search investors by name and/or type")
    public ResponseEntity<Page<Investor>> searchInvestors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) InvestorType investorType,
            Pageable pageable) {
        Page<Investor> result = partyService.searchInvestors(name, investorType, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/borrowers/search")
    @Operation(summary = "Search borrowers by name and/or credit rating")
    public ResponseEntity<Page<Borrower>> searchBorrowers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) CreditRating creditRating,
            Pageable pageable) {
        Page<Borrower> result = partyService.searchBorrowers(name, creditRating, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/companies/search")
    @Operation(summary = "Search companies by name and/or industry")
    public ResponseEntity<Page<Company>> searchCompanies(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Industry industry,
            Pageable pageable) {
        Page<Company> result = partyService.searchCompanies(name, industry, pageable);
        return ResponseEntity.ok(result);
    }
}
