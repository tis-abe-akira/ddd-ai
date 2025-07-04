package com.example.syndicatelending.loan.controller;

import com.example.syndicatelending.loan.dto.CreateDrawdownRequest;
import com.example.syndicatelending.loan.entity.Drawdown;
import com.example.syndicatelending.loan.service.DrawdownService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans/drawdowns")
public class DrawdownController {
    private final DrawdownService drawdownService;

    public DrawdownController(DrawdownService drawdownService) {
        this.drawdownService = drawdownService;
    }

    @PostMapping
    public ResponseEntity<Drawdown> createDrawdown(@RequestBody CreateDrawdownRequest request) {
        Drawdown drawdown = drawdownService.createDrawdown(request);
        return ResponseEntity.ok(drawdown);
    }

    @GetMapping
    public ResponseEntity<List<Drawdown>> getAllDrawdowns() {
        List<Drawdown> drawdowns = drawdownService.getAllDrawdowns();
        return ResponseEntity.ok(drawdowns);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<Drawdown>> getAllDrawdowns(Pageable pageable) {
        Page<Drawdown> drawdowns = drawdownService.getAllDrawdowns(pageable);
        return ResponseEntity.ok(drawdowns);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Drawdown> getDrawdownById(@PathVariable Long id) {
        Drawdown drawdown = drawdownService.getDrawdownById(id);
        return ResponseEntity.ok(drawdown);
    }

    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<Drawdown>> getDrawdownsByFacilityId(@PathVariable Long facilityId) {
        List<Drawdown> drawdowns = drawdownService.getDrawdownsByFacilityId(facilityId);
        return ResponseEntity.ok(drawdowns);
    }
}
