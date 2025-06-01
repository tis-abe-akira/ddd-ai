package com.example.syndicatelending.syndicate.service;

import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.syndicate.entity.Syndicate;
import com.example.syndicatelending.syndicate.repository.SyndicateRepository;
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
    private SyndicateService syndicateService;

    @BeforeEach
    void setUp() {
        syndicateService = new SyndicateService(syndicateRepository);
    }

    @Test
    void createSyndicate正常系() {
        Syndicate s = new Syndicate("団A", 1L, List.of(2L, 3L));
        when(syndicateRepository.existsByName("団A")).thenReturn(false);
        when(syndicateRepository.save(any(Syndicate.class))).thenReturn(s);
        Syndicate result = syndicateService.createSyndicate(s);
        assertEquals("団A", result.getName());
        verify(syndicateRepository).save(any(Syndicate.class));
    }

    @Test
    void createSyndicate重複名で例外() {
        Syndicate s = new Syndicate("団A", 1L, List.of(2L));
        when(syndicateRepository.existsByName("団A")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> syndicateService.createSyndicate(s));
        verify(syndicateRepository, never()).save(any());
    }

    @Test
    void getSyndicateById正常系() {
        Syndicate s = new Syndicate("団A", 1L, List.of(2L));
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
}
