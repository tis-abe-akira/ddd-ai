package com.example.syndicatelending.syndicate.controller;

import com.example.syndicatelending.syndicate.entity.Syndicate;
import com.example.syndicatelending.syndicate.service.SyndicateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SyndicateController.class)
class SyndicateControllerTest {
        @Autowired
        private MockMvc mockMvc;
        @MockBean
        private SyndicateService syndicateService;

        private Syndicate sampleSyndicate;

        @BeforeEach
        void setUp() {
                sampleSyndicate = new Syndicate("団A", 1L, 1L, List.of(2L, 3L)); // name, leadBankId, borrowerId,
                                                                                // memberInvestorIds
                sampleSyndicate.setId(1L);
        }

        @Test
        void createSyndicate正常系() throws Exception {
                when(syndicateService.createSyndicate(any())).thenReturn(sampleSyndicate);
                mockMvc.perform(post("/api/v1/syndicates")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{" +
                                                "\"name\":\"団A\"," +
                                                "\"leadBankId\":1," +
                                                "\"borrowerId\":1," +
                                                "\"memberInvestorIds\":[2,3]}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("団A"));
        }

        @Test
        void getSyndicate正常系() throws Exception {
                when(syndicateService.getSyndicateById(1L)).thenReturn(sampleSyndicate);
                mockMvc.perform(get("/api/v1/syndicates/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1L));
        }

        @Test
        void getAllSyndicatesページング() throws Exception {
                Page<Syndicate> page = new PageImpl<>(List.of(sampleSyndicate), PageRequest.of(0, 10), 1);
                when(syndicateService.getAllSyndicates(any(Pageable.class))).thenReturn(page);
                mockMvc.perform(get("/api/v1/syndicates?page=0&size=10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].name").value("団A"));
        }

        // Update Tests
        @Test
        void updateSyndicate正常系() throws Exception {
                Syndicate updatedSyndicate = new Syndicate("団A更新", 1L, 1L, List.of(2L, 3L, 4L));
                updatedSyndicate.setId(1L);
                when(syndicateService.updateSyndicate(any(Long.class), any())).thenReturn(updatedSyndicate);
                mockMvc.perform(put("/api/v1/syndicates/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{" +
                                                "\"name\":\"団A更新\"," +
                                                "\"leadBankId\":1," +
                                                "\"borrowerId\":1," +
                                                "\"memberInvestorIds\":[2,3,4]," +
                                                "\"version\":1}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("団A更新"))
                                .andExpect(jsonPath("$.memberInvestorIds.length()").value(3));
        }

        @Test
        void updateSyndicate存在しないと404() throws Exception {
                when(syndicateService.updateSyndicate(any(Long.class), any()))
                                .thenThrow(new com.example.syndicatelending.common.application.exception.ResourceNotFoundException(
                                                "Syndicate not found"));
                mockMvc.perform(put("/api/v1/syndicates/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{" +
                                                "\"name\":\"団B\"," +
                                                "\"leadBankId\":1," +
                                                "\"borrowerId\":1," +
                                                "\"memberInvestorIds\":[2,3]," +
                                                "\"version\":1}"))
                                .andExpect(status().isNotFound());
        }

        // Delete Tests
        @Test
        void deleteSyndicate正常系() throws Exception {
                mockMvc.perform(delete("/api/v1/syndicates/1"))
                                .andExpect(status().isNoContent());
        }

        @Test
        void deleteSyndicate存在しないと404() throws Exception {
                doThrow(new com.example.syndicatelending.common.application.exception.ResourceNotFoundException(
                                "Syndicate not found"))
                                .when(syndicateService).deleteSyndicate(999L);

                mockMvc.perform(delete("/api/v1/syndicates/999"))
                                .andExpect(status().isNotFound());
        }
}
