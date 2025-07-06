package com.example.syndicatelending.loan.service;

import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.loan.dto.CreateDrawdownRequest;
import com.example.syndicatelending.loan.dto.UpdateDrawdownRequest;
import com.example.syndicatelending.loan.entity.Drawdown;
import com.example.syndicatelending.loan.entity.Loan;
import com.example.syndicatelending.loan.entity.RepaymentCycle;
import com.example.syndicatelending.loan.repository.DrawdownRepository;
import com.example.syndicatelending.loan.repository.LoanRepository;
import com.example.syndicatelending.party.repository.BorrowerRepository;
import com.example.syndicatelending.transaction.entity.TransactionStatus;
import com.example.syndicatelending.loan.entity.AmountPie;
import com.example.syndicatelending.loan.dto.AmountPieDto;
import com.example.syndicatelending.facility.entity.SharePie;
import com.example.syndicatelending.facility.repository.SharePieRepository;
import com.example.syndicatelending.party.entity.Investor;
import com.example.syndicatelending.party.repository.InvestorRepository;
import com.example.syndicatelending.facility.service.FacilityService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * ドローダウンサービス - シンジケートローンの資金引き出し処理を管理
 * 
 * 主な責務：
 * - ドローダウン（資金引き出し）の実行
 * - 投資家への按分計算（AmountPie生成）
 * - 投資家の現在投資額の更新
 * - Facilityの状態管理（DRAFT → FIXED）
 */
@Service
public class DrawdownService {
    // データアクセス層
    private final DrawdownRepository drawdownRepository;
    private final LoanRepository loanRepository;
    private final FacilityRepository facilityRepository;
    private final BorrowerRepository borrowerRepository;
    private final SharePieRepository sharePieRepository;
    private final InvestorRepository investorRepository;
    
    // 他のサービス層（状態管理のため）
    private final FacilityService facilityService;

    public DrawdownService(DrawdownRepository drawdownRepository,
            LoanRepository loanRepository,
            FacilityRepository facilityRepository,
            BorrowerRepository borrowerRepository,
            SharePieRepository sharePieRepository,
            InvestorRepository investorRepository,
            FacilityService facilityService) {
        this.drawdownRepository = drawdownRepository;
        this.loanRepository = loanRepository;
        this.facilityRepository = facilityRepository;
        this.borrowerRepository = borrowerRepository;
        this.sharePieRepository = sharePieRepository;
        this.investorRepository = investorRepository;
        this.facilityService = facilityService;
    }

