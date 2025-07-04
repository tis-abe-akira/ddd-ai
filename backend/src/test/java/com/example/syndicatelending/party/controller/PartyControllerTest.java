package com.example.syndicatelending.party.controller;

import com.example.syndicatelending.common.domain.model.Money;
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
                                null, Money.of(BigDecimal.valueOf(1000000)), CreditRating.AA);

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

        // Update Tests
        @Test
        void 企業を正常に更新できる() throws Exception {
                setUp();
                // まず企業を作成
                CreateCompanyRequest createRequest = new CreateCompanyRequest(
                                "Original Company", "REG123456", Industry.IT, "123 Main St", Country.JAPAN);
                String createResponse = mockMvc.perform(post("/api/v1/parties/companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                Long companyId = objectMapper.readTree(createResponse).get("id").asLong();
                Long version = objectMapper.readTree(createResponse).get("version").asLong();
                // 企業を更新（UpdateCompanyRequestを利用）
                UpdateCompanyRequest updateRequest = new UpdateCompanyRequest(
                                "Updated Company", "REG123456", Industry.FINANCE, "456 Updated St", Country.JAPAN,
                                version);
                mockMvc.perform(put("/api/v1/parties/companies/" + companyId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.companyName").value("Updated Company"))
                                .andExpect(jsonPath("$.industry").value("FINANCE"))
                                .andExpect(jsonPath("$.address").value("456 Updated St"));
        }

        @Test
        void 存在しない企業を更新すると404が返る() throws Exception {
                setUp();
                UpdateCompanyRequest updateRequest = new UpdateCompanyRequest(
                                "Updated Company", "REG123456", Industry.FINANCE, "456 Updated St", Country.JAPAN, 1L);
                mockMvc.perform(put("/api/v1/parties/companies/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        void 借り手を正常に更新できる() throws Exception {
                setUp();
                // まず借り手を作成
                CreateBorrowerRequest createRequest = new CreateBorrowerRequest(
                                "Original Borrower", "original@example.com", "123-456-7890",
                                null, Money.of(BigDecimal.valueOf(1000000)), CreditRating.AA);
                String createResponse = mockMvc.perform(post("/api/v1/parties/borrowers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                Long borrowerId = objectMapper.readTree(createResponse).get("id").asLong();
                Long version = objectMapper.readTree(createResponse).get("version").asLong();
                // 借り手を更新（UpdateBorrowerRequestを利用）
                UpdateBorrowerRequest updateRequest = new UpdateBorrowerRequest(
                                "Updated Borrower", "updated@example.com", "987-654-3210",
                                null, Money.of(BigDecimal.valueOf(2000000)), CreditRating.A, version);
                mockMvc.perform(put("/api/v1/parties/borrowers/" + borrowerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Updated Borrower"))
                                .andExpect(jsonPath("$.email").value("updated@example.com"))
                                .andExpect(jsonPath("$.creditLimit").value(2000000))
                                .andExpect(jsonPath("$.creditRating").value("A"));
        }

        @Test
        void 存在しない借り手を更新すると404が返る() throws Exception {
                setUp();
                UpdateBorrowerRequest updateRequest = new UpdateBorrowerRequest(
                                "Updated Borrower", "updated@example.com", "987-654-3210",
                                null, Money.of(BigDecimal.valueOf(2000000)), CreditRating.A, 1L);
                mockMvc.perform(put("/api/v1/parties/borrowers/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        void 投資家を正常に更新できる() throws Exception {
                setUp();
                // まず投資家を作成
                CreateInvestorRequest createRequest = new CreateInvestorRequest(
                                "Original Investor", "original@example.com", "123-456-7890",
                                null, BigDecimal.valueOf(5000000), InvestorType.BANK);
                String createResponse = mockMvc.perform(post("/api/v1/parties/investors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                Long investorId = objectMapper.readTree(createResponse).get("id").asLong();
                Long version = objectMapper.readTree(createResponse).get("version").asLong();
                // 投資家を更新（UpdateInvestorRequestを利用）
                UpdateInvestorRequest updateRequest = new UpdateInvestorRequest(
                                "Updated Investor", "updated@example.com", "987-654-3210",
                                null, BigDecimal.valueOf(10000000), InvestorType.INSURANCE, version);
                mockMvc.perform(put("/api/v1/parties/investors/" + investorId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Updated Investor"))
                                .andExpect(jsonPath("$.email").value("updated@example.com"))
                                .andExpect(jsonPath("$.investmentCapacity").value(10000000))
                                .andExpect(jsonPath("$.investorType").value("INSURANCE"));
        }

        @Test
        void 存在しない投資家を更新すると404が返る() throws Exception {
                setUp();
                UpdateInvestorRequest updateRequest = new UpdateInvestorRequest(
                                "Updated Investor", "updated@example.com", "987-654-3210",
                                null, BigDecimal.valueOf(10000000), InvestorType.INSURANCE, 1L);
                mockMvc.perform(put("/api/v1/parties/investors/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }

        // Delete Tests
        @Test
        void 企業を正常に削除できる() throws Exception {
                setUp();
                // まず企業を作成
                CreateCompanyRequest createRequest = new CreateCompanyRequest(
                                "Test Company", "REG123456", Industry.IT, "123 Main St", Country.JAPAN);

                String createResponse = mockMvc.perform(post("/api/v1/parties/companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                Long companyId = objectMapper.readTree(createResponse).get("id").asLong();

                // 企業を削除
                mockMvc.perform(delete("/api/v1/parties/companies/" + companyId))
                                .andExpect(status().isNoContent());

                // 削除後、取得すると404になることを確認
                mockMvc.perform(get("/api/v1/parties/companies/" + companyId))
                                .andExpect(status().isNotFound());
        }

        @Test
        void 存在しない企業を削除すると404が返る() throws Exception {
                setUp();
                mockMvc.perform(delete("/api/v1/parties/companies/999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        void 借り手を正常に削除できる() throws Exception {
                setUp();
                // まず借り手を作成
                CreateBorrowerRequest createRequest = new CreateBorrowerRequest(
                                "Test Borrower", "test@example.com", "123-456-7890",
                                null, Money.of(BigDecimal.valueOf(1000000)), CreditRating.AA);

                String createResponse = mockMvc.perform(post("/api/v1/parties/borrowers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                Long borrowerId = objectMapper.readTree(createResponse).get("id").asLong();

                // 借り手を削除
                mockMvc.perform(delete("/api/v1/parties/borrowers/" + borrowerId))
                                .andExpect(status().isNoContent());

                // 削除後、取得すると404になることを確認
                mockMvc.perform(get("/api/v1/parties/borrowers/" + borrowerId))
                                .andExpect(status().isNotFound());
        }

        @Test
        void 存在しない借り手を削除すると404が返る() throws Exception {
                setUp();
                mockMvc.perform(delete("/api/v1/parties/borrowers/999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        void 投資家を正常に削除できる() throws Exception {
                setUp();
                // まず投資家を作成
                CreateInvestorRequest createRequest = new CreateInvestorRequest(
                                "Test Investor", "test@example.com", "123-456-7890",
                                null, BigDecimal.valueOf(5000000), InvestorType.BANK);

                String createResponse = mockMvc.perform(post("/api/v1/parties/investors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                Long investorId = objectMapper.readTree(createResponse).get("id").asLong();

                // 投資家を削除
                mockMvc.perform(delete("/api/v1/parties/investors/" + investorId))
                                .andExpect(status().isNoContent());

                // 削除後、取得すると404になることを確認
                mockMvc.perform(get("/api/v1/parties/investors/" + investorId))
                                .andExpect(status().isNotFound());
        }

        @Test
        void 存在しない投資家を削除すると404が返る() throws Exception {
                setUp();
                mockMvc.perform(delete("/api/v1/parties/investors/999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }
}
