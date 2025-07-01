package com.example.syndicatelending.transaction.controller;

import com.example.syndicatelending.transaction.entity.Transaction;
import com.example.syndicatelending.transaction.entity.TransactionType;
import com.example.syndicatelending.transaction.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 取引管理API
 * 
 * 取引履歴の参照、統計情報の取得、取引状態管理のための
 * REST APIエンドポイントを提供する。
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * 取引ID で取引を取得
     * 
     * @param transactionId 取引ID
     * @return 取引情報
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long transactionId) {
        Transaction transaction = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(transaction);
    }

    /**
     * 全取引をページング取得
     * 
     * @param pageable ページング情報
     * @return 取引のページ
     */
    @GetMapping
    public ResponseEntity<Page<Transaction>> getAllTransactions(Pageable pageable) {
        Page<Transaction> transactions = transactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Facility ID で取引履歴を取得
     * 
     * @param facilityId Facility ID
     * @return 取引履歴のリスト
     */
    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<Transaction>> getTransactionsByFacility(@PathVariable Long facilityId) {
        List<Transaction> transactions = transactionService.getTransactionsByFacility(facilityId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Borrower ID で取引履歴を取得
     * 
     * @param borrowerId Borrower ID
     * @return 取引履歴のリスト
     */
    @GetMapping("/borrower/{borrowerId}")
    public ResponseEntity<List<Transaction>> getTransactionsByBorrower(@PathVariable Long borrowerId) {
        List<Transaction> transactions = transactionService.getTransactionsByBorrower(borrowerId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * 取引タイプで取引を取得
     * 
     * @param transactionType 取引タイプ
     * @return 取引のリスト
     */
    @GetMapping("/type/{transactionType}")
    public ResponseEntity<List<Transaction>> getTransactionsByType(@PathVariable TransactionType transactionType) {
        List<Transaction> transactions = transactionService.getTransactionsByType(transactionType);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Facility の取引統計を取得
     * 
     * @param facilityId Facility ID
     * @return 取引統計情報
     */
    @GetMapping("/facility/{facilityId}/statistics")
    public ResponseEntity<TransactionService.TransactionStatistics> getTransactionStatistics(@PathVariable Long facilityId) {
        TransactionService.TransactionStatistics statistics = transactionService.getTransactionStatistics(facilityId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 取引を承認
     * 
     * @param transactionId 取引ID
     * @return 成功メッセージ
     */
    @PostMapping("/{transactionId}/approve")
    public ResponseEntity<String> approveTransaction(@PathVariable Long transactionId) {
        transactionService.approveTransaction(transactionId);
        return ResponseEntity.ok("Transaction approved successfully");
    }

    /**
     * 取引を完了
     * 
     * @param transactionId 取引ID
     * @return 成功メッセージ
     */
    @PostMapping("/{transactionId}/complete")
    public ResponseEntity<String> completeTransaction(@PathVariable Long transactionId) {
        transactionService.completeTransaction(transactionId);
        return ResponseEntity.ok("Transaction completed successfully");
    }

    /**
     * 取引をキャンセル
     * 
     * @param transactionId 取引ID
     * @param request キャンセル理由
     * @return 成功メッセージ
     */
    @PostMapping("/{transactionId}/cancel")
    public ResponseEntity<String> cancelTransaction(
            @PathVariable Long transactionId,
            @RequestBody CancelTransactionRequest request) {
        transactionService.cancelTransaction(transactionId, request.getReason());
        return ResponseEntity.ok("Transaction cancelled successfully");
    }

    /**
     * 取引を失敗状態に変更
     * 
     * @param transactionId 取引ID
     * @param request エラーメッセージ
     * @return 成功メッセージ
     */
    @PostMapping("/{transactionId}/fail")
    public ResponseEntity<String> failTransaction(
            @PathVariable Long transactionId,
            @RequestBody FailTransactionRequest request) {
        transactionService.failTransaction(transactionId, request.getErrorMessage());
        return ResponseEntity.ok("Transaction marked as failed");
    }

    /**
     * 取引キャンセルリクエスト
     */
    public static class CancelTransactionRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    /**
     * 取引失敗リクエスト
     */
    public static class FailTransactionRequest {
        private String errorMessage;

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}