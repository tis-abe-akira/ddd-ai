package com.example.syndicatelending.loan.repository;

import com.example.syndicatelending.loan.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByFacilityId(Long facilityId);
    List<Loan> findByBorrowerId(Long borrowerId);
    List<Loan> findByFacilityIdAndBorrowerId(Long facilityId, Long borrowerId);
}
