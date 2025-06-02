package com.example.syndicatelending.facility.controller;

import com.example.syndicatelending.facility.dto.CreateFacilityRequest;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.service.FacilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/facilities")
public class FacilityController {
    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @PostMapping
    public ResponseEntity<Facility> createFacility(@RequestBody CreateFacilityRequest request) {
        Facility facility = facilityService.createFacility(request);
        return ResponseEntity.ok(facility);
    }

    @GetMapping
    public ResponseEntity<List<Facility>> getAllFacilities() {
        List<Facility> facilities = facilityService.getAllFacilities();
        return ResponseEntity.ok(facilities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Facility> getFacilityById(@PathVariable Long id) {
        Optional<Facility> facility = facilityService.getFacilityById(id);
        return facility.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Facility> updateFacility(@PathVariable Long id, @RequestBody CreateFacilityRequest request) {
        try {
            Facility updatedFacility = facilityService.updateFacility(id, request);
            return ResponseEntity.ok(updatedFacility);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacility(@PathVariable Long id) {
        try {
            facilityService.deleteFacility(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
