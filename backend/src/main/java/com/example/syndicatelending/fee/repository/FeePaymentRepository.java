package com.example.syndicatelending.fee.repository;

import com.example.syndicatelending.fee.entity.FeePayment;
import com.example.syndicatelending.fee.entity.FeeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 手数料支払いリポジトリ
 * 
 * FeePaymentエンティティのデータアクセスを提供し、
 * 手数料関連の検索・集計機能を実現する。
 */
@Repository
public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {

    /**
     * Facility IDで手数料支払いを検索
     * 
     * @param facilityId Facility ID
     * @return 手数料支払いのリスト
     */
    List<FeePayment> findByFacilityId(Long facilityId);

    /**
     * Borrower IDで手数料支払いを検索
     * 
     * @param borrowerId Borrower ID
     * @return 手数料支払いのリスト
     */
    List<FeePayment> findByBorrowerId(Long borrowerId);

    /**
     * 手数料タイプで検索
     * 
     * @param feeType 手数料タイプ
     * @return 手数料支払いのリスト
     */
    List<FeePayment> findByFeeType(FeeType feeType);

    /**
     * 手数料日付範囲で検索
     * 
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 手数料支払いのリスト
     */
    List<FeePayment> findByFeeDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 受益者タイプと受益者IDで検索
     * 
     * @param recipientType 受益者タイプ
     * @param recipientId 受益者ID
     * @return 手数料支払いのリスト
     */
    List<FeePayment> findByRecipientTypeAndRecipientId(String recipientType, Long recipientId);

    /**
     * Facility IDと手数料タイプで検索
     * 
     * @param facilityId Facility ID
     * @param feeType 手数料タイプ
     * @return 手数料支払いのリスト
     */
    List<FeePayment> findByFacilityIdAndFeeType(Long facilityId, FeeType feeType);

    /**
     * Facility IDと手数料日付範囲で検索
     * 
     * @param facilityId Facility ID
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 手数料支払いのリスト
     */
    List<FeePayment> findByFacilityIdAndFeeDateBetween(Long facilityId, LocalDate startDate, LocalDate endDate);
}