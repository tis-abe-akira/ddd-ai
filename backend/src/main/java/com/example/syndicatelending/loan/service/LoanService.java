package com.example.syndicatelending.loan.service;

import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.loan.entity.Loan;
import com.example.syndicatelending.loan.entity.Drawdown;
import com.example.syndicatelending.loan.entity.PaymentDetail;
import com.example.syndicatelending.loan.repository.LoanRepository;
import com.example.syndicatelending.loan.repository.DrawdownRepository;
import com.example.syndicatelending.loan.repository.PaymentDetailRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ローン管理サービス
 * <p>
 * ローンエンティティに関するビジネスロジックを処理します。
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class LoanService {
    
    private final LoanRepository loanRepository;
    private final DrawdownRepository drawdownRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    
    public LoanService(LoanRepository loanRepository, DrawdownRepository drawdownRepository, PaymentDetailRepository paymentDetailRepository) {
        this.loanRepository = loanRepository;
        this.drawdownRepository = drawdownRepository;
        this.paymentDetailRepository = paymentDetailRepository;
    }
    
    /**
     * ローンIDでローンを取得します。
     * 
     * @param id ローンID
     * @return ローンエンティティ
     * @throws ResourceNotFoundException ローンが見つからない場合
     */
    public Loan getLoanById(Long id) {
        return loanRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));
    }
    
    /**
     * ドローダウンIDから関連するローンを取得します。
     * 
     * @param drawdownId ドローダウンID
     * @return ローンエンティティ
     * @throws ResourceNotFoundException ドローダウンまたはローンが見つからない場合
     */
    public Loan getLoanByDrawdownId(Long drawdownId) {
        // ドローダウンを取得
        Drawdown drawdown = drawdownRepository.findById(drawdownId)
            .orElseThrow(() -> new ResourceNotFoundException("Drawdown not found with id: " + drawdownId));
        
        // ドローダウンのloanIdからローンを取得
        return getLoanById(drawdown.getLoanId());
    }
    
    /**
     * 全てのローンをページネーション付きで取得します。
     * 
     * @param pageable ページネーション情報
     * @return ローンのページ
     */
    public Page<Loan> getAllLoans(Pageable pageable) {
        return loanRepository.findAll(pageable);
    }
    
    /**
     * ファシリティIDに関連するローンを取得します。
     * 
     * @param facilityId ファシリティID
     * @return ローンのリスト
     */
    public List<Loan> getLoansByFacilityId(Long facilityId) {
        return loanRepository.findByFacilityId(facilityId);
    }
    
    /**
     * 借り手IDに関連するローンを取得します。
     * 
     * @param borrowerId 借り手ID
     * @return ローンのリスト
     */
    public List<Loan> getLoansByBorrowerId(Long borrowerId) {
        return loanRepository.findByBorrowerId(borrowerId);
    }
    
    /**
     * ファシリティIDと借り手IDに関連するローンを取得します。
     * 
     * @param facilityId ファシリティID
     * @param borrowerId 借り手ID
     * @return ローンのリスト
     */
    public List<Loan> getLoansByFacilityIdAndBorrowerId(Long facilityId, Long borrowerId) {
        return loanRepository.findByFacilityIdAndBorrowerId(facilityId, borrowerId);
    }

    /**
     * ローンIDに関連するPaymentDetailを取得します。
     * 
     * @param loanId ローンID
     * @return PaymentDetailのリスト
     */
    public List<PaymentDetail> getPaymentDetailsByLoanId(Long loanId) {
        return paymentDetailRepository.findByLoanIdOrderByPaymentNumber(loanId);
    }
}