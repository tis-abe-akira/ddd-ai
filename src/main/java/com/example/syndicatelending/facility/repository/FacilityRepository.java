package com.example.syndicatelending.facility.repository;

import com.example.syndicatelending.facility.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
    // 追加のクエリが必要ならここに定義
}