    /**
     * ドローダウンの実行
     * 
     * シンジケートローンにおいて、借り手がファシリティから資金を引き出す処理。
     * 以下のステップで実行される：
     * 1. 入力値の妥当性検証
     * 2. ローンエンティティの作成（返済スケジュール管理用）
     * 3. ドローダウンエンティティの作成（取引記録用）
     * 4. 投資家別の引き出し額按分（AmountPie生成）
     * 5. 各投資家の現在投資額を更新
     * 6. ファシリティを確定状態（FIXED）に変更
     * 
     * @param request ドローダウンリクエスト（金額、ファシリティID、借り手ID等）
     * @return 作成されたDrawdownエンティティ
     * @throws BusinessRuleViolationException 業務ルール違反時
     * @throws ResourceNotFoundException 関連エンティティが存在しない場合
     */
    @Transactional
    public Drawdown createDrawdown(CreateDrawdownRequest request) {
        // 1. バリデーション - 入力値とビジネスルールの検証
        validateDrawdownRequest(request);

        // 2. Loanエンティティの作成 - 返済スケジュール管理のためのローン記録
        Loan loan = createLoan(request);
        Loan savedLoan = loanRepository.save(loan);

        // 3. Drawdownエンティティの作成 - 資金引き出しの取引記録
        Drawdown drawdown = new Drawdown();
        drawdown.setFacilityId(request.getFacilityId());
        drawdown.setBorrowerId(request.getBorrowerId());
        drawdown.setTransactionDate(request.getDrawdownDate());
        drawdown.setAmount(Money.of(request.getAmount()));
        drawdown.setLoanId(savedLoan.getId());
        drawdown.setCurrency(request.getCurrency());
        drawdown.setPurpose(request.getPurpose());

        // 4. AmountPieの生成 - 投資家別の引き出し額按分計算
        List<AmountPie> amountPies = new ArrayList<>();
        if (request.getAmountPies() != null && !request.getAmountPies().isEmpty()) {
            // 4-1. 明示的指定ありの場合 - リクエストで投資家別金額が指定済み
            BigDecimal total = request.getAmountPies().stream().map(AmountPieDto::getAmount).reduce(BigDecimal.ZERO,
                    BigDecimal::add);
            if (total.compareTo(request.getAmount()) != 0) {
                throw new BusinessRuleViolationException("AmountPieの合計がDrawdown金額と一致しません");
            }
            // 指定された投資家別金額でAmountPieを作成
            for (AmountPieDto dto : request.getAmountPies()) {
                AmountPie pie = new AmountPie();
                pie.setInvestorId(dto.getInvestorId());
                pie.setAmount(dto.getAmount());
                pie.setCurrency(dto.getCurrency());
                pie.setDrawdown(drawdown);
                amountPies.add(pie);
            }
        } else {
            // 4-2. SharePieで按分 - ファシリティの持分比率に従って自動按分
            List<SharePie> sharePies = sharePieRepository.findByFacility_Id(request.getFacilityId());
            BigDecimal total = BigDecimal.ZERO;
            for (SharePie sharePie : sharePies) {
                AmountPie pie = new AmountPie();
                pie.setInvestorId(sharePie.getInvestorId());
                // 投資家の持分比率 × ドローダウン金額 = 投資家の引き出し額
                BigDecimal investorAmount = request.getAmount().multiply(sharePie.getShare().getValue());
                // 小数点以下第2位まで四捨五入（通貨の精度に合わせる）
                investorAmount = investorAmount.setScale(2, java.math.RoundingMode.HALF_UP);
                pie.setAmount(investorAmount);
                pie.setCurrency(request.getCurrency());
                pie.setDrawdown(drawdown);
                amountPies.add(pie);
                total = total.add(investorAmount);
            }
            // 端数調整 - 四捨五入による誤差を最後の投資家で調整
            if (!amountPies.isEmpty()) {
                AmountPie last = amountPies.get(amountPies.size() - 1);
                BigDecimal diff = request.getAmount().subtract(total);
                last.setAmount(last.getAmount().add(diff));
            }
        }
        drawdown.setAmountPies(amountPies);

        // 5. Drawdown保存 - 作成したドローダウンをデータベースに永続化
        Drawdown savedDrawdown = drawdownRepository.save(drawdown);

        // 6. Investor投資額の更新 - 各投資家の現在投資額（currentInvestmentAmount）を増加
        updateInvestorAmounts(amountPies);

        // 7. FacilityをFIXED状態に変更 - 初回ドローダウンでファシリティを確定状態にする
        //    これにより、以降のファシリティ変更（持分比率変更等）を禁止する
        facilityService.fixFacility(request.getFacilityId());

        return savedDrawdown;
    }

    /**
     * 全ドローダウンの取得
     * @return 全ドローダウンのリスト
     */
    @Transactional(readOnly = true)
    public List<Drawdown> getAllDrawdowns() {
        return drawdownRepository.findAll();
    }

    /**
     * ページネーション付きドローダウンの取得
     * @param pageable ページネーション情報
     * @return ページ分割されたドローダウンのリスト
     */
    @Transactional(readOnly = true)
    public Page<Drawdown> getAllDrawdowns(Pageable pageable) {
        return drawdownRepository.findAll(pageable);
    }

    /**
     * IDによるドローダウンの取得
     * @param id ドローダウンID
     * @return 指定されたドローダウン
     * @throws ResourceNotFoundException ドローダウンが存在しない場合
     */
    @Transactional(readOnly = true)
    public Drawdown getDrawdownById(Long id) {
        return drawdownRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drawdown not found with id: " + id));
    }

    /**
     * ファシリティIDによるドローダウンの取得
     * @param facilityId ファシリティID
     * @return 指定されたファシリティに関連するドローダウンのリスト
     */
    @Transactional(readOnly = true)
    public List<Drawdown> getDrawdownsByFacilityId(Long facilityId) {
        return drawdownRepository.findByFacilityId(facilityId);
    }

    /**
     * ドローダウンの削除
     * 
     * ドローダウンを削除し、関連するデータを適切にクリーンアップします。
     * - PENDING, FAILED状態のドローダウンのみ削除可能
     * - 投資家の投資額を元に戻す
     * - 関連するローンとPaymentDetailも削除
     * 
     * @param id 削除するドローダウンのID
     * @throws ResourceNotFoundException ドローダウンが存在しない場合
     * @throws BusinessRuleViolationException 削除不可能な状態の場合
     */
    @Transactional
    public void deleteDrawdown(Long id) {
        // 1. ドローダウンの存在確認
        Drawdown drawdown = drawdownRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drawdown not found with id: " + id));

        // 2. 削除可能状態の確認 (PENDING, FAILED のみ削除可能)
        if (!canDelete(drawdown.getStatus())) {
            throw new BusinessRuleViolationException(
                    "Cannot delete drawdown with status: " + drawdown.getStatus() + 
                    ". Only PENDING or FAILED drawdowns can be deleted.");
        }

        // 3. 投資家の投資額を元に戻す
        revertInvestorAmounts(drawdown.getAmountPies());

        // 4. 関連するローンの削除 (ローンが存在する場合)
        if (drawdown.getLoanId() != null) {
            loanRepository.deleteById(drawdown.getLoanId());
        }

        // 5. Facilityの状態をDRAFTに戻す（他にDrawdownが無い場合のみ）
        revertFacilityStateIfNeeded(drawdown.getFacilityId(), drawdown.getId());

        // 6. ドローダウンの削除 (AmountPieはCascadeで自動削除)
        drawdownRepository.delete(drawdown);
    }

