package com.example.syndicatelending.fee.service;

import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.fee.dto.CreateFeePaymentRequest;
import com.example.syndicatelending.fee.entity.FeeDistribution;
import com.example.syndicatelending.fee.entity.FeePayment;
import com.example.syndicatelending.fee.entity.FeeType;
import com.example.syndicatelending.fee.repository.FeePaymentRepository;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.entity.SharePie;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 手数料支払いサービス
 * 
 * 手数料支払い処理のビジネスロジックを提供し、
 * 手数料計算・配分・管理機能を実現する。
 */
@Service
@Transactional
public class FeePaymentService {

    private final FeePaymentRepository feePaymentRepository;
    private final FacilityRepository facilityRepository;

    public FeePaymentService(FeePaymentRepository feePaymentRepository,
                           FacilityRepository facilityRepository) {
        this.feePaymentRepository = feePaymentRepository;
        this.facilityRepository = facilityRepository;
    }

    /**
     * 手数料支払いを作成
     * 
     * @param request 手数料支払い作成リクエスト
     * @return 作成された手数料支払い
     */
    public FeePayment createFeePayment(CreateFeePaymentRequest request) {
        // バリデーション
        validateFeePaymentRequest(request);

        // Facility存在確認（SharePieも一緒に取得）
        Facility facility = facilityRepository.findById(request.getFacilityId())
            .orElseThrow(() -> new ResourceNotFoundException("Facility not found: " + request.getFacilityId()));
        
        // SharePieのLazy Loadingを明示的に初期化
        facility.getSharePies().size(); // Lazy Loadingを強制実行

        // 手数料支払いエンティティ作成
        Money feeAmount = Money.of(request.getFeeAmount());
        Money calculationBase = Money.of(request.getCalculationBase());

        FeePayment feePayment = new FeePayment(
            request.getFeeType(),
            request.getFeeDate(),
            feeAmount,
            calculationBase,
            request.getFeeRate(),
            request.getRecipientType(),
            request.getRecipientId(),
            request.getCurrency(),
            request.getDescription()
        );

        // Transaction基底クラスのフィールド設定
        feePayment.setFacilityId(request.getFacilityId());
        feePayment.setBorrowerId(request.getBorrowerId());

        // 手数料配分生成（投資家配分が必要な場合）
        if (feePayment.requiresInvestorDistribution()) {
            List<FeeDistribution> distributions = generateFeeDistributions(feePayment, facility);
            // 手数料配分の逆参照設定（保存前に設定）
            distributions.forEach(dist -> dist.setFeePayment(feePayment));
            feePayment.setFeeDistributions(distributions);
        }

        // 保存
        FeePayment savedFeePayment = feePaymentRepository.save(feePayment);

        return savedFeePayment;
    }

    /**
     * 手数料支払いをIDで取得
     * 
     * @param feePaymentId 手数料支払いID
     * @return 手数料支払い
     */
    @Transactional(readOnly = true)
    public FeePayment getFeePaymentById(Long feePaymentId) {
        return feePaymentRepository.findById(feePaymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Fee payment not found: " + feePaymentId));
    }

    /**
     * Facility IDで手数料支払い履歴を取得
     * 
     * @param facilityId Facility ID
     * @return 手数料支払いのリスト
     */
    @Transactional(readOnly = true)
    public List<FeePayment> getFeePaymentsByFacility(Long facilityId) {
        return feePaymentRepository.findByFacilityId(facilityId);
    }

    /**
     * 手数料タイプで検索
     * 
     * @param feeType 手数料タイプ
     * @return 手数料支払いのリスト
     */
    @Transactional(readOnly = true)
    public List<FeePayment> getFeePaymentsByType(FeeType feeType) {
        return feePaymentRepository.findByFeeType(feeType);
    }

    /**
     * 日付範囲で手数料支払いを検索
     * 
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 手数料支払いのリスト
     */
    @Transactional(readOnly = true)
    public List<FeePayment> getFeePaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return feePaymentRepository.findByFeeDateBetween(startDate, endDate);
    }

    /**
     * 全手数料支払いをページング取得
     * 
     * @param pageable ページング情報
     * @return 手数料支払いのページ
     */
    @Transactional(readOnly = true)
    public Page<FeePayment> getAllFeePayments(Pageable pageable) {
        return feePaymentRepository.findAll(pageable);
    }

    /**
     * 手数料配分を生成
     * 
     * @param feePayment 手数料支払い
     * @param facility Facility
     * @return 手数料配分のリスト
     */
    private List<FeeDistribution> generateFeeDistributions(FeePayment feePayment, Facility facility) {
        List<FeeDistribution> distributions = new ArrayList<>();

        // SharePieに基づく投資家別配分計算
        for (SharePie sharePie : facility.getSharePies()) {
            // 配分金額計算：手数料総額 × 持分比率
            // SharePieの値は既に小数点形式（0.4 = 40%）なので100で除算不要
            Money distributionAmount = feePayment.getAmount()
                .multiply(sharePie.getShare().getValue());

            FeeDistribution distribution = new FeeDistribution(
                "INVESTOR",
                sharePie.getInvestorId(),
                distributionAmount,
                sharePie.getShare().getValue().multiply(BigDecimal.valueOf(100)).doubleValue(), // パーセンテージ表示のため100倍
                feePayment.getCurrency()
            );

            distributions.add(distribution);
        }

        return distributions;
    }

    /**
     * 手数料支払いリクエストのバリデーション
     * 
     * @param request 手数料支払いリクエスト
     */
    private void validateFeePaymentRequest(CreateFeePaymentRequest request) {
        // 手数料計算の整合性チェック
        BigDecimal expectedAmount = request.getCalculationBase()
            .multiply(BigDecimal.valueOf(request.getFeeRate() / 100.0));

        if (request.getFeeAmount().compareTo(expectedAmount) != 0) {
            throw new BusinessRuleViolationException(
                "Fee amount calculation mismatch. Expected: " + expectedAmount + 
                ", Actual: " + request.getFeeAmount());
        }

        // 手数料日付の妥当性チェック
        if (request.getFeeDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleViolationException(
                "Fee date cannot be in the future: " + request.getFeeDate());
        }

        // 手数料タイプと受益者タイプの整合性チェック
        validateFeeTypeAndRecipientType(request.getFeeType(), request.getRecipientType());
    }

    /**
     * 手数料タイプと受益者タイプの整合性検証
     * 
     * @param feeType 手数料タイプ
     * @param recipientType 受益者タイプ
     */
    private void validateFeeTypeAndRecipientType(FeeType feeType, String recipientType) {
        switch (feeType) {
            case MANAGEMENT_FEE:
            case ARRANGEMENT_FEE:
                if (!"BANK".equals(recipientType)) {
                    throw new BusinessRuleViolationException(
                        "Management/Arrangement fee recipient must be BANK, but was: " + recipientType);
                }
                break;
            case COMMITMENT_FEE:
            case LATE_FEE:
                if (!"INVESTOR".equals(recipientType)) {
                    throw new BusinessRuleViolationException(
                        "Commitment/Late fee recipient must be INVESTOR, but was: " + recipientType);
                }
                break;
            case TRANSACTION_FEE:
            case AGENT_FEE:
                if (!"BANK".equals(recipientType)) {
                    throw new BusinessRuleViolationException(
                        "Transaction/Agent fee recipient must be BANK, but was: " + recipientType);
                }
                break;
            case OTHER_FEE:
                // OTHER_FEEは任意の受益者タイプを許可
                break;
            default:
                throw new BusinessRuleViolationException("Unknown fee type: " + feeType);
        }
    }
}