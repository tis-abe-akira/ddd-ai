package com.example.syndicatelending.party.controller;

import com.example.syndicatelending.party.dto.*;
import com.example.syndicatelending.party.entity.Industry;
import com.example.syndicatelending.party.entity.Country;
import com.example.syndicatelending.party.entity.CreditRating;
import com.example.syndicatelending.party.entity.InvestorType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PartyController 統合テスト。
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PartyControllerTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        @Autowired
        private ObjectMapper objectMapper;

        private MockMvc mockMvc;

        void setUp() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }

        @Test
        void 企業を正常に作成できる() throws Exception {
                setUp();
                CreateCompanyRequest request = new CreateCompanyRequest(
                                "Test Company", "REG123456", Industry.IT, "123 Main St", Country.JAPAN);

                mockMvc.perform(post("/api/v1/parties/companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.companyName").value("Test Company"))
                                .andExpect(jsonPath("$.registrationNumber").value("REG123456"))
                                .andExpect(jsonPath("$.industry").value("IT"))
                                .andExpect(jsonPath("$.address").value("123 Main St"))
                                .andExpect(jsonPath("$.country").value("JAPAN"))
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.createdAt").exists())
                                .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        void 借り手を正常に作成できる() throws Exception {
                setUp();
                CreateBorrowerRequest request = new CreateBorrowerRequest(
                                "Test Borrower", "borrower@example.com", "123-456-7890",
                                null, BigDecimal.valueOf(1000000), CreditRating.AA);

                mockMvc.perform(post("/api/v1/parties/borrowers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name").value("Test Borrower"))
                                .andExpect(jsonPath("$.email").value("borrower@example.com"))
                                .andExpect(jsonPath("$.phoneNumber").value("123-456-7890"))
                                .andExpect(jsonPath("$.creditLimit").value(1000000))
                                .andExpect(jsonPath("$.creditRating").value("AA"))
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.createdAt").exists())
                                .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        void 投資家を正常に作成できる() throws Exception {
                setUp();
                CreateInvestorRequest request = new CreateInvestorRequest(
                                "Test Investor", "investor@example.com", "987-654-3210",
                                null, BigDecimal.valueOf(5000000), InvestorType.BANK);

                mockMvc.perform(post("/api/v1/parties/investors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name").value("Test Investor"))
                                .andExpect(jsonPath("$.email").value("investor@example.com"))
                                .andExpect(jsonPath("$.phoneNumber").value("987-654-3210"))
                                .andExpect(jsonPath("$.investmentCapacity").value(5000000))
                                .andExpect(jsonPath("$.investorType").value("BANK"))
                                .andExpect(jsonPath("$.isActive").value(true))
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.createdAt").exists())
                                .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        void バリデーションエラーで400が返る() throws Exception {
                setUp();
                CreateCompanyRequest request = new CreateCompanyRequest("", null, null, null, null); // 空の企業名

                mockMvc.perform(post("/api/v1/parties/companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        void 存在しない企業を取得すると404が返る() throws Exception {
                setUp();
                mockMvc.perform(get("/api/v1/parties/companies/999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        void 全ての企業リストを取得できる() throws Exception {
                setUp();
                mockMvc.perform(get("/api/v1/parties/companies"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void 全ての借り手リストを取得できる() throws Exception {
                setUp();
                mockMvc.perform(get("/api/v1/parties/borrowers"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void 全ての投資家リストを取得できる() throws Exception {
                setUp();
                mockMvc.perform(get("/api/v1/parties/investors"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void アクティブな投資家リストを取得できる() throws Exception {
                setUp();
                mockMvc.perform(get("/api/v1/parties/investors/active"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void 存在しない借り手を取得すると404が返る() throws Exception {
                setUp();
                mockMvc.perform(get("/api/v1/parties/borrowers/999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        void 存在しない投資家を取得すると404が返る() throws Exception {
                setUp();
                mockMvc.perform(get("/api/v1/parties/investors/999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }
}
