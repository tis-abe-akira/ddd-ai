package com.example.syndicatelending.syndicate.service;

import com.example.syndicatelending.syndicate.entity.Syndicate;
import com.example.syndicatelending.syndicate.repository.SyndicateRepository;
import com.example.syndicatelending.syndicate.dto.UpdateSyndicateRequest;
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

    /**
     * このメソッドは非推奨にする。（理由は更新時の楽観的排他制御を行っていないため）
     * Syndicateの更新メソッド。
     * 
     * @deprecated
     *             このメソッドは楽観的排他制御を行っていないため、使用しないでください。
     *             代わりに、updateSyndicate(Long id, UpdateSyndicateRequest
     *             request)を使用してください。
     * @param id
     * @param updatedSyndicate
     * @return
     */
    public Syndicate updateSyndicate(Long id, Syndicate updatedSyndicate) {
        Syndicate existingSyndicate = getSyndicateById(id); // ResourceNotFoundExceptionをスロー

        // 名前変更時の重複チェック
        if (!existingSyndicate.getName().equals(updatedSyndicate.getName()) &&
                syndicateRepository.existsByName(updatedSyndicate.getName())) {
            throw new IllegalArgumentException("Syndicate name already exists: " + updatedSyndicate.getName());
        }

        // LEAD_BANK資格チェック
        Long leadBankId = updatedSyndicate.getLeadBankId();
        Investor leadBank = investorRepository.findById(leadBankId)
                .orElseThrow(() -> new BusinessRuleViolationException("指定されたリードバンクが存在しません: id=" + leadBankId));
        if (leadBank.getInvestorType() != InvestorType.LEAD_BANK) {
            throw new BusinessRuleViolationException("指定されたリードバンクはLEAD_BANKの資格を持っていません: id=" + leadBankId);
        }

        existingSyndicate.setName(updatedSyndicate.getName());
        existingSyndicate.setLeadBankId(updatedSyndicate.getLeadBankId());
        existingSyndicate.setBorrowerId(updatedSyndicate.getBorrowerId());
        existingSyndicate.setMemberInvestorIds(updatedSyndicate.getMemberInvestorIds());

        return syndicateRepository.save(existingSyndicate);
    }

    public Syndicate updateSyndicate(Long id, UpdateSyndicateRequest request) {
        Syndicate existingSyndicate = getSyndicateById(id);

        // LEAD_BANK資格チェック
        Long leadBankId = request.getLeadBankId();
        Investor leadBank = investorRepository.findById(leadBankId)
                .orElseThrow(() -> new BusinessRuleViolationException("指定されたリードバンクが存在しません: id=" + leadBankId));
        if (leadBank.getInvestorType() != InvestorType.LEAD_BANK) {
            throw new BusinessRuleViolationException("指定されたリードバンクはLEAD_BANKの資格を持っていません: id=" + leadBankId);
        }

        // 名前変更時の重複チェック
        if (!existingSyndicate.getName().equals(request.getName()) &&
                syndicateRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Syndicate name already exists: " + request.getName());
        }

        Syndicate entityToSave = new Syndicate();

        entityToSave.setId(id);
        entityToSave.setVersion(request.getVersion());

        entityToSave.setName(request.getName());
        entityToSave.setLeadBankId(request.getLeadBankId());
        entityToSave.setBorrowerId(request.getBorrowerId());
        entityToSave.setMemberInvestorIds(request.getMemberInvestorIds());
        entityToSave.setCreatedAt(existingSyndicate.getCreatedAt());

        return syndicateRepository.save(entityToSave);
    }

    public void deleteSyndicate(Long id) {
        if (!syndicateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Syndicate not found with ID: " + id);
        }
        syndicateRepository.deleteById(id);
    }
}
