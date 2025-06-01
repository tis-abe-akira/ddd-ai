package com.example.syndicatelending.syndicate.service;

import com.example.syndicatelending.syndicate.entity.Syndicate;
import com.example.syndicatelending.syndicate.repository.SyndicateRepository;
import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SyndicateService {
    private final SyndicateRepository syndicateRepository;

    public SyndicateService(SyndicateRepository syndicateRepository) {
        this.syndicateRepository = syndicateRepository;
    }

    public Syndicate createSyndicate(Syndicate syndicate) {
        if (syndicateRepository.existsByName(syndicate.getName())) {
            throw new IllegalArgumentException("Syndicate name already exists: " + syndicate.getName());
        }
        return syndicateRepository.save(syndicate);
    }

    public Syndicate getSyndicateById(Long id) {
        return syndicateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syndicate not found with ID: " + id));
    }

    public Page<Syndicate> getAllSyndicates(Pageable pageable) {
        return syndicateRepository.findAll(pageable);
    }
}
