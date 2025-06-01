package com.example.syndicatelending.syndicate.service;

import com.example.syndicatelending.syndicate.entity.Syndicate;
import com.example.syndicatelending.syndicate.repository.SyndicateRepository;
import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.party.repository.InvestorRepository;
import com.example.syndicatelending.party.entity.Investor;
import com.example.syndicatelending.party.entity.InvestorType;
import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SyndicateService {
    private final SyndicateRepository syndicateRepository;
    private final InvestorRepository investorRepository;

    public SyndicateService(SyndicateRepository syndicateRepository, InvestorRepository investorRepository) {
        this.syndicateRepository = syndicateRepository;
        this.investorRepository = investorRepository;
    }

    public Syndicate createSyndicate(Syndicate syndicate) {
        if (syndicateRepository.existsByName(syndicate.getName())) {
            throw new IllegalArgumentException("Syndicate name already exists: " + syndicate.getName());
        }
        // --- LEAD_BANK資格チェック追加 ---
        Long leadBankId = syndicate.getLeadBankId();
        Investor leadBank = investorRepository.findById(leadBankId)
                .orElseThrow(() -> new BusinessRuleViolationException("指定されたリードバンクが存在しません: id=" + leadBankId));
        if (leadBank.getInvestorType() != InvestorType.LEAD_BANK) {
            throw new BusinessRuleViolationException("指定されたリードバンクはLEAD_BANKの資格を持っていません: id=" + leadBankId);
        }
        // ---
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
