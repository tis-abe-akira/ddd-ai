package com.example.syndicatelending.transaction.service;

import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.transaction.entity.Transaction;
import com.example.syndicatelending.transaction.entity.TransactionType;
import com.example.syndicatelending.transaction.entity.TransactionStatus;
import com.example.syndicatelending.loan.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 取引管理の統合サービス
 * 
 * 全ての取引タイプに共通する横断的なビジネスロジックを提供し、
 * 取引の状態管理、検索、レポート機能を実現する。
 */
@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * 取引IDで取引を取得
     * 
     * @param transactionId 取引ID
     * @return 取引エンティティ
     * @throws ResourceNotFoundException 取引が見つからない場合
     */
    @Transactional(readOnly = true)
    public Transaction getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
    }

    /**
     * Facility IDで取引履歴を取得
     * 
     * @param facilityId Facility ID
     * @return 取引のリスト
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByFacility(Long facilityId) {
        return transactionRepository.findByFacilityId(facilityId);
    }

    /**
     * Borrower IDで取引履歴を取得
     * 
     * @param borrowerId Borrower ID
     * @return 取引のリスト
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByBorrower(Long borrowerId) {
        return transactionRepository.findByBorrowerId(borrowerId);
    }

    /**
     * 取引タイプで取引を取得
     * 
     * @param transactionType 取引タイプ
     * @return 取引のリスト
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByType(TransactionType transactionType) {
        return transactionRepository.findByTransactionType(transactionType);
    }

    /**
     * 全取引をページング取得
     * 
     * @param pageable ページング情報
     * @return 取引のページ
     */
    @Transactional(readOnly = true)
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    /**
     * 取引を承認し、処理中状態に変更
     * 
     * @param transactionId 取引ID
     * @throws BusinessRuleViolationException 承認できない状態の場合
     */
    public void approveTransaction(Long transactionId) {
        Transaction transaction = getTransactionById(transactionId);
        
        if (transaction.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessRuleViolationException(
                "Transaction cannot be approved. Current status: " + transaction.getStatus());
        }
        
        transaction.setStatus(TransactionStatus.ACTIVE);
        transactionRepository.save(transaction);
    }

    /**
     * 取引を完了状態に変更
     * 
     * @param transactionId 取引ID
     * @throws BusinessRuleViolationException 完了できない状態の場合
     */
    public void completeTransaction(Long transactionId) {
        Transaction transaction = getTransactionById(transactionId);
        
        if (transaction.getStatus() != TransactionStatus.ACTIVE) {
            throw new BusinessRuleViolationException(
                "Transaction cannot be completed. Current status: " + transaction.getStatus());
        }
        
        transaction.markAsCompleted();
        transactionRepository.save(transaction);
    }

    /**
     * 取引をキャンセル
     * 
     * @param transactionId 取引ID
     * @param reason キャンセル理由
     * @throws BusinessRuleViolationException キャンセルできない状態の場合
     */
    public void cancelTransaction(Long transactionId, String reason) {
        Transaction transaction = getTransactionById(transactionId);
        
        if (!transaction.isCancellable()) {
            throw new BusinessRuleViolationException(
                "Transaction cannot be cancelled. Current status: " + transaction.getStatus());
        }
        
        transaction.markAsCancelled();
        transactionRepository.save(transaction);
    }

    /**
     * 取引を失敗状態に変更
     * 
     * @param transactionId 取引ID
     * @param errorMessage エラーメッセージ
     */
    public void failTransaction(Long transactionId, String errorMessage) {
        Transaction transaction = getTransactionById(transactionId);
        transaction.markAsFailed();
        transactionRepository.save(transaction);
    }

    /**
     * 取引統計を取得
     * 
     * @param facilityId Facility ID
     * @return 取引統計情報
     */
    @Transactional(readOnly = true)
    public TransactionStatistics getTransactionStatistics(Long facilityId) {
        List<Transaction> transactions = getTransactionsByFacility(facilityId);
        
        long completedCount = transactions.stream()
            .filter(Transaction::isCompleted)
            .count();
            
        long pendingCount = transactions.stream()
            .filter(t -> t.getStatus() == TransactionStatus.DRAFT)
            .count();
            
        long processingCount = transactions.stream()
            .filter(Transaction::isActive)
            .count();
            
        return new TransactionStatistics(
            transactions.size(),
            completedCount,
            pendingCount,
            processingCount
        );
    }

    /**
     * 取引統計情報クラス
     */
    public static class TransactionStatistics {
        private final long totalCount;
        private final long completedCount;
        private final long pendingCount;
        private final long processingCount;

        public TransactionStatistics(long totalCount, long completedCount, long pendingCount, long processingCount) {
            this.totalCount = totalCount;
            this.completedCount = completedCount;
            this.pendingCount = pendingCount;
            this.processingCount = processingCount;
        }

        public long getTotalCount() { return totalCount; }
        public long getCompletedCount() { return completedCount; }
        public long getPendingCount() { return pendingCount; }
        public long getProcessingCount() { return processingCount; }
    }
}