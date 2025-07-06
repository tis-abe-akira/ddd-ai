package com.example.syndicatelending.loan.repository;

import com.example.syndicatelending.loan.entity.PaymentDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PaymentDetailエンティティのリポジトリインターフェース。
 * <p>
 * 支払い詳細に関するデータアクセス操作を提供します。
 * </p>
 */
@Repository
public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {

    /**
     * 指定されたローンIDに関連するすべての支払い詳細を、支払い番号順で取得します。
     *
     * @param loanId ローンID
     * @return 支払い詳細のリスト（支払い番号順）
     */
    @Query("SELECT pd FROM PaymentDetail pd WHERE pd.loan.id = :loanId ORDER BY pd.paymentNumber")
    List<PaymentDetail> findByLoanIdOrderByPaymentNumber(@Param("loanId") Long loanId);

    /**
     * 指定されたローンIDに関連する支払い詳細をページングで取得します。
     *
     * @param loanId   ローンID
     * @param pageable ページング情報
     * @return 支払い詳細のページ
     */
    @Query("SELECT pd FROM PaymentDetail pd WHERE pd.loan.id = :loanId")
    Page<PaymentDetail> findByLoanId(@Param("loanId") Long loanId, Pageable pageable);

    /**
     * 指定されたローンIDの支払い詳細の件数を取得します。
     *
     * @param loanId ローンID
     * @return 支払い詳細の件数
     */
    long countByLoanId(Long loanId);

    /**
     * 指定されたローンIDの支払い詳細をすべて削除します。
     *
     * @param loanId ローンID
     */
    void deleteByLoanId(Long loanId);

    /**
     * 指定されたPaymentIDに関連するPaymentDetailを取得します。
     *
     * @param paymentId PaymentID
     * @return PaymentDetail（存在しない場合は空のOptional）
     */
    Optional<PaymentDetail> findByPaymentId(Long paymentId);
}
