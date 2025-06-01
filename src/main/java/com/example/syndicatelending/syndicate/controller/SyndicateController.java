package com.example.syndicatelending.syndicate.controller;

import com.example.syndicatelending.syndicate.dto.CreateSyndicateRequest;
import com.example.syndicatelending.syndicate.entity.Syndicate;
import com.example.syndicatelending.syndicate.service.SyndicateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/syndicates")
public class SyndicateController {
    private final SyndicateService syndicateService;

    public SyndicateController(SyndicateService syndicateService) {
        this.syndicateService = syndicateService;
    }

    @PostMapping
    public ResponseEntity<Syndicate> createSyndicate(@RequestBody CreateSyndicateRequest request) {
        Syndicate syndicate = new Syndicate(request.getName(), request.getLeadBankId(), request.getMemberInvestorIds());
        Syndicate saved = syndicateService.createSyndicate(syndicate);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Syndicate> getSyndicate(@PathVariable Long id) {
        return ResponseEntity.ok(syndicateService.getSyndicateById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Syndicate>> getAllSyndicates(Pageable pageable) {
        return ResponseEntity.ok(syndicateService.getAllSyndicates(pageable));
    }
}
