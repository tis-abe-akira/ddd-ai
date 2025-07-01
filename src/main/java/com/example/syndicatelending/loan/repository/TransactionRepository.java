package com.example.syndicatelending.loan.repository;

import com.example.syndicatelending.transaction.entity.Transaction;
import com.example.syndicatelending.transaction.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFacilityId(Long facilityId);
    List<Transaction> findByBorrowerId(Long borrowerId);
    List<Transaction> findByTransactionType(TransactionType transactionType);
}
