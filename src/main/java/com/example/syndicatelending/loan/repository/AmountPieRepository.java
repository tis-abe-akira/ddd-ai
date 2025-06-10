package com.example.syndicatelending.loan.repository;

import com.example.syndicatelending.loan.entity.AmountPie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmountPieRepository extends JpaRepository<AmountPie, Long> {
    List<AmountPie> findByDrawdown_Id(Long drawdownId);
    
    List<AmountPie> findByDrawdown_LoanId(Long loanId);

    void deleteByDrawdown_Id(Long drawdownId);
}