    /**
     * 削除可能状態かどうかを判定
     */
    private boolean canDelete(TransactionStatus status) {
        return status == TransactionStatus.PENDING || status == TransactionStatus.FAILED;
    }

    /**
     * ドローダウンの更新
     * 
     * PENDING, FAILED状態のドローダウンのみ更新可能
     * 既存の投資額配分を調整し、新しい値で再計算する
     * 
     * @param id ドローダウンID
     * @param request 更新リクエスト
     * @return 更新されたDrawdownエンティティ
     * @throws ResourceNotFoundException ドローダウンが存在しない場合
     * @throws BusinessRuleViolationException 更新不可能な状態の場合
     */
    @Transactional
    public Drawdown updateDrawdown(Long id, UpdateDrawdownRequest request) {
        // 1. ドローダウンの存在確認
        Drawdown drawdown = drawdownRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drawdown not found with id: " + id));

        // 2. 更新可能状態の確認 (PENDING, FAILED のみ更新可能)
        if (!canEdit(drawdown.getStatus())) {
            throw new BusinessRuleViolationException(
                    "Cannot update drawdown with status: " + drawdown.getStatus() + 
                    ". Only PENDING or FAILED drawdowns can be updated.");
        }

        // 3. 楽観的ロックの確認
        if (!drawdown.getVersion().equals(request.getVersion())) {
            throw new BusinessRuleViolationException(
                    "Drawdown has been modified by another user. Please refresh and try again.");
        }

        // 4. 既存の投資額を元に戻す
        revertInvestorAmounts(drawdown.getAmountPies());

        // 5. ドローダウンの更新
        drawdown.setAmount(Money.of(request.getAmount()));
        drawdown.setCurrency(request.getCurrency());
        drawdown.setPurpose(request.getPurpose());
        drawdown.setTransactionDate(request.getDrawdownDate());

        // 6. 関連するローンの更新
        if (drawdown.getLoanId() != null) {
            Loan loan = loanRepository.findById(drawdown.getLoanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + drawdown.getLoanId()));
            
            loan.setPrincipalAmount(Money.of(request.getAmount()));
            loan.setAnnualInterestRate(Percentage.of(request.getAnnualInterestRate()));
            loan.setDrawdownDate(request.getDrawdownDate());
            loan.setRepaymentPeriodMonths(request.getRepaymentPeriodMonths());
            loan.setRepaymentCycle(RepaymentCycle.valueOf(request.getRepaymentCycle()));
            loan.setRepaymentMethod(request.getRepaymentMethod());
            loan.setCurrency(request.getCurrency());
            
            // 支払いスケジュールを再生成
            loan.generatePaymentSchedule();
            loanRepository.save(loan);
        }

        // 7. AmountPieの再計算
        List<AmountPie> newAmountPies = new ArrayList<>();
        if (request.getAmountPies() != null && !request.getAmountPies().isEmpty()) {
            // 明示的指定ありの場合
            BigDecimal total = request.getAmountPies().stream().map(AmountPieDto::getAmount).reduce(BigDecimal.ZERO,
                    BigDecimal::add);
            if (total.compareTo(request.getAmount()) != 0) {
                throw new BusinessRuleViolationException("AmountPieの合計がDrawdown金額と一致しません");
            }
            for (AmountPieDto dto : request.getAmountPies()) {
                AmountPie pie = new AmountPie();
                pie.setInvestorId(dto.getInvestorId());
                pie.setAmount(dto.getAmount());
                pie.setCurrency(dto.getCurrency());
                pie.setDrawdown(drawdown);
                newAmountPies.add(pie);
            }
        } else {
            // SharePieで按分
            List<SharePie> sharePies = sharePieRepository.findByFacility_Id(drawdown.getFacilityId());
            BigDecimal total = BigDecimal.ZERO;
            for (SharePie sharePie : sharePies) {
                AmountPie pie = new AmountPie();
                pie.setInvestorId(sharePie.getInvestorId());
                BigDecimal investorAmount = request.getAmount().multiply(sharePie.getShare().getValue());
                investorAmount = investorAmount.setScale(2, java.math.RoundingMode.HALF_UP);
                pie.setAmount(investorAmount);
                pie.setCurrency(request.getCurrency());
                pie.setDrawdown(drawdown);
                newAmountPies.add(pie);
                total = total.add(investorAmount);
            }
            // 端数調整
            if (!newAmountPies.isEmpty()) {
                AmountPie last = newAmountPies.get(newAmountPies.size() - 1);
                BigDecimal diff = request.getAmount().subtract(total);
                last.setAmount(last.getAmount().add(diff));
            }
        }

