package com.example.syndicatelending.party.controller;

import com.example.syndicatelending.party.dto.*;
import com.example.syndicatelending.party.entity.*;
import com.example.syndicatelending.party.service.PartyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<Company>> getAllCompanies() {
        List<Company> companies = partyService.getAllCompanies();
        return ResponseEntity.ok(companies);
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
    public ResponseEntity<List<Borrower>> getAllBorrowers() {
        List<Borrower> borrowers = partyService.getAllBorrowers();
        return ResponseEntity.ok(borrowers);
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
    public ResponseEntity<List<Investor>> getAllInvestors() {
        List<Investor> investors = partyService.getAllInvestors();
        return ResponseEntity.ok(investors);
    }

    @GetMapping("/investors/active")
    @Operation(summary = "Get all active investors")
    public ResponseEntity<List<Investor>> getActiveInvestors() {
        List<Investor> activeInvestors = partyService.getActiveInvestors();
        return ResponseEntity.ok(activeInvestors);
    }
}