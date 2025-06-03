package com.example.syndicatelending.facility.controller;

import com.example.syndicatelending.facility.dto.CreateFacilityRequest;
import com.example.syndicatelending.facility.dto.UpdateFacilityRequest;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.service.FacilityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<Facility> createFacility(@RequestBody CreateFacilityRequest request) {
        Facility facility = facilityService.createFacility(request);
        return ResponseEntity.ok(facility);
    }

    @GetMapping
    public ResponseEntity<Page<Facility>> getAllFacilities(Pageable pageable) {
        Page<Facility> facilities = facilityService.getAllFacilities(pageable);
        return ResponseEntity.ok(facilities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Facility> getFacilityById(@PathVariable Long id) {
        Facility facility = facilityService.getFacilityById(id);
        return ResponseEntity.ok(facility);
    }

    /**
     * このメソッドは非推奨にする。（理由は更新時の楽観的排他制御を行っていないため）
     * Facilityの更新エンドポイント。
     * 
     * @deprecated
     *             このエンドポイントは楽観的排他制御を行っていないため、使用しないでください。
     *             代わりに、PUT /{id}/versionedを使用してください。
     * @param id
     * @param request
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<Facility> updateFacility(@PathVariable Long id, @RequestBody UpdateFacilityRequest request) {
        Facility updatedFacility = facilityService.updateFacility(id, request);
        return ResponseEntity.ok(updatedFacility);
    }

    @PutMapping("/{id}/versioned")
    public ResponseEntity<Facility> updateFacilityVersioned(@PathVariable Long id,
            @RequestBody UpdateFacilityRequest request) {
        Facility updatedFacility = facilityService.updateFacility(id, request);
        return ResponseEntity.ok(updatedFacility);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacility(@PathVariable Long id) {
        facilityService.deleteFacility(id);
        return ResponseEntity.noContent().build();
    }
}
