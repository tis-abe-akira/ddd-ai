package com.example.syndicatelending.facility.controller;

import com.example.syndicatelending.facility.dto.CreateFacilityRequest;
import com.example.syndicatelending.facility.entity.FacilityEntity;
import com.example.syndicatelending.facility.service.FacilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/facilities")
public class FacilityController {
    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @PostMapping
    public ResponseEntity<FacilityEntity> createFacility(@RequestBody CreateFacilityRequest request) {
        FacilityEntity facility = facilityService.createFacility(request);
        return ResponseEntity.ok(facility);
    }
}
