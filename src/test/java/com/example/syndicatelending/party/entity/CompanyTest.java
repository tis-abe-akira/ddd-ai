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
                Industry.IT,
                "123 Main St",
                Country.JAPAN);

        assertEquals("Test Company", company.getCompanyName());
        assertEquals("REG123456", company.getRegistrationNumber());
        assertEquals(Industry.IT, company.getIndustry());
        assertEquals("123 Main St", company.getAddress());
        assertEquals(Country.JAPAN, company.getCountry());
        assertNotNull(company.getCreatedAt());
        assertNotNull(company.getUpdatedAt());
    }

    @Test
    void 企業名を更新すると更新日時が変更される() throws InterruptedException {
        Company company = new Company("Original Name", null, Industry.OTHER, null, Country.OTHER);
        var originalUpdatedAt = company.getUpdatedAt();

        Thread.sleep(10); // 時間差を作る
        company.setCompanyName("Updated Name");

        assertEquals("Updated Name", company.getCompanyName());
        assertTrue(company.getUpdatedAt().isAfter(originalUpdatedAt));
    }
}
