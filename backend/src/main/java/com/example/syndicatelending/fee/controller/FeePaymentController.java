package com.example.syndicatelending.fee.controller;

import com.example.syndicatelending.fee.dto.CreateFeePaymentRequest;
import com.example.syndicatelending.fee.entity.FeePayment;
import com.example.syndicatelending.fee.entity.FeeType;
import com.example.syndicatelending.fee.service.FeePaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 手数料支払いAPI
 * 
 * 手数料支払いの作成、検索、管理のための
 * REST APIエンドポイントを提供する。
 */
@RestController
@RequestMapping("/api/v1/fees/payments")
public class FeePaymentController {

    private final FeePaymentService feePaymentService;

    public FeePaymentController(FeePaymentService feePaymentService) {
        this.feePaymentService = feePaymentService;
    }

    /**
     * 手数料支払いを作成
     * 
     * @param request 手数料支払い作成リクエスト
     * @return 作成された手数料支払い
     */
    @PostMapping
    public ResponseEntity<FeePayment> createFeePayment(@Valid @RequestBody CreateFeePaymentRequest request) {
        FeePayment feePayment = feePaymentService.createFeePayment(request);
        return ResponseEntity.ok(feePayment);
    }

    /**
     * 手数料支払いIDで取得
     * 
     * @param feePaymentId 手数料支払いID
     * @return 手数料支払い情報
     */
    @GetMapping("/{feePaymentId}")
    public ResponseEntity<FeePayment> getFeePayment(@PathVariable Long feePaymentId) {
        FeePayment feePayment = feePaymentService.getFeePaymentById(feePaymentId);
        return ResponseEntity.ok(feePayment);
    }

    /**
     * 全手数料支払いをページング取得
     * 
     * @param pageable ページング情報
     * @return 手数料支払いのページ
     */
    @GetMapping
    public ResponseEntity<Page<FeePayment>> getAllFeePayments(Pageable pageable) {
        Page<FeePayment> feePayments = feePaymentService.getAllFeePayments(pageable);
        return ResponseEntity.ok(feePayments);
    }

    /**
     * Facility IDで手数料支払い履歴を取得
     * 
     * @param facilityId Facility ID
     * @return 手数料支払い履歴のリスト
     */
    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<FeePayment>> getFeePaymentsByFacility(@PathVariable Long facilityId) {
        List<FeePayment> feePayments = feePaymentService.getFeePaymentsByFacility(facilityId);
        return ResponseEntity.ok(feePayments);
    }

    /**
     * 手数料タイプで検索
     * 
     * @param feeType 手数料タイプ
     * @return 手数料支払いのリスト
     */
    @GetMapping("/type/{feeType}")
    public ResponseEntity<List<FeePayment>> getFeePaymentsByType(@PathVariable FeeType feeType) {
        List<FeePayment> feePayments = feePaymentService.getFeePaymentsByType(feeType);
        return ResponseEntity.ok(feePayments);
    }

    /**
     * 日付範囲で手数料支払いを検索
     * 
     * @param startDate 開始日 (YYYY-MM-DD形式)
     * @param endDate 終了日 (YYYY-MM-DD形式)
     * @return 手数料支払いのリスト
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<FeePayment>> getFeePaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<FeePayment> feePayments = feePaymentService.getFeePaymentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(feePayments);
    }

    /**
     * 手数料支払い統計レスポンス
     */
    public static class FeePaymentStatistics {
        private final long totalCount;
        private final String totalAmount;
        private final long managementFeeCount;
        private final long arrangementFeeCount;
        private final long commitmentFeeCount;
        private final long lateFeeCount;

        public FeePaymentStatistics(long totalCount, String totalAmount, long managementFeeCount,
                                  long arrangementFeeCount, long commitmentFeeCount, long lateFeeCount) {
            this.totalCount = totalCount;
            this.totalAmount = totalAmount;
            this.managementFeeCount = managementFeeCount;
            this.arrangementFeeCount = arrangementFeeCount;
            this.commitmentFeeCount = commitmentFeeCount;
            this.lateFeeCount = lateFeeCount;
        }

        // Getters
        public long getTotalCount() { return totalCount; }
        public String getTotalAmount() { return totalAmount; }
        public long getManagementFeeCount() { return managementFeeCount; }
        public long getArrangementFeeCount() { return arrangementFeeCount; }
        public long getCommitmentFeeCount() { return commitmentFeeCount; }
        public long getLateFeeCount() { return lateFeeCount; }
    }

    /**
     * 手数料支払いを削除
     * 
     * @param feePaymentId 削除する手数料支払いID
     * @return 削除完了レスポンス
     */
    @DeleteMapping("/{feePaymentId}")
    public ResponseEntity<Void> deleteFeePayment(@PathVariable Long feePaymentId) {
        feePaymentService.deleteFeePayment(feePaymentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Facility別手数料統計を取得
     * 
     * @param facilityId Facility ID
     * @return 手数料統計情報
     */
    @GetMapping("/facility/{facilityId}/statistics")
    public ResponseEntity<FeePaymentStatistics> getFeePaymentStatistics(@PathVariable Long facilityId) {
        List<FeePayment> feePayments = feePaymentService.getFeePaymentsByFacility(facilityId);
        
        long totalCount = feePayments.size();
        String totalAmount = feePayments.stream()
            .map(fp -> fp.getAmount().getAmount())
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
            .toString();
        
        long managementFeeCount = feePayments.stream()
            .filter(fp -> fp.getFeeType() == FeeType.MANAGEMENT_FEE)
            .count();
        
        long arrangementFeeCount = feePayments.stream()
            .filter(fp -> fp.getFeeType() == FeeType.ARRANGEMENT_FEE)
            .count();
        
        long commitmentFeeCount = feePayments.stream()
            .filter(fp -> fp.getFeeType() == FeeType.COMMITMENT_FEE)
            .count();
        
        long lateFeeCount = feePayments.stream()
            .filter(fp -> fp.getFeeType() == FeeType.LATE_FEE)
            .count();

        FeePaymentStatistics statistics = new FeePaymentStatistics(
            totalCount, totalAmount, managementFeeCount, 
            arrangementFeeCount, commitmentFeeCount, lateFeeCount
        );
        
        return ResponseEntity.ok(statistics);
    }
}