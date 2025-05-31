package com.example.syndicatelending.party.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Company エンティティの単体テスト。
 */
class CompanyTest {

    @Test
    void 企業を正常に作成できる() {
        Company company = new Company(
                "Test Company",
                "REG123456",
                "Technology",
                "123 Main St",
                "Japan"
        );

        assertNotNull(company.getBusinessId());
        assertEquals("Test Company", company.getCompanyName());
        assertEquals("REG123456", company.getRegistrationNumber());
        assertEquals("Technology", company.getIndustry());
        assertEquals("123 Main St", company.getAddress());
        assertEquals("Japan", company.getCountry());
        assertNotNull(company.getCreatedAt());
        assertNotNull(company.getUpdatedAt());
    }

    @Test
    void 企業名を更新すると更新日時が変更される() throws InterruptedException {
        Company company = new Company("Original Name", null, null, null, null);
        var originalUpdatedAt = company.getUpdatedAt();
        
        Thread.sleep(10); // 時間差を作る
        company.setCompanyName("Updated Name");

        assertEquals("Updated Name", company.getCompanyName());
        assertTrue(company.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    void ビジネスIDは自動生成される() {
        Company company1 = new Company("Company 1", null, null, null, null);
        Company company2 = new Company("Company 2", null, null, null, null);

        assertNotNull(company1.getBusinessId());
        assertNotNull(company2.getBusinessId());
        assertNotEquals(company1.getBusinessId(), company2.getBusinessId());
    }
}