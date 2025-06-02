package com.example.syndicatelending.common.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.math.BigDecimal;
import java.math.RoundingMode; // Use standard RoundingMode
import java.util.Objects;

/**
 * 金額を表すValue Object。金融計算のためBigDecimalを使用。
 * Immutable Class.
 */
public final class Money {

    static final int DEFAULT_SCALE = 2; // 通常の金額で使うスケール（例: 円、ドルセント）
    static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP; // デフォルトの丸めモード

    private final BigDecimal amount;
    // private final Currency currency; // 通貨も考慮する場合は追加

    private Money(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        // コンストラクタでスケールと丸めを強制
        this.amount = amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 指定されたBigDecimalからMoneyインスタンスを生成するファクトリメソッド。
     */
    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    /**
     * JSONからMoneyインスタンスを生成するファクトリメソッド。
     */
    @JsonCreator
    public static Money fromJson(BigDecimal amount) {
        return new Money(amount);
    }

    /**
     * 指定されたlong値からMoneyインスタンスを生成するファクトリメソッド。(整数金額用)
     */
    public static Money of(long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    /**
     * ゼロ金額のMoneyインスタンスを取得する。
     */
    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    /**
     * 金額のBigDecimal値を取得する。
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * JSON用の値を取得する。
     */
    @JsonValue
    public BigDecimal toJson() {
        return amount;
    }

    /**
     * 加算。
     */
    public Money add(Money other) {
        Objects.requireNonNull(other, "Cannot add null Money");
        return new Money(this.amount.add(other.amount));
    }

    /**
     * 減算。
     */
    public Money subtract(Money other) {
        Objects.requireNonNull(other, "Cannot subtract null Money");
        return new Money(this.amount.subtract(other.amount));
    }

    /**
     * 乗算。
     */
    public Money multiply(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "Cannot multiply by null BigDecimal");
        // 乗算結果のスケールはBigDecimalのデフォルトに任せるか、ここで強制するか検討
        return new Money(this.amount.multiply(multiplier));
    }

    /**
     * 比較：現在の金額が他の金額より大きいか。
     */
    public boolean isGreaterThan(Money other) {
        Objects.requireNonNull(other, "Cannot compare with null Money");
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * 比較：現在の金額が他の金額以上か。
     */
    public boolean isGreaterThanOrEqual(Money other) {
        Objects.requireNonNull(other, "Cannot compare with null Money");
        return this.amount.compareTo(other.amount) >= 0;
    }

    /**
     * 比較：現在の金額が他の金額より小さいか。
     */
    public boolean isLessThan(Money other) {
        Objects.requireNonNull(other, "Cannot compare with null Money");
        return this.amount.compareTo(other.amount) < 0;
    }

    /**
     * 現在の金額がゼロまたは正か。
     */
    public boolean isPositiveOrZero() {
        return this.amount.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * 現在の金額がゼロか。
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    // equals, hashCode, toString (BigDecimal based) - Omitted for brevity

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Money money = (Money) o;
        // BigDecimalのequalsはスケールも比較するので注意。
        // 金額としての等価性を比較する場合は compareTo == 0 を使うことが多いが、
        // Value Objectとしてはequalsもスケール込みとするのが一般的。
        // または、equalsForAmount(Money other) のようなメソッドを別途用意する。
        // ここでは標準的なequals実装を採用。
        return Objects.equals(amount, money.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return amount.toPlainString(); // 指数表記を避ける
    }
}
