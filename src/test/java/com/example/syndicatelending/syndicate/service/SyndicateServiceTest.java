package com.example.syndicatelending.syndicate.service;

import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.syndicate.entity.Syndicate;
import com.example.syndicatelending.syndicate.repository.SyndicateRepository;
import com.example.syndicatelending.party.repository.InvestorRepository;
import com.example.syndicatelending.party.entity.Investor;
import com.example.syndicatelending.party.entity.InvestorType;
import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyndicateServiceTest {
    @Mock
    private SyndicateRepository syndicateRepository;
    @Mock
    private InvestorRepository investorRepository;
    private SyndicateService syndicateService;

    @BeforeEach
    void setUp() {
        syndicateService = new SyndicateService(syndicateRepository, investorRepository);
    }

    @Test
    void createSyndicate正常系() {
        Syndicate s = new Syndicate("団A", 1L, 1L, List.of(2L, 3L)); // borrowerId: 1L を追加
        when(syndicateRepository.existsByName("団A")).thenReturn(false);
        Investor leadBank = new Investor();
        leadBank.setId(1L);
        leadBank.setInvestorType(InvestorType.LEAD_BANK);
        when(investorRepository.findById(1L)).thenReturn(java.util.Optional.of(leadBank));
        when(syndicateRepository.save(any(Syndicate.class))).thenReturn(s);
        Syndicate result = syndicateService.createSyndicate(s);
        assertEquals("団A", result.getName());
        verify(syndicateRepository).save(any(Syndicate.class));
    }

    @Test
    void createSyndicate重複名で例外() {
        Syndicate s = new Syndicate("団A", 1L, 1L, List.of(2L)); // borrowerId: 1L を追加
        when(syndicateRepository.existsByName("団A")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> syndicateService.createSyndicate(s));
        verify(syndicateRepository, never()).save(any());
    }

    @Test
    void getSyndicateById正常系() {
        Syndicate s = new Syndicate("団A", 1L, 1L, List.of(2L)); // borrowerId: 1L を追加
        s.setId(10L);
        when(syndicateRepository.findById(10L)).thenReturn(Optional.of(s));
        Syndicate result = syndicateService.getSyndicateById(10L);
        assertEquals(10L, result.getId());
    }

    @Test
    void getSyndicateById存在しないと例外() {
        when(syndicateRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> syndicateService.getSyndicateById(99L));
    }

    @Test
    void createSyndicate_リードバンクがLEAD_BANKでないと例外() {
        Syndicate s = new Syndicate("団B", 100L, 100L, List.of(2L, 3L)); // borrowerId: 100L を追加
        when(syndicateRepository.existsByName("団B")).thenReturn(false);
        Investor notLeadBank = new Investor();
        notLeadBank.setId(100L);
        notLeadBank.setInvestorType(InvestorType.BANK); // LEAD_BANK以外
        when(investorRepository.findById(100L)).thenReturn(java.util.Optional.of(notLeadBank));
        assertThrows(BusinessRuleViolationException.class, () -> syndicateService.createSyndicate(s));
        verify(syndicateRepository, never()).save(any());
    }

    @Test
    void createSyndicate_リードバンクが存在しないと例外() {
        Syndicate s = new Syndicate("団C", 200L, 200L, List.of(2L, 3L)); // borrowerId: 200L を追加
        when(syndicateRepository.existsByName("団C")).thenReturn(false);
        when(investorRepository.findById(200L)).thenReturn(java.util.Optional.empty());
        assertThrows(BusinessRuleViolationException.class, () -> syndicateService.createSyndicate(s));
        verify(syndicateRepository, never()).save(any());
    }

    @Test
    void createSyndicate_リードバンクがLEAD_BANKなら正常() {
        Syndicate s = new Syndicate("団D", 300L, 300L, List.of(2L, 3L)); // borrowerId: 300L を追加
        when(syndicateRepository.existsByName("団D")).thenReturn(false);
        Investor leadBank = new Investor();
        leadBank.setId(300L);
        leadBank.setInvestorType(InvestorType.LEAD_BANK);
        when(investorRepository.findById(300L)).thenReturn(java.util.Optional.of(leadBank));
        when(syndicateRepository.save(any(Syndicate.class))).thenReturn(s);
        Syndicate result = syndicateService.createSyndicate(s);
        assertEquals("団D", result.getName());
        verify(syndicateRepository).save(any(Syndicate.class));
    }
}
