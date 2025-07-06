package com.example.syndicatelending.loan.entity;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Loanエンティティの支払いスケジュール生成機能のテスト。
 */
class LoanPaymentScheduleTest {

    @Test
    void 元利均等返済のスケジュール生成ができること() {
        // Given: 元利均等返済のローン
        Loan loan = new Loan(
                1L, // facilityId
                1L, // borrowerId
                Money.of(new BigDecimal("1000000")), // 100万円
                Percentage.of(new BigDecimal("0.05")), // 年利5%
                LocalDate.of(2024, 1, 1), // drawdownDate
                12, // 12ヶ月
                RepaymentCycle.MONTHLY, // repaymentCycle
                RepaymentMethod.EQUAL_INSTALLMENT,
                "JPY"
        );

        // When: 支払いスケジュールを生成
        loan.generatePaymentSchedule();

        // Then: 適切なスケジュールが生成される
        List<PaymentDetail> paymentDetails = loan.getPaymentDetails();

        assertEquals(12, paymentDetails.size(), "12回の支払いが生成されること");

        // 最初の支払い詳細を確認
        PaymentDetail firstPayment = paymentDetails.get(0);
        assertEquals(1, firstPayment.getPaymentNumber(), "支払い番号が1であること");
        assertEquals(LocalDate.of(2024, 2, 1), firstPayment.getDueDate(), "期日が正しいこと");
        assertTrue(firstPayment.getPrincipalPayment().isGreaterThan(Money.zero()), "元本支払いが正の値であること");
        assertTrue(firstPayment.getInterestPayment().isGreaterThan(Money.zero()), "利息支払いが正の値であること");

        // 最後の支払い詳細を確認
        PaymentDetail lastPayment = paymentDetails.get(11);
        assertEquals(12, lastPayment.getPaymentNumber(), "最後の支払い番号が12であること");
        assertEquals(LocalDate.of(2025, 1, 1), lastPayment.getDueDate(), "最後の期日が正しいこと");
        assertTrue(lastPayment.getRemainingBalance().isZero(), "最終残高がゼロであること");

        // 総支払額の検証
        Money totalPrincipal = paymentDetails.stream()
                .map(PaymentDetail::getPrincipalPayment)
                .reduce(Money.zero(), Money::add);
        assertEquals(loan.getPrincipalAmount(), totalPrincipal, "元本総額が一致すること");
    }

    @Test
    void バレット返済のスケジュール生成ができること() {
        // Given: バレット返済のローン
        Loan loan = new Loan(
                1L, 1L,
                Money.of(new BigDecimal("1000000")),
                Percentage.of(new BigDecimal("0.05")),
                LocalDate.of(2024, 1, 1),
                6,
                RepaymentCycle.MONTHLY,
                RepaymentMethod.BULLET_PAYMENT,
                "JPY"
        );

        // When: 支払いスケジュールを生成
        loan.generatePaymentSchedule();

        // Then: 適切なスケジュールが生成される
        List<PaymentDetail> paymentDetails = loan.getPaymentDetails();

        assertEquals(6, paymentDetails.size(), "6回の支払いが生成されること");

        // 最初の5回は利息のみ
        for (int i = 0; i < 5; i++) {
            PaymentDetail payment = paymentDetails.get(i);
            assertTrue(payment.getPrincipalPayment().isZero(), "元本支払いがゼロであること");
            assertTrue(payment.getInterestPayment().isGreaterThan(Money.zero()), "利息支払いが正の値であること");
            assertEquals(loan.getPrincipalAmount(), payment.getRemainingBalance(), "残高が元本と同じであること");
        }

        // 最後の支払いは元本 + 利息
        PaymentDetail lastPayment = paymentDetails.get(5);
        assertEquals(loan.getPrincipalAmount(), lastPayment.getPrincipalPayment(), "最後に元本全額を支払うこと");
        assertTrue(lastPayment.getInterestPayment().isGreaterThan(Money.zero()), "利息支払いが正の値であること");
        assertTrue(lastPayment.getRemainingBalance().isZero(), "最終残高がゼロであること");
    }

    @Test
    void 無利息ローンでも正しく計算されること() {
        // Given: 無利息のローン
        Loan loan = new Loan(
                1L, 1L,
                Money.of(new BigDecimal("1200000")),
                Percentage.of(BigDecimal.ZERO),
                LocalDate.of(2024, 1, 1),
                12,
                RepaymentCycle.MONTHLY,
                RepaymentMethod.EQUAL_INSTALLMENT,
                "JPY"
        );

        // When: 支払いスケジュールを生成
        loan.generatePaymentSchedule();

        // Then: 適切なスケジュールが生成される
        List<PaymentDetail> paymentDetails = loan.getPaymentDetails();

        assertEquals(12, paymentDetails.size(), "12回の支払いが生成されること");

        // すべての支払いで利息がゼロであること
        for (PaymentDetail payment : paymentDetails) {
            assertTrue(payment.getInterestPayment().isZero(), "利息支払いがゼロであること");
            assertEquals(Money.of(new BigDecimal("100000")), payment.getPrincipalPayment(), "元本支払いが10万円ずつであること");
        }
    }

    @Test
    void スケジュール再生成で既存の明細がクリアされること() {
        // Given: 既にスケジュールが生成されているローン
        Loan loan = new Loan(
                1L, 1L,
                Money.of(new BigDecimal("1000000")),
                Percentage.of(new BigDecimal("0.05")),
                LocalDate.of(2024, 1, 1),
                6,
                RepaymentCycle.MONTHLY,
                RepaymentMethod.EQUAL_INSTALLMENT,
                "JPY"
        );
        assertEquals(6, loan.getPaymentDetails().size(), "最初に6回の支払いが生成されること");

        // When: 返済期間を変更して再生成
        loan.setRepaymentPeriodMonths(12);
        loan.generatePaymentSchedule();

        // Then: 新しいスケジュールが生成される
        assertEquals(12, loan.getPaymentDetails().size(), "12回の支払いに更新されること");
    }
}
