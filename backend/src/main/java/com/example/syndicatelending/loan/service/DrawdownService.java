package com.example.syndicatelending.loan.service;

import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.loan.dto.CreateDrawdownRequest;
import com.example.syndicatelending.loan.entity.Drawdown;
import com.example.syndicatelending.loan.entity.Loan;
import com.example.syndicatelending.loan.repository.DrawdownRepository;
import com.example.syndicatelending.loan.repository.LoanRepository;
import com.example.syndicatelending.party.repository.BorrowerRepository;
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
                request.getRepaymentCycle(),
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
