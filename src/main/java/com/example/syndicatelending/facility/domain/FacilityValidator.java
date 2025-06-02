package com.example.syndicatelending.facility.domain;

import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import com.example.syndicatelending.facility.dto.CreateFacilityRequest;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.party.entity.Borrower;
import com.example.syndicatelending.party.entity.Investor;
import com.example.syndicatelending.party.repository.BorrowerRepository;
import com.example.syndicatelending.party.repository.InvestorRepository;
import com.example.syndicatelending.syndicate.entity.Syndicate;
import com.example.syndicatelending.syndicate.repository.SyndicateRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Facility作成時のバリデーションを集約するクラス
 * 外部依存を必要とするバリデーションロジックを一元管理
 */
@Component
public class FacilityValidator {

    private final SyndicateRepository syndicateRepository;
    // 将来の機能拡張のために保持
    @SuppressWarnings("unused")
    private final InvestorRepository investorRepository;
    @SuppressWarnings("unused")
    private final BorrowerRepository borrowerRepository;
    @SuppressWarnings("unused")
    private final FacilityRepository facilityRepository;

    public FacilityValidator(SyndicateRepository syndicateRepository,
            InvestorRepository investorRepository,
            BorrowerRepository borrowerRepository,
            FacilityRepository facilityRepository) {
        this.syndicateRepository = syndicateRepository;
        this.investorRepository = investorRepository;
        this.borrowerRepository = borrowerRepository;
        this.facilityRepository = facilityRepository;
    }

    /**
     * Facility作成リクエストの総合バリデーション
     */
    public void validateCreateFacilityRequest(CreateFacilityRequest request) {
        validateBasicInputs(request);
        validateSyndicateExists(request.getSyndicateId());

        Syndicate syndicate = syndicateRepository.findById(request.getSyndicateId())
                .orElseThrow(() -> new BusinessRuleViolationException("Syndicateが見つかりません"));

        validateInvestorsExistAndBelongToSyndicate(request, syndicate);
        validateSharePieDuplication(request);
        validateCreditLimit(request, syndicate);
        validateSharePiePercentage(request);
    }

    /**
     * 基本入力値のバリデーション
     */
    private void validateBasicInputs(CreateFacilityRequest request) {
        if (request.getCommitment() == null || request.getCommitment().getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("コミットメント金額は正の値である必要があります");
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BusinessRuleViolationException("開始日と終了日は必須です");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessRuleViolationException("開始日は終了日より前である必要があります");
        }

        if (request.getSharePies() == null || request.getSharePies().isEmpty()) {
            throw new BusinessRuleViolationException("SharePieは最低1つ必要です");
        }
    }

    /**
     * Syndicateの存在チェック
     */
    private void validateSyndicateExists(Long syndicateId) {
        if (!syndicateRepository.existsById(syndicateId)) {
            throw new BusinessRuleViolationException("指定されたSyndicateが存在しません: id=" + syndicateId);
        }
    }

    /**
     * Investorの存在とSyndicateメンバーシップチェック
     */
    private void validateInvestorsExistAndBelongToSyndicate(CreateFacilityRequest request, Syndicate syndicate) {
        for (CreateFacilityRequest.SharePieRequest pie : request.getSharePies()) {
            // Investor存在チェック
            Investor investor = investorRepository.findById(pie.getInvestorId())
                    .orElseThrow(() -> new BusinessRuleViolationException(
                            "指定されたInvestorが存在しません: id=" + pie.getInvestorId()));

            // アクティブ状態チェック
            if (!investor.getIsActive()) {
                throw new BusinessRuleViolationException(
                        "非アクティブなInvestorは投資できません: investorId=" + pie.getInvestorId());
            }

            // Syndicateメンバーシップチェック
            if (!syndicate.getMemberInvestorIds().contains(pie.getInvestorId())) {
                throw new BusinessRuleViolationException(
                        "InvestorはSyndicateメンバーではありません: investorId=" + pie.getInvestorId());
            }
        }
    }

    /**
     * SharePieの重複チェック
     */
    private void validateSharePieDuplication(CreateFacilityRequest request) {
        Set<Long> investorIds = new HashSet<>();
        for (CreateFacilityRequest.SharePieRequest pie : request.getSharePies()) {
            if (!investorIds.add(pie.getInvestorId())) {
                throw new BusinessRuleViolationException(
                        "同一のInvestorが複数のSharePieに含まれています: investorId=" + pie.getInvestorId());
            }
        }
    }

    /**
     * BorrowerのCreditLimitチェック
     */
    private void validateCreditLimit(CreateFacilityRequest request, Syndicate syndicate) {
        Borrower borrower = borrowerRepository.findById(syndicate.getBorrowerId())
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "指定されたSyndicateにBorrowerが関連付けられていません"));

        // 新規CommitmentがCreditLimitを超えていないかチェック
        if (borrower.getCreditLimit().getAmount().compareTo(request.getCommitment().getAmount()) < 0) {
            throw new BusinessRuleViolationException(
                    "FacilityのCommitment(" + request.getCommitment() +
                            ")がBorrowerのCreditLimit(" + borrower.getCreditLimit() + ")を超えています");
        }

        // 既存Facility合計 + 新規CommitmentがCreditLimit以下かチェック
        List<Facility> existingFacilities = facilityRepository.findBySyndicateId(request.getSyndicateId());
        Money totalExistingCommitment = existingFacilities.stream()
                .map(Facility::getCommitment)
                .reduce(Money.zero(), Money::add);

        Money totalCommitment = totalExistingCommitment.add(request.getCommitment());
        if (totalCommitment.getAmount().compareTo(borrower.getCreditLimit().getAmount()) > 0) {
            throw new BusinessRuleViolationException(
                    "総Commitment(" + totalCommitment +
                            ")がBorrowerのCreditLimit(" + borrower.getCreditLimit() + ")を超えています");
        }
    }

    /**
     * SharePieの合計が100%であることをチェック
     */
    private void validateSharePiePercentage(CreateFacilityRequest request) {
        Percentage totalPercentage = request.getSharePies().stream()
                .map(CreateFacilityRequest.SharePieRequest::getShare)
                .reduce(Percentage.of(BigDecimal.ZERO), Percentage::add);

        Percentage hundred = Percentage.of(BigDecimal.ONE);
        if (!hundred.equals(totalPercentage)) {
            throw new BusinessRuleViolationException("SharePieの合計は100%である必要があります。現在の合計: " + totalPercentage);
        }
    }
}
