package com.example.syndicatelending.facility.entity;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class FacilityJsonTest {

    @Test
    void testFacilityJsonSerialization() throws JsonProcessingException {
        // ObjectMapperの設定
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // テストデータ作成
        Facility facility = new Facility(
                1L,
                Money.of(new BigDecimal("1000000")),
                "USD",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "Fixed 5%");

        SharePie sharePie1 = new SharePie();
        sharePie1.setInvestorId(1L);
        sharePie1.setShare(Percentage.of(new BigDecimal("0.6")));
        sharePie1.setFacility(facility);

        SharePie sharePie2 = new SharePie();
        sharePie2.setInvestorId(2L);
        sharePie2.setShare(Percentage.of(new BigDecimal("0.4")));
        sharePie2.setFacility(facility);

        facility.setSharePies(Arrays.asList(sharePie1, sharePie2));

        // JSONシリアライゼーション実行
        String json = objectMapper.writeValueAsString(facility);

        // 結果確認
        assertNotNull(json);
        assertTrue(json.contains("syndicateId"));
        assertTrue(json.contains("sharePies"));
        // SharePieにfacilityフィールドが含まれていないことを確認（無限ループ回避）
        assertFalse(json.contains("\"facility\""));

        System.out.println("Generated JSON:");
        System.out.println(json);
    }

    @Test
    void testSharePieJsonSerialization() throws JsonProcessingException {
        // ObjectMapperの設定
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // テストデータ作成
        Facility facility = new Facility(
                1L,
                Money.of(new BigDecimal("1000000")),
                "USD",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "Fixed 5%");

        SharePie sharePie = new SharePie();
        sharePie.setInvestorId(1L);
        sharePie.setShare(Percentage.of(new BigDecimal("0.6")));
        sharePie.setFacility(facility);

        // JSONシリアライゼーション実行
        String json = objectMapper.writeValueAsString(sharePie);

        // 結果確認
        assertNotNull(json);
        assertTrue(json.contains("investorId"));
        assertTrue(json.contains("share"));
        // facilityフィールドが含まれていないことを確認（@JsonIgnoreが効いている）
        assertFalse(json.contains("facility"));

        System.out.println("SharePie JSON:");
        System.out.println(json);
    }
}
