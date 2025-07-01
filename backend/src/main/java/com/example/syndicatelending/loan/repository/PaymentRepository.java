package com.example.syndicatelending.loan.repository;

import com.example.syndicatelending.loan.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByLoanId(Long loanId);
    List<Payment> findByLoanIdOrderByPaymentDateDesc(Long loanId);
}