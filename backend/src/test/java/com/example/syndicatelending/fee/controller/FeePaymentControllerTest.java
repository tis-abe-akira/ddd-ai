package com.example.syndicatelending.fee.controller;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.fee.dto.CreateFeePaymentRequest;
import com.example.syndicatelending.fee.entity.FeeDistribution;
import com.example.syndicatelending.fee.entity.FeePayment;
import com.example.syndicatelending.fee.entity.FeeType;
import com.example.syndicatelending.fee.entity.RecipientType;
import com.example.syndicatelending.fee.service.FeePaymentService;
import com.example.syndicatelending.transaction.entity.TransactionStatus;
import com.example.syndicatelending.transaction.entity.TransactionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeePaymentController.class)
class FeePaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeePaymentService feePaymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 手数料支払いを正常に作成できる() throws Exception {
        CreateFeePaymentRequest request = createValidFeePaymentRequest();
        FeePayment feePayment = createMockFeePayment();

        when(feePaymentService.createFeePayment(any(CreateFeePaymentRequest.class)))
            .thenReturn(feePayment);

        mockMvc.perform(post("/api/v1/fees/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.feeType").value("MANAGEMENT_FEE"))
                .andExpect(jsonPath("$.amount").exists())
                .andExpect(jsonPath("$.transactionType").value("FEE_PAYMENT"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void 手数料支払い詳細を取得できる() throws Exception {
        Long feePaymentId = 1L;
        FeePayment feePayment = createMockFeePayment();

        when(feePaymentService.getFeePaymentById(eq(feePaymentId)))
            .thenReturn(feePayment);

        mockMvc.perform(get("/api/v1/fees/payments/{id}", feePaymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.feeType").value("MANAGEMENT_FEE"))
                .andExpect(jsonPath("$.description").value("Test Management Fee"));
    }

    @Test
    void Facility別手数料履歴を取得できる() throws Exception {
        Long facilityId = 1L;
        List<FeePayment> feePayments = Arrays.asList(
            createMockFeePayment(),
            createMockCommitmentFeePayment()
        );

        when(feePaymentService.getFeePaymentsByFacility(eq(facilityId)))
            .thenReturn(feePayments);

        mockMvc.perform(get("/api/v1/fees/payments/facility/{facilityId}", facilityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].feeType").value("MANAGEMENT_FEE"))
                .andExpect(jsonPath("$[1].feeType").value("COMMITMENT_FEE"));
    }

    @Test
    void 手数料タイプ別検索ができる() throws Exception {
        List<FeePayment> managementFees = Arrays.asList(createMockFeePayment());

        when(feePaymentService.getFeePaymentsByType(eq(FeeType.MANAGEMENT_FEE)))
            .thenReturn(managementFees);

        mockMvc.perform(get("/api/v1/fees/payments/type/{feeType}", "MANAGEMENT_FEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].feeType").value("MANAGEMENT_FEE"));
    }

    @Test
    void 日付範囲検索ができる() throws Exception {
        List<FeePayment> feePayments = Arrays.asList(createMockFeePayment());

        when(feePaymentService.getFeePaymentsByDateRange(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(feePayments);

        mockMvc.perform(get("/api/v1/fees/payments/date-range")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void 全手数料支払いをページング取得できる() throws Exception {
        List<FeePayment> feePayments = Arrays.asList(createMockFeePayment());
        Page<FeePayment> page = new PageImpl<>(feePayments, PageRequest.of(0, 20), 1);

        when(feePaymentService.getAllFeePayments(any(PageRequest.class)))
            .thenReturn(page);

        mockMvc.perform(get("/api/v1/fees/payments")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void 無効なリクエストで手数料支払い作成時400エラーを返す() throws Exception {
        CreateFeePaymentRequest invalidRequest = new CreateFeePaymentRequest();
        invalidRequest.setFacilityId(null); // 必須フィールドがnull
        invalidRequest.setFeeAmount(new BigDecimal("-1000")); // 負の値

        mockMvc.perform(post("/api/v1/fees/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    private CreateFeePaymentRequest createValidFeePaymentRequest() {
        CreateFeePaymentRequest request = new CreateFeePaymentRequest();
        request.setFacilityId(1L);
        request.setBorrowerId(1L);
        request.setFeeType(FeeType.MANAGEMENT_FEE);
        request.setFeeDate(LocalDate.of(2025, 1, 31));
        request.setFeeAmount(new BigDecimal("25000.00"));
        request.setCalculationBase(new BigDecimal("5000000.00"));
        request.setFeeRate(0.5);
        request.setRecipientType("BANK");
        request.setRecipientId(1L);
        request.setCurrency("USD");
        request.setDescription("Test Management Fee");
        return request;
    }

    private FeePayment createMockFeePayment() {
        FeePayment feePayment = new FeePayment(
            FeeType.MANAGEMENT_FEE,
            LocalDate.of(2025, 1, 31),
            Money.of(new BigDecimal("25000.00")),
            Money.of(new BigDecimal("5000000.00")),
            0.5,
            RecipientType.LEAD_BANK,
            1L,
            "USD",
            "Test Management Fee"
        );
        
        // Transaction基底クラスのフィールドをモック用に設定
        feePayment.setFacilityId(1L);
        feePayment.setBorrowerId(1L);
        feePayment.setTransactionDate(LocalDate.of(2025, 1, 31));
        feePayment.setTransactionType(TransactionType.FEE_PAYMENT);
        feePayment.setStatus(TransactionStatus.PENDING);
        feePayment.setAmount(Money.of(new BigDecimal("25000.00")));
        feePayment.setCreatedAt(LocalDateTime.now());
        feePayment.setUpdatedAt(LocalDateTime.now());
        feePayment.setVersion(0L);
        
        // ReflectionでIDを設定（Transaction基底クラスのprotected setId()を使用）
        try {
            Field idField = feePayment.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(feePayment, 1L);
        } catch (Exception e) {
            // テスト環境でのReflection失敗は無視
        }
        
        return feePayment;
    }

    private FeePayment createMockCommitmentFeePayment() {
        FeePayment feePayment = new FeePayment(
            FeeType.COMMITMENT_FEE,
            LocalDate.of(2025, 1, 31),
            Money.of(new BigDecimal("12500.00")),
            Money.of(new BigDecimal("5000000.00")),
            0.25,
            RecipientType.AUTO_DISTRIBUTE,
            1L,
            "USD",
            "Test Commitment Fee"
        );
        
        // Transaction基底クラスのフィールドをモック用に設定
        feePayment.setFacilityId(1L);
        feePayment.setBorrowerId(1L);
        feePayment.setTransactionDate(LocalDate.of(2025, 1, 31));
        feePayment.setTransactionType(TransactionType.FEE_PAYMENT);
        feePayment.setStatus(TransactionStatus.PENDING);
        feePayment.setAmount(Money.of(new BigDecimal("12500.00")));
        feePayment.setCreatedAt(LocalDateTime.now());
        feePayment.setUpdatedAt(LocalDateTime.now());
        feePayment.setVersion(0L);

        // 投資家配分をモック
        FeeDistribution dist1 = new FeeDistribution("INVESTOR", 1L, Money.of(new BigDecimal("5000.00")), 40.0, "USD");
        FeeDistribution dist2 = new FeeDistribution("INVESTOR", 2L, Money.of(new BigDecimal("4375.00")), 35.0, "USD");
        FeeDistribution dist3 = new FeeDistribution("INVESTOR", 3L, Money.of(new BigDecimal("3125.00")), 25.0, "USD");
        
        dist1.setFeePayment(feePayment);
        dist2.setFeePayment(feePayment);
        dist3.setFeePayment(feePayment);
        
        feePayment.setFeeDistributions(Arrays.asList(dist1, dist2, dist3));
        
        // ReflectionでIDを設定
        try {
            Field idField = feePayment.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(feePayment, 2L);
        } catch (Exception e) {
            // テスト環境でのReflection失敗は無視
        }
        
        return feePayment;
    }
}