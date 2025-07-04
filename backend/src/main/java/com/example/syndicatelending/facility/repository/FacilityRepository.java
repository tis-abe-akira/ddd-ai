package com.example.syndicatelending.facility.repository;

import com.example.syndicatelending.facility.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
    /**
     * 指定されたSyndicateに関連付けられたFacilityリストを取得
     */
    List<Facility> findBySyndicateId(Long syndicateId);

    /**
     * 指定されたBorrowerがFacilityに参加しているかチェック
     * SyndicateのborrowerIdを通じてFacilityとの関連を確認
     */
    @Query("SELECT COUNT(f) > 0 FROM Facility f " +
           "JOIN Syndicate s ON f.syndicateId = s.id " +
           "WHERE s.borrowerId = :borrowerId")
    boolean existsActiveFacilityForBorrower(@Param("borrowerId") Long borrowerId);

    /**
     * 指定されたInvestorがFacilityに参加しているかチェック
     * SharePieを通じてFacilityとの関連を確認
     */
    @Query("SELECT COUNT(f) > 0 FROM Facility f " +
           "JOIN SharePie sp ON f.id = sp.facility.id " +
           "WHERE sp.investorId = :investorId")
    boolean existsActiveFacilityForInvestor(@Param("investorId") Long investorId);
}