        // 8. 既存のAmountPieを削除し、新しいものに置き換え
        // Hibernateのorphan removalを適切に処理するため、既存のアイテムを個別に削除
        List<AmountPie> existingAmountPies = drawdown.getAmountPies();
        existingAmountPies.clear();
        
        // 新しいAmountPieを追加
        for (AmountPie newPie : newAmountPies) {
            newPie.setDrawdown(drawdown);
            existingAmountPies.add(newPie);
        }

        // 9. 更新されたドローダウンを保存
        Drawdown savedDrawdown = drawdownRepository.save(drawdown);

        // 10. 新しい投資額配分を適用
        updateInvestorAmounts(newAmountPies);

        return savedDrawdown;
    }

    /**
     * 編集可能状態かどうかを判定
     */
    private boolean canEdit(TransactionStatus status) {
        return status == TransactionStatus.PENDING || status == TransactionStatus.FAILED;
    }

    /**
     * Facilityの状態をDRAFTに戻す（必要な場合のみ）
     * 
     * 削除対象以外に他のDrawdownが存在しない場合のみ、FacilityをDRAFT状態に戻す
     */
    private void revertFacilityStateIfNeeded(Long facilityId, Long excludeDrawdownId) {
        // 削除対象以外のDrawdownが存在するかチェック
        List<Drawdown> otherDrawdowns = drawdownRepository.findByFacilityId(facilityId)
                .stream()
                .filter(d -> !d.getId().equals(excludeDrawdownId))
                .toList();
        
        // 他にDrawdownが無い場合のみ、FacilityをDRAFTに戻す
        if (otherDrawdowns.isEmpty()) {
            facilityService.revertToDraft(facilityId);
        }
    }

    /**
     * 投資家の投資額を元に戻す
     */
    private void revertInvestorAmounts(List<AmountPie> amountPies) {
        for (AmountPie amountPie : amountPies) {
            Investor investor = investorRepository.findById(amountPie.getInvestorId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Investor not found with id: " + amountPie.getInvestorId()));
            
            Money investmentAmount = Money.of(amountPie.getAmount());
            investor.decreaseInvestmentAmount(investmentAmount);
            investorRepository.save(investor);
        }
    }

    private void validateDrawdownRequest(CreateDrawdownRequest request) {
        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Facility not found with id: " + request.getFacilityId()));

        if (!borrowerRepository.existsById(request.getBorrowerId())) {
            throw new ResourceNotFoundException("Borrower not found with id: " + request.getBorrowerId());
        }

        // 金額の妥当性チェック
        Money amount = Money.of(request.getAmount());
        if (amount.isZero() || !amount.isPositiveOrZero()) {
            throw new BusinessRuleViolationException("Drawdown amount must be positive");
        }

        // Facilityの利用可能残高チェック（簡易版）
        Money facilityCommitment = facility.getCommitment();
        if (amount.isGreaterThan(facilityCommitment)) {
            throw new BusinessRuleViolationException("Drawdown amount exceeds facility commitment");
        }

        // 金利の妥当性チェック
        Percentage interestRate = Percentage.of(request.getAnnualInterestRate());
        if (interestRate.getValue().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("Interest rate cannot be negative");
        }

        // 返済期間の妥当性チェック
        if (request.getRepaymentPeriodMonths() <= 0) {
            throw new BusinessRuleViolationException("Repayment period must be positive");
        }
    }

    private Loan createLoan(CreateDrawdownRequest request) {
        return new Loan(
                request.getFacilityId(),
                request.getBorrowerId(),
                Money.of(request.getAmount()),
                Percentage.of(request.getAnnualInterestRate()),
                request.getDrawdownDate(),
                request.getRepaymentPeriodMonths(),
                RepaymentCycle.valueOf(request.getRepaymentCycle()),
                request.getRepaymentMethod(),
                request.getCurrency());
    }

    private void updateInvestorAmounts(List<AmountPie> amountPies) {
        for (AmountPie amountPie : amountPies) {
            Investor investor = investorRepository.findById(amountPie.getInvestorId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Investor not found with id: " + amountPie.getInvestorId()));
            
            Money investmentAmount = Money.of(amountPie.getAmount());
            investor.increaseInvestmentAmount(investmentAmount);
            investorRepository.save(investor);
        }
    }
}
