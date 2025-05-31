package com.example.syndicatelending.party.repository;

import com.example.syndicatelending.party.entity.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Borrower Spring Data JPA Repository。
 */
@Repository
public interface BorrowerRepository extends JpaRepository<Borrower, Long> {

    List<Borrower> findByNameContainingIgnoreCase(String name);

    List<Borrower> findByCreditRating(String creditRating);
}
