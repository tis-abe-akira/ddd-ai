package com.example.syndicatelending.common.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 割合（パーセンテージ）を表すValue Object。
 * 内部的には0から1の間の値（例: 0.10 = 10%）として保持。
 * Immutable Class.
 */
public final class Percentage {

    private static final int DEFAULT_SCALE = 4; // 割合で使うスケール（例: 0.1234）
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    private final BigDecimal value; // 0 から 1.0 の間の値

    private Percentage(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Percentage value cannot be null");
        }
        // 0以上1以下のバリデーション（必要に応じて）
        // if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) >
        // 0) {
        // throw new IllegalArgumentException("Percentage value must be between 0 and
        // 1");
        // }
        // コンストラクタでスケールと丸めを強制
        this.value = value.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 0から1の間のBigDecimal値からPercentageインスタンスを生成する。
     * 例: Percentage.of(BigDecimal.valueOf(0.10)) // 10%
     */
    public static Percentage of(BigDecimal value) {
        return new Percentage(value);
    }

    /**
     * 0から100の間のint値からPercentageインスタンスを生成する。
     * 例: Percentage.of(10) // 10%
     */
    public static Percentage of(int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Percentage integer value must be between 0 and 100");
        }
        return new Percentage(
                BigDecimal.valueOf(value).divide(BigDecimal.valueOf(100), DEFAULT_SCALE, DEFAULT_ROUNDING_MODE));
    }

    /**
     * パーセンテージのBigDecimal値（0〜1）を取得する。
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * この割合を金額に適用し、計算結果の金額を取得する。
     * 例: percentageOf(Money.of(1000)) // 10% of 1000
     */
    public Money applyTo(Money money) {
        Objects.requireNonNull(money, "Cannot apply percentage to null Money");
        // 計算結果のスケールはMoneyのデフォルトスケールに合わせる
        BigDecimal resultAmount = money.getAmount().multiply(this.value).setScale(Money.DEFAULT_SCALE,
                Money.DEFAULT_ROUNDING_MODE);
        return Money.of(resultAmount);
    }

    /**
     * このパーセンテージと他のパーセンテージを加算した新しいPercentageを返す。
     */
    public Percentage add(Percentage other) {
        Objects.requireNonNull(other, "other Percentage must not be null");
        return new Percentage(this.value.add(other.value));
    }

    // equals, hashCode, toString (BigDecimal based) - Omitted for brevity

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Percentage that = (Percentage) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toPlainString() + " (ratio)"; // または value.multiply(BigDecimal.valueOf(100)).toPlainString() + " %"
    }
}
