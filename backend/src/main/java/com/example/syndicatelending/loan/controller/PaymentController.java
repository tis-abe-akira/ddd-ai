package com.example.syndicatelending.loan.controller;

import com.example.syndicatelending.loan.dto.CreatePaymentRequest;
import com.example.syndicatelending.loan.entity.Payment;
import com.example.syndicatelending.loan.service.PaymentService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        Payment payment = paymentService.processPayment(request);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<Payment>> getPaymentsByLoanId(@PathVariable Long loanId) {
        List<Payment> payments = paymentService.getPaymentsByLoanId(loanId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/scheduled/{paymentDetailId}")
    public ResponseEntity<Payment> processScheduledPayment(@PathVariable Long paymentDetailId) {
        Payment payment = paymentService.processScheduledPayment(paymentDetailId);
        return ResponseEntity.ok(payment);
    }
}