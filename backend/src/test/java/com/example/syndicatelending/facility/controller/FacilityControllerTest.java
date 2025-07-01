package com.example.syndicatelending.facility.controller;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import com.example.syndicatelending.facility.dto.CreateFacilityRequest;
import com.example.syndicatelending.facility.dto.UpdateFacilityRequest;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.service.FacilityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FacilityController 単体テスト。
 */
@WebMvcTest(FacilityController.class)
class FacilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FacilityService facilityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void Facilityを正常に作成できる() throws Exception {
        CreateFacilityRequest request = createValidFacilityRequest();
        Facility facility = new Facility();
        facility.setId(1L);
        facility.setSyndicateId(1L);
        facility.setCommitment(Money.of(BigDecimal.valueOf(5000000)));
        facility.setCurrency("USD");

        when(facilityService.createFacility(any(CreateFacilityRequest.class))).thenReturn(facility);

        mockMvc.perform(post("/api/v1/facilities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syndicateId").value(1))
                .andExpect(jsonPath("$.commitment").value(5000000))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void 全てのFacilityリストを取得できる() throws Exception {
        Facility facility = new Facility();
        facility.setId(1L);
        List<Facility> facilities = List.of(facility);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Facility> facilityPage = new PageImpl<>(facilities, pageable, facilities.size());

        when(facilityService.getAllFacilities(any(Pageable.class))).thenReturn(facilityPage);

        mockMvc.perform(get("/api/v1/facilities"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void 存在しないFacilityを取得すると404が返る() throws Exception {
        when(facilityService.getFacilityById(999L))
                .thenThrow(new com.example.syndicatelending.common.application.exception.ResourceNotFoundException(
                        "Facility not found"));

        mockMvc.perform(get("/api/v1/facilities/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void バリデーションエラーで400が返る() throws Exception {
        CreateFacilityRequest request = new CreateFacilityRequest();
        // 必要なフィールドを設定しない（バリデーションエラー）

        // バリデーションエラーをシミュレート
        when(facilityService.createFacility(any(CreateFacilityRequest.class)))
                .thenThrow(new com.example.syndicatelending.common.application.exception.BusinessRuleViolationException(
                        "Validation failed"));

        mockMvc.perform(post("/api/v1/facilities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    } // Update Tests

    @Test
    void Facilityを正常に更新できる() throws Exception {
        Long facilityId = 1L;
        UpdateFacilityRequest updateRequest = createValidUpdateFacilityRequest();
        updateRequest.setCommitment(Money.of(BigDecimal.valueOf(6000000))); // 更新: Commitmentを6000000に
        updateRequest.setInterestTerms("LIBOR + 3%"); // 更新: Interest Termsを変更
        updateRequest.setVersion(1L); // バージョン情報

        Facility updatedFacility = new Facility();
        updatedFacility.setId(facilityId);
        updatedFacility.setCommitment(Money.of(BigDecimal.valueOf(6000000)));
        updatedFacility.setInterestTerms("LIBOR + 3%");

        when(facilityService.updateFacility(eq(facilityId), any(UpdateFacilityRequest.class)))
                .thenReturn(updatedFacility);

        mockMvc.perform(put("/api/v1/facilities/" + facilityId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commitment").value(6000000))
                .andExpect(jsonPath("$.interestTerms").value("LIBOR + 3%"));
    }

    @Test
    void 存在しないFacilityを更新すると404が返る() throws Exception {
        UpdateFacilityRequest updateRequest = createValidUpdateFacilityRequest();

        when(facilityService.updateFacility(eq(999L), any(UpdateFacilityRequest.class)))
                .thenThrow(new com.example.syndicatelending.common.application.exception.ResourceNotFoundException(
                        "Facility not found"));

        mockMvc.perform(put("/api/v1/facilities/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void バージョンが異なる場合は楽観的排他制御でエラーになる() throws Exception {
        Long facilityId = 1L;
        UpdateFacilityRequest updateRequest = createValidUpdateFacilityRequest();
        updateRequest.setVersion(1L); // 古いバージョン

        when(facilityService.updateFacility(eq(facilityId), any(UpdateFacilityRequest.class)))
                .thenThrow(new com.example.syndicatelending.common.application.exception.BusinessRuleViolationException(
                        "Facility has been modified by another user"));

        mockMvc.perform(put("/api/v1/facilities/" + facilityId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    // Delete Tests
    @Test
    void Facilityを正常に削除できる() throws Exception {
        Long facilityId = 1L;

        doNothing().when(facilityService).deleteFacility(facilityId);

        mockMvc.perform(delete("/api/v1/facilities/" + facilityId))
                .andExpect(status().isNoContent());

        verify(facilityService).deleteFacility(facilityId);
    }

    @Test
    void 存在しないFacilityを削除すると404が返る() throws Exception {
        doThrow(new com.example.syndicatelending.common.application.exception.ResourceNotFoundException(
                "Facility not found"))
                .when(facilityService).deleteFacility(999L);

        mockMvc.perform(delete("/api/v1/facilities/999"))
                .andExpect(status().isNotFound());
    }

    private CreateFacilityRequest createValidFacilityRequest() {
        CreateFacilityRequest request = new CreateFacilityRequest();
        request.setSyndicateId(1L);
        request.setCommitment(Money.of(BigDecimal.valueOf(5000000)));
        request.setCurrency("USD");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2026, 1, 1));
        request.setInterestTerms("LIBOR + 2%");

        // 合計100%のSharePie
        CreateFacilityRequest.SharePieRequest pie1 = new CreateFacilityRequest.SharePieRequest();
        pie1.setInvestorId(1L);
        pie1.setShare(Percentage.of(BigDecimal.valueOf(0.4))); // 40%

        CreateFacilityRequest.SharePieRequest pie2 = new CreateFacilityRequest.SharePieRequest();
        pie2.setInvestorId(2L);
        pie2.setShare(Percentage.of(BigDecimal.valueOf(0.35))); // 35%

        CreateFacilityRequest.SharePieRequest pie3 = new CreateFacilityRequest.SharePieRequest();
        pie3.setInvestorId(3L);
        pie3.setShare(Percentage.of(BigDecimal.valueOf(0.25))); // 25%

        List<CreateFacilityRequest.SharePieRequest> sharePies = Arrays.asList(pie1, pie2, pie3);
        request.setSharePies(sharePies);

        return request;
    }

    private UpdateFacilityRequest createValidUpdateFacilityRequest() {
        UpdateFacilityRequest request = new UpdateFacilityRequest();
        request.setSyndicateId(1L);
        request.setCommitment(Money.of(BigDecimal.valueOf(5000000)));
        request.setCurrency("USD");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2026, 1, 1));
        request.setInterestTerms("LIBOR + 2%");
        request.setVersion(1L); // デフォルトバージョン

        // 合計100%のSharePie
        UpdateFacilityRequest.SharePieRequest pie1 = new UpdateFacilityRequest.SharePieRequest();
        pie1.setInvestorId(1L);
        pie1.setShare(Percentage.of(BigDecimal.valueOf(0.4))); // 40%

        UpdateFacilityRequest.SharePieRequest pie2 = new UpdateFacilityRequest.SharePieRequest();
        pie2.setInvestorId(2L);
        pie2.setShare(Percentage.of(BigDecimal.valueOf(0.35))); // 35%

        UpdateFacilityRequest.SharePieRequest pie3 = new UpdateFacilityRequest.SharePieRequest();
        pie3.setInvestorId(3L);
        pie3.setShare(Percentage.of(BigDecimal.valueOf(0.25))); // 25%

        List<UpdateFacilityRequest.SharePieRequest> sharePies = Arrays.asList(pie1, pie2, pie3);
        request.setSharePies(sharePies);

        return request;
    }
}
