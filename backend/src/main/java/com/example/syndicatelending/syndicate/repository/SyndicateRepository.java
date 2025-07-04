package com.example.syndicatelending.syndicate.repository;

import com.example.syndicatelending.syndicate.entity.Syndicate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SyndicateRepository extends JpaRepository<Syndicate, Long> {
    boolean existsByName(String name);
    
    /**
     * 関連エンティティの詳細情報を含むシンジケート一覧を取得
     * Borrower、Lead Bank Investor の名前情報を併せて取得
     */
    @Query("SELECT s FROM Syndicate s")
    List<Syndicate> findAllForDetailResponse();
    
    /**
     * 関連エンティティの詳細情報を含む特定のシンジケートを取得
     * Borrower、Lead Bank Investor の名前情報を併せて取得
     */
    @Query("SELECT s FROM Syndicate s WHERE s.id = :id")
    Optional<Syndicate> findByIdForDetailResponse(Long id);
}
