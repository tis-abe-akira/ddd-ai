package com.example.syndicatelending.loan.controller;

import com.example.syndicatelending.loan.entity.Loan;
import com.example.syndicatelending.loan.entity.PaymentDetail;
import com.example.syndicatelending.loan.service.LoanService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ローン管理REST APIコントローラー
 * <p>
 * ローンエンティティに関するREST APIエンドポイントを提供します。
 * </p>
 */
@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {
    
    private final LoanService loanService;
    
    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }
    
    /**
     * ローンIDでローン詳細を取得します。
     * 
     * @param id ローンID
     * @return ローン詳細
     */
    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        Loan loan = loanService.getLoanById(id);
        return ResponseEntity.ok(loan);
    }
    
    /**
     * ドローダウンIDから関連するローンを取得します。
     * 
     * @param drawdownId ドローダウンID
     * @return 関連するローン
     */
    @GetMapping("/drawdown/{drawdownId}")
    public ResponseEntity<Loan> getLoanByDrawdownId(@PathVariable Long drawdownId) {
        Loan loan = loanService.getLoanByDrawdownId(drawdownId);
        return ResponseEntity.ok(loan);
    }
    
    /**
     * 全てのローンをページネーション付きで取得します。
     * 
     * @param pageable ページネーション情報
     * @return ローンのページ
     */
    @GetMapping
    public ResponseEntity<Page<Loan>> getAllLoans(Pageable pageable) {
        Page<Loan> loans = loanService.getAllLoans(pageable);
        return ResponseEntity.ok(loans);
    }
    
    /**
     * ファシリティIDに関連するローンを取得します。
     * 
     * @param facilityId ファシリティID
     * @return ローンのリスト
     */
    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<Loan>> getLoansByFacilityId(@PathVariable Long facilityId) {
        List<Loan> loans = loanService.getLoansByFacilityId(facilityId);
        return ResponseEntity.ok(loans);
    }
    
    /**
     * 借り手IDに関連するローンを取得します。
     * 
     * @param borrowerId 借り手ID
     * @return ローンのリスト
     */
    @GetMapping("/borrower/{borrowerId}")
    public ResponseEntity<List<Loan>> getLoansByBorrowerId(@PathVariable Long borrowerId) {
        List<Loan> loans = loanService.getLoansByBorrowerId(borrowerId);
        return ResponseEntity.ok(loans);
    }

    /**
     * ローンIDに関連するPaymentDetailを取得します。
     * 
     * @param loanId ローンID
     * @return PaymentDetailのリスト
     */
    @GetMapping("/{loanId}/payment-details")
    public ResponseEntity<List<PaymentDetail>> getPaymentDetailsByLoanId(@PathVariable Long loanId) {
        List<PaymentDetail> paymentDetails = loanService.getPaymentDetailsByLoanId(loanId);
        return ResponseEntity.ok(paymentDetails);
    }
}