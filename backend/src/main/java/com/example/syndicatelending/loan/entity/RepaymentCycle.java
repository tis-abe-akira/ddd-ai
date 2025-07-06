package com.example.syndicatelending.loan.entity;

/**
 * 返済サイクルを表すEnum
 */
public enum RepaymentCycle {
    MONTHLY(1, "Monthly"),
    QUARTERLY(3, "Quarterly"), 
    SEMI_ANNUALLY(6, "Semi-Annually"),
    ANNUALLY(12, "Annually");

    private final int months;
    private final String displayName;

    RepaymentCycle(int months, String displayName) {
        this.months = months;
        this.displayName = displayName;
    }

    /**
     * 返済サイクルの月数を取得
     * @return 月数
     */
    public int getMonths() {
        return months;
    }

    /**
     * 表示名を取得
     * @return 表示名
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 年間の返済回数を取得
     * @return 年間返済回数
     */
    public int getPaymentsPerYear() {
        return 12 / months;
    }

    /**
     * 指定された返済期間での総返済回数を計算
     * @param repaymentPeriodMonths 返済期間（月数）
     * @return 総返済回数
     */
    public int getTotalPayments(int repaymentPeriodMonths) {
        return (int) Math.ceil((double) repaymentPeriodMonths / months);
    }

    /**
     * 期間利率を計算（年利からサイクル利率への変換）
     * @param annualRate 年利
     * @return サイクル利率
     */
    public double getCycleRate(double annualRate) {
        return annualRate * months / 12.0;
    }
}