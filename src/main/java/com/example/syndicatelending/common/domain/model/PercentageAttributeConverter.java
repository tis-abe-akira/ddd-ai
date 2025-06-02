package com.example.syndicatelending.common.domain.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.math.BigDecimal;

/**
 * Percentage型とBigDecimal間の変換を行うJPAコンバータ。
 */
@Converter
public class PercentageAttributeConverter implements AttributeConverter<Percentage, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Percentage percentage) {
        return percentage == null ? null : percentage.getValue();
    }

    @Override
    public Percentage convertToEntityAttribute(BigDecimal dbData) {
        return dbData == null ? null : Percentage.of(dbData);
    }
}
