package com.example.syndicatelending.syndicate.repository;

import com.example.syndicatelending.syndicate.entity.Syndicate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyndicateRepository extends JpaRepository<Syndicate, Long> {
    boolean existsByName(String name);
}
