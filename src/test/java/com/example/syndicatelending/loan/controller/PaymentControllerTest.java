package com.example.syndicatelending.loan.controller;

import com.example.syndicatelending.loan.dto.CreatePaymentRequest;
import com.example.syndicatelending.loan.entity.Payment;
import com.example.syndicatelending.loan.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PaymentController 単体テスト。
 */
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 支払いを正常に作成できる() throws Exception {
        CreatePaymentRequest request = createValidPaymentRequest();
        Payment payment = createMockPayment();

        when(paymentService.processPayment(any(CreatePaymentRequest.class))).thenReturn(payment);

        mockMvc.perform(post("/api/v1/loans/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.loanId").value(1L))
                .andExpect(jsonPath("$.totalAmount").exists())
                .andExpect(jsonPath("$.principalAmount").exists())
                .andExpect(jsonPath("$.interestAmount").exists());

        verify(paymentService, times(1)).processPayment(any(CreatePaymentRequest.class));
    }

    @Test
    void ローンIDで支払い履歴を取得できる() throws Exception {
        Long loanId = 1L;
        List<Payment> payments = Arrays.asList(createMockPayment(), createMockPayment());

        when(paymentService.getPaymentsByLoanId(eq(loanId))).thenReturn(payments);

        mockMvc.perform(get("/api/v1/loans/payments/loan/{loanId}", loanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(paymentService, times(1)).getPaymentsByLoanId(eq(loanId));
    }

    @Test
    void 支払いIDで支払い詳細を取得できる() throws Exception {
        Long paymentId = 1L;
        Payment payment = createMockPayment();

        when(paymentService.getPaymentById(eq(paymentId))).thenReturn(payment);

        mockMvc.perform(get("/api/v1/loans/payments/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.loanId").value(1L));

        verify(paymentService, times(1)).getPaymentById(eq(paymentId));
    }

    @Test
    void 無効なリクエストで支払い作成時400エラーを返す() throws Exception {
        // principalAmountとinterestAmountをnullにしてgetTotalAmount()でNPEが発生しないよう
        // 代わりに負の値を設定してバリデーションエラーをテスト
        CreatePaymentRequest invalidRequest = new CreatePaymentRequest();
        invalidRequest.setLoanId(1L);
        invalidRequest.setPrincipalAmount(BigDecimal.valueOf(-1000)); // 負の値（無効）
        invalidRequest.setInterestAmount(BigDecimal.valueOf(-100)); // 負の値（無効）
        invalidRequest.setPaymentDate(LocalDate.now());
        invalidRequest.setCurrency("JPY");

        mockMvc.perform(post("/api/v1/loans/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).processPayment(any(CreatePaymentRequest.class));
    }

    private CreatePaymentRequest createValidPaymentRequest() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setLoanId(1L);
        request.setPrincipalAmount(BigDecimal.valueOf(1000000));
        request.setInterestAmount(BigDecimal.valueOf(50000));
        request.setPaymentDate(LocalDate.now());
        request.setCurrency("JPY");
        return request;
    }

    private Payment createMockPayment() {
        Payment payment = new Payment(
                1L, // loanId
                LocalDate.now(), // paymentDate
                com.example.syndicatelending.common.domain.model.Money.of(BigDecimal.valueOf(1050000)), // totalAmount
                com.example.syndicatelending.common.domain.model.Money.of(BigDecimal.valueOf(1000000)), // principalAmount
                com.example.syndicatelending.common.domain.model.Money.of(BigDecimal.valueOf(50000)), // interestAmount
                "JPY" // currency
        );
        // Transaction基底クラスのフィールドをモック用に設定
        payment.setFacilityId(1L);
        payment.setBorrowerId(1L);
        
        // ReflectionでIDを設定（Transaction基底クラスのprotected setId()を使用）
        try {
            Field idField = payment.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(payment, 1L);
        } catch (Exception e) {
            // テスト環境でのReflection失敗は無視
        }
        
        return payment;
    }
}