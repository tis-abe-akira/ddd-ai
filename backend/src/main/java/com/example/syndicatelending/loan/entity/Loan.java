package com.example.syndicatelending.loan.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import com.example.syndicatelending.common.domain.model.MoneyAttributeConverter;
import com.example.syndicatelending.common.domain.model.PercentageAttributeConverter;
import com.example.syndicatelending.common.statemachine.loan.LoanState;

/**
 * ローン（貸付）エンティティ。
 * <p>
 * シンジケートローンの各貸付情報を表す集約ルート。
 * 監査フィールド（createdAt, updatedAt）、バージョン（version）を持つ。
 * </p>
 */
@Entity
@Table(name = "loan")
public class Loan {
    /** ローンID（主キー） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ファシリティID（外部キー） */
    @Column(nullable = false)
    private Long facilityId;

    /** 借り手ID（外部キー） */
    @Column(nullable = false)
    private Long borrowerId;

    /** 元本金額 */
    @Column(nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money principalAmount;

    /** 現在の貸付残高 */
    @Column(nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money outstandingBalance;

    /** 年利率（%） */
    @Column(nullable = false, precision = 38, scale = 4)
    @Convert(converter = PercentageAttributeConverter.class)
    private Percentage annualInterestRate;

    /** ドローダウン日（貸付実行日） */
    @Column(nullable = false)
    private LocalDate drawdownDate;

    /** 返済期間（月単位） */
    @Column(nullable = false)
    private Integer repaymentPeriodMonths;

    /** 返済サイクル（例: MONTHLY, QUARTERLY等） */
    @Column(nullable = false)
    private String repaymentCycle;

    /** 返済方法（元利均等、バレット返済等） */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RepaymentMethod repaymentMethod;

    /** 支払い詳細リスト */
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PaymentDetail> paymentDetails = new ArrayList<>();

    /** 通貨コード（例: JPY, USD等） */
    @Column(nullable = false)
    private String currency;

    /** ローン状態 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoanState status = LoanState.DRAFT;

    /** レコード作成日時 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** レコード更新日時 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 楽観的ロック用バージョン番号 */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * エンティティ新規作成時に作成・更新日時を自動設定。
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * エンティティ更新時に更新日時を自動設定。
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 主要プロパティを全て受け取るコンストラクタ。
     * 支払いスケジュールも自動生成する。
     */
    public Loan(Long facilityId, Long borrowerId, Money principalAmount, Percentage annualInterestRate,
            LocalDate drawdownDate, Integer repaymentPeriodMonths, String repaymentCycle,
            RepaymentMethod repaymentMethod, String currency) {
        this.facilityId = facilityId;
        this.borrowerId = borrowerId;
        this.principalAmount = principalAmount;
        this.outstandingBalance = principalAmount; // 初期残高は元本と同じ
        this.annualInterestRate = annualInterestRate;
        this.drawdownDate = drawdownDate;
        this.repaymentPeriodMonths = repaymentPeriodMonths;
        this.repaymentCycle = repaymentCycle;
        this.repaymentMethod = repaymentMethod;
        this.currency = currency;
        // 支払いスケジュール自動生成
        generatePaymentSchedule();
    }

    /**
     * JPA用のデフォルトコンストラクタ（必須）
     */
    protected Loan() {
        // for JPA
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public Money getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(Money principalAmount) {
        this.principalAmount = principalAmount;
    }

    public Money getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(Money outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public Percentage getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(Percentage annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public LocalDate getDrawdownDate() {
        return drawdownDate;
    }

    public void setDrawdownDate(LocalDate drawdownDate) {
        this.drawdownDate = drawdownDate;
    }

    public Integer getRepaymentPeriodMonths() {
        return repaymentPeriodMonths;
    }

    public void setRepaymentPeriodMonths(Integer repaymentPeriodMonths) {
        this.repaymentPeriodMonths = repaymentPeriodMonths;
    }

    public String getRepaymentCycle() {
        return repaymentCycle;
    }

    public void setRepaymentCycle(String repaymentCycle) {
        this.repaymentCycle = repaymentCycle;
    }

    public RepaymentMethod getRepaymentMethod() {
        return repaymentMethod;
    }

    public void setRepaymentMethod(RepaymentMethod repaymentMethod) {
        this.repaymentMethod = repaymentMethod;
    }

    public List<PaymentDetail> getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(List<PaymentDetail> paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LoanState getStatus() {
        return status;
    }

    public void setStatus(LoanState status) {
        this.status = status;
    }

    /**
     * Loanが返済中かどうかを判定する
     * 
     * @return 返済中の場合 true
     */
    public boolean isActive() {
        return LoanState.ACTIVE.equals(this.status);
    }

    /**
     * Loanが遅延中かどうかを判定する
     * 
     * @return 遅延中の場合 true
     */
    public boolean isOverdue() {
        return LoanState.OVERDUE.equals(this.status);
    }

    /**
     * Loanが完済済みかどうかを判定する
     * 
     * @return 完済済みの場合 true
     */
    public boolean isCompleted() {
        return LoanState.COMPLETED.equals(this.status);
    }

    /**
     * 支払いスケジュールを生成します。
     * <p>
     * 返済方法に基づいて適切な支払い詳細を生成し、既存の支払い詳細をクリアしてから新しいものを設定します。
     * </p>
     */
    public void generatePaymentSchedule() {
        // 既存の支払い詳細をクリア
        this.paymentDetails.clear();

        // 返済方法に基づいて支払いスケジュールを生成
        List<PaymentDetail> newPaymentDetails;
        switch (this.repaymentMethod) {
            case EQUAL_INSTALLMENT:
                newPaymentDetails = generateEqualInstallmentSchedule();
                break;
            case BULLET_PAYMENT:
                newPaymentDetails = generateBulletPaymentSchedule();
                break;
            default:
                throw new IllegalStateException("サポートされていない返済方法です: " + this.repaymentMethod);
        }

        // 生成された支払い詳細を設定
        this.paymentDetails.addAll(newPaymentDetails);
    }

    /**
     * 元利均等返済の支払いスケジュールを生成します。
     *
     * @return 支払い詳細のリスト
     */
    private List<PaymentDetail> generateEqualInstallmentSchedule() {
        List<PaymentDetail> details = new ArrayList<>();

        // 月利を計算
        BigDecimal monthlyRate = this.annualInterestRate.getValue().divide(new BigDecimal("12"), 10,
                RoundingMode.HALF_UP);

        // 毎月の支払額を計算（元利均等）
        BigDecimal principalBd = this.principalAmount.getAmount();
        int numberOfPayments = this.repaymentPeriodMonths;

        BigDecimal monthlyPayment = calculateEqualInstallmentPayment(principalBd, monthlyRate, numberOfPayments);

        BigDecimal remainingBalance = principalBd;
        LocalDate paymentDate = this.drawdownDate.plusMonths(1);

        for (int i = 1; i <= numberOfPayments; i++) {
            // 利息部分を計算
            BigDecimal interestPayment = remainingBalance.multiply(monthlyRate).setScale(0, RoundingMode.HALF_UP);

            // 元本部分を計算
            BigDecimal principalPayment = monthlyPayment.subtract(interestPayment);

            // 最終回の調整
            if (i == numberOfPayments) {
                principalPayment = remainingBalance;
                monthlyPayment = principalPayment.add(interestPayment);
            }

            // 残高を更新
            remainingBalance = remainingBalance.subtract(principalPayment);

            // PaymentDetailを作成
            PaymentDetail detail = new PaymentDetail(
                    this,
                    i,
                    Money.of(principalPayment),
                    Money.of(interestPayment),
                    paymentDate,
                    Money.of(remainingBalance));

            details.add(detail);
            paymentDate = paymentDate.plusMonths(1);
        }

        return details;
    }

    /**
     * バレット返済の支払いスケジュールを生成します。
     *
     * @return 支払い詳細のリスト
     */
    private List<PaymentDetail> generateBulletPaymentSchedule() {
        List<PaymentDetail> details = new ArrayList<>();

        // 月利を計算
        BigDecimal monthlyRate = this.annualInterestRate.getValue().divide(new BigDecimal("12"), 10,
                RoundingMode.HALF_UP);
        BigDecimal principalBd = this.principalAmount.getAmount();

        LocalDate paymentDate = this.drawdownDate.plusMonths(1);

        // 利息のみの支払い（最終回を除く）
        for (int i = 1; i < this.repaymentPeriodMonths; i++) {
            BigDecimal interestPayment = principalBd.multiply(monthlyRate).setScale(0, RoundingMode.HALF_UP);

            PaymentDetail detail = new PaymentDetail(
                    this,
                    i,
                    Money.zero(),
                    Money.of(interestPayment),
                    paymentDate,
                    this.principalAmount // 残高は元本と同じ
            );

            details.add(detail);
            paymentDate = paymentDate.plusMonths(1);
        }

        // 最終回：元本 + 利息
        if (this.repaymentPeriodMonths > 0) {
            BigDecimal finalInterestPayment = principalBd.multiply(monthlyRate).setScale(0, RoundingMode.HALF_UP);

            PaymentDetail finalDetail = new PaymentDetail(
                    this,
                    this.repaymentPeriodMonths,
                    this.principalAmount, // 元本全額
                    Money.of(finalInterestPayment),
                    paymentDate,
                    Money.zero() // 最終的な残高は0
            );

            details.add(finalDetail);
        }

        return details;
    }

    /**
     * 元利均等返済の月次支払額を計算します。
     *
     * @param principal        元本金額
     * @param monthlyRate      月利
     * @param numberOfPayments 支払回数
     * @return 月次支払額
     */
    private BigDecimal calculateEqualInstallmentPayment(BigDecimal principal, BigDecimal monthlyRate,
            int numberOfPayments) {
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            // 無利息の場合
            return principal.divide(new BigDecimal(numberOfPayments), 0, RoundingMode.HALF_UP);
        }

        // PMT計算式: P * r * (1 + r)^n / ((1 + r)^n - 1)
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRatePowerN = onePlusRate.pow(numberOfPayments);

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRatePowerN);
        BigDecimal denominator = onePlusRatePowerN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 0, RoundingMode.HALF_UP);
    }
}
