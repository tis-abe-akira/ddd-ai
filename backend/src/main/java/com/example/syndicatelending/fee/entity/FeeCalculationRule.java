package com.example.syndicatelending.fee.entity;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/**
 * 手数料計算ルール
 * 
 * 各手数料タイプの標準的な計算方法と料率を管理する。
 * 将来的にはDBテーブルに移行する予定だが、現在はEnum定義で管理。
 */
public class FeeCalculationRule {
    
    /**
     * 手数料タイプ別のデフォルト料率（パーセント）
     */
    private static final Map<FeeType, BigDecimal> DEFAULT_RATES = new EnumMap<>(FeeType.class);
    
    /**
     * 手数料タイプ別の受取人タイプ
     */
    private static final Map<FeeType, RecipientType> RECIPIENT_TYPES = new EnumMap<>(FeeType.class);
    
    static {
        // デフォルト料率の設定
        DEFAULT_RATES.put(FeeType.MANAGEMENT_FEE, new BigDecimal("1.5")); // 1.5%
        DEFAULT_RATES.put(FeeType.ARRANGEMENT_FEE, new BigDecimal("2.0")); // 2.0%
        DEFAULT_RATES.put(FeeType.COMMITMENT_FEE, new BigDecimal("0.5")); // 0.5%
        DEFAULT_RATES.put(FeeType.TRANSACTION_FEE, new BigDecimal("0.1")); // 0.1%
        DEFAULT_RATES.put(FeeType.LATE_FEE, new BigDecimal("5.0")); // 5.0%
        DEFAULT_RATES.put(FeeType.AGENT_FEE, new BigDecimal("1.0")); // 1.0%
        DEFAULT_RATES.put(FeeType.OTHER_FEE, new BigDecimal("0.0")); // 0.0% (個別設定)
        
        // 受取人タイプの設定
        RECIPIENT_TYPES.put(FeeType.MANAGEMENT_FEE, RecipientType.LEAD_BANK);
        RECIPIENT_TYPES.put(FeeType.ARRANGEMENT_FEE, RecipientType.LEAD_BANK);
        RECIPIENT_TYPES.put(FeeType.COMMITMENT_FEE, RecipientType.AUTO_DISTRIBUTE);
        RECIPIENT_TYPES.put(FeeType.TRANSACTION_FEE, RecipientType.AGENT_BANK);
        RECIPIENT_TYPES.put(FeeType.LATE_FEE, RecipientType.AUTO_DISTRIBUTE);
        RECIPIENT_TYPES.put(FeeType.AGENT_FEE, RecipientType.AGENT_BANK);
        RECIPIENT_TYPES.put(FeeType.OTHER_FEE, RecipientType.INVESTOR); // デフォルト、変更可能
    }
    
    /**
     * 指定された手数料タイプのデフォルト料率を取得
     * 
     * @param feeType 手数料タイプ
     * @return デフォルト料率（パーセント）
     */
    public static BigDecimal getDefaultRate(FeeType feeType) {
        return DEFAULT_RATES.getOrDefault(feeType, BigDecimal.ZERO);
    }
    
    /**
     * 指定された手数料タイプの受取人タイプを取得
     * 
     * @param feeType 手数料タイプ
     * @return 受取人タイプ
     */
    public static RecipientType getRecipientType(FeeType feeType) {
        return RECIPIENT_TYPES.get(feeType);
    }
    
    /**
     * 手数料を自動計算
     * 
     * @param calculationBase 計算基準額
     * @param feeRate 手数料率（パーセント）
     * @return 計算された手数料額
     */
    public static BigDecimal calculateFeeAmount(BigDecimal calculationBase, BigDecimal feeRate) {
        if (calculationBase == null || feeRate == null) {
            return BigDecimal.ZERO;
        }
        return calculationBase.multiply(feeRate.divide(new BigDecimal("100")))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * 手数料タイプに基づいて自動計算
     * 
     * @param calculationBase 計算基準額
     * @param feeType 手数料タイプ
     * @return 計算された手数料額
     */
    public static BigDecimal calculateFeeAmount(BigDecimal calculationBase, FeeType feeType) {
        BigDecimal defaultRate = getDefaultRate(feeType);
        return calculateFeeAmount(calculationBase, defaultRate);
    }
    
    /**
     * 受取人選択が必要かチェック
     * 
     * @param feeType 手数料タイプ
     * @return 受取人選択が必要な場合 true
     */
    public static boolean requiresRecipientSelection(FeeType feeType) {
        RecipientType recipientType = getRecipientType(feeType);
        return recipientType == RecipientType.AGENT_BANK || 
               recipientType == RecipientType.INVESTOR;
    }
    
    /**
     * 投資家配分が必要かチェック
     * 
     * @param feeType 手数料タイプ
     * @return 投資家配分が必要な場合 true
     */
    public static boolean requiresInvestorDistribution(FeeType feeType) {
        return getRecipientType(feeType) == RecipientType.AUTO_DISTRIBUTE;
    }
}