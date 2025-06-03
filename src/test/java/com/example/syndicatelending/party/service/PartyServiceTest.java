package com.example.syndicatelending.party.service;

import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.party.dto.*;
import com.example.syndicatelending.party.entity.*;
import com.example.syndicatelending.party.repository.*;
import com.example.syndicatelending.common.domain.model.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PartyService の単体テスト。
 */
@ExtendWith(MockitoExtension.class)
class PartyServiceTest {

        @Mock
        private CompanyRepository companyRepository;

        @Mock
        private BorrowerRepository borrowerRepository;

        @Mock
        private InvestorRepository investorRepository;

        private PartyService partyService;

        @BeforeEach
        void setUp() {
                partyService = new PartyService(companyRepository, borrowerRepository, investorRepository);
        }

        @Test
        void 企業を正常に作成できる() {
                CreateCompanyRequest request = new CreateCompanyRequest(
                                "Test Company", "REG123", Industry.IT, "Tokyo", Country.JAPAN);
                Company savedCompany = new Company("Test Company", "REG123", Industry.IT, "Tokyo", Country.JAPAN);

                when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

                Company result = partyService.createCompany(request);

                assertNotNull(result);
                assertEquals("Test Company", result.getCompanyName());
                assertEquals("REG123", result.getRegistrationNumber());
                verify(companyRepository).save(any(Company.class));
        }

        @Test
        void 企業IDで企業を取得できる() {
                Long id = 1L;
                Company company = new Company("Test Company", null, null, null, null);
                when(companyRepository.findById(id)).thenReturn(Optional.of(company));

                Company result = partyService.getCompanyById(id);

                assertNotNull(result);
                assertEquals("Test Company", result.getCompanyName());
                verify(companyRepository).findById(id);
        }

        @Test
        void 存在しない企業IDで例外が発生する() {
                Long id = 999L;
                when(companyRepository.findById(id)).thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> partyService.getCompanyById(id));

                assertTrue(exception.getMessage().contains(String.valueOf(id)));
        }

        @Test
        void 借り手を正常に作成できる() {
                CreateBorrowerRequest request = new CreateBorrowerRequest(
                                "Test Borrower", "test@example.com", "123-456-7890",
                                null, Money.of(1000000), CreditRating.AA);
                Borrower savedBorrower = new Borrower(
                                "Test Borrower", "test@example.com", "123-456-7890",
                                null, Money.of(1000000), CreditRating.AA);

                when(borrowerRepository.save(any(Borrower.class))).thenReturn(savedBorrower);

                Borrower result = partyService.createBorrower(request);

                assertNotNull(result);
                assertEquals("Test Borrower", result.getName());
                assertEquals("test@example.com", result.getEmail());
                assertEquals(Money.of(1000000), result.getCreditLimit());
                verify(borrowerRepository).save(any(Borrower.class));
        }

        @Test
        void 企業IDが指定された借り手作成時に企業存在確認を行う() {
                String companyId = "1";
                CreateBorrowerRequest request = new CreateBorrowerRequest(
                                "Test Borrower", "test@example.com", "123-456-7890",
                                companyId, Money.zero(), CreditRating.AA);

                when(companyRepository.existsById(1L)).thenReturn(true);
                Borrower savedBorrower = new Borrower(
                                "Test Borrower", "test@example.com", "123-456-7890",
                                companyId, Money.zero(), CreditRating.AA);
                when(borrowerRepository.save(any(Borrower.class))).thenReturn(savedBorrower);

                Borrower result = partyService.createBorrower(request);

                assertNotNull(result);
                assertEquals(companyId, result.getCompanyId());
                verify(companyRepository).existsById(1L);
                verify(borrowerRepository).save(any(Borrower.class));
        }

        @Test
        void 存在しない企業IDの借り手作成で例外が発生する() {
                String companyId = "999";
                CreateBorrowerRequest request = new CreateBorrowerRequest(
                                "Test Borrower", "test@example.com", "123-456-7890",
                                companyId, Money.zero(), CreditRating.AA);

                when(companyRepository.existsById(999L)).thenReturn(false);

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> partyService.createBorrower(request));

                assertTrue(exception.getMessage().contains(companyId));
                verify(companyRepository).existsById(999L);
                verify(borrowerRepository, never()).save(any(Borrower.class));
        }

        @Test
        void 投資家を正常に作成できる() {
                CreateInvestorRequest request = new CreateInvestorRequest(
                                "Test Investor", "investor@example.com", "987-654-3210",
                                null, BigDecimal.valueOf(5000000), InvestorType.BANK);
                Investor savedInvestor = new Investor(
                                "Test Investor", "investor@example.com", "987-654-3210",
                                null, BigDecimal.valueOf(5000000), InvestorType.BANK);

                when(investorRepository.save(any(Investor.class))).thenReturn(savedInvestor);

                Investor result = partyService.createInvestor(request);

                assertNotNull(result);
                assertEquals("Test Investor", result.getName());
                assertEquals("investor@example.com", result.getEmail());
                assertEquals(BigDecimal.valueOf(5000000), result.getInvestmentCapacity());
                assertEquals(InvestorType.BANK, result.getInvestorType());
                verify(investorRepository).save(any(Investor.class));
        }

        @Test
        void 全ての企業を取得できる() {
                List<Company> companies = List.of(
                                new Company("Company 1", null, Industry.OTHER, null, Country.OTHER),
                                new Company("Company 2", null, Industry.OTHER, null, Country.OTHER));
                Pageable pageable = PageRequest.of(0, 10);
                Page<Company> companyPage = new PageImpl<>(companies, pageable, companies.size());
                when(companyRepository.findAll(pageable)).thenReturn(companyPage);

                Page<Company> result = partyService.getAllCompanies(pageable);

                assertEquals(2, result.getTotalElements());
                assertEquals("Company 1", result.getContent().get(0).getCompanyName());
                assertEquals("Company 2", result.getContent().get(1).getCompanyName());
                verify(companyRepository).findAll(pageable);
        }

        @Test
        void アクティブな投資家のみ取得できる() {
                List<Investor> activeInvestors = List.of(
                                new Investor("Investor 1", null, null, null, null, InvestorType.BANK),
                                new Investor("Investor 2", null, null, null, null, InvestorType.BANK));
                Pageable pageable = PageRequest.of(0, 10);
                Page<Investor> investorPage = new PageImpl<>(activeInvestors, pageable, activeInvestors.size());
                when(investorRepository.findAll(
                                ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Investor>>any(),
                                eq(pageable))).thenReturn(investorPage);

                Page<Investor> result = partyService.getActiveInvestors(pageable);

                assertEquals(2, result.getTotalElements());
                assertEquals("Investor 1", result.getContent().get(0).getName());
                assertEquals("Investor 2", result.getContent().get(1).getName());
                verify(investorRepository).findAll(
                                ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Investor>>any(),
                                eq(pageable));
        }

        @Test
        void creditLimit上限を超えると例外が発生する() {
                CreateBorrowerRequest request = new CreateBorrowerRequest(
                                "Test Borrower", "test@example.com", "123-456-7890",
                                null, Money.of(60000000), CreditRating.AA); // AAの上限は50000000
                // creditLimitOverrideはデフォルトfalse
                assertThrows(com.example.syndicatelending.common.application.exception.BusinessRuleViolationException.class,
                                () -> partyService.createBorrower(request));
                verify(borrowerRepository, never()).save(any(Borrower.class));
        }

        @Test
        void creditLimitOverrideがtrueなら上限超過でも登録できる() {
                CreateBorrowerRequest request = new CreateBorrowerRequest(
                                "Test Borrower", "test@example.com", "123-456-7890",
                                null, Money.of(60000000), CreditRating.AA);
                request.setCreditLimitOverride(true);
                Borrower savedBorrower = new Borrower(
                                "Test Borrower", "test@example.com", "123-456-7890",
                                null, Money.of(60000000), CreditRating.AA);
                when(borrowerRepository.save(any(Borrower.class))).thenReturn(savedBorrower);
                Borrower result = partyService.createBorrower(request);
                assertNotNull(result);
                assertEquals(Money.of(60000000), result.getCreditLimit());
                verify(borrowerRepository).save(any(Borrower.class));
        }

        // Update Tests
        @Test
        void 企業を正常に更新できる() {
                Long companyId = 1L;
                UpdateCompanyRequest request = new UpdateCompanyRequest(
                                "Updated Company", "REG123", Industry.FINANCE, "Osaka", Country.JAPAN, 1L);
                Company existingCompany = new Company("Old Company", "REG123", Industry.IT, "Tokyo", Country.JAPAN);
                existingCompany.setId(companyId);
                existingCompany.setVersion(1L);
                Company updatedCompany = new Company("Updated Company", "REG123", Industry.FINANCE, "Osaka",
                                Country.JAPAN);
                updatedCompany.setId(companyId);
                updatedCompany.setVersion(2L);

                when(companyRepository.findById(companyId)).thenReturn(Optional.of(existingCompany));
                when(companyRepository.save(any(Company.class))).thenReturn(updatedCompany);

                Company result = partyService.updateCompany(companyId, request);

                assertNotNull(result);
                assertEquals("Updated Company", result.getCompanyName());
                assertEquals(2L, result.getVersion());
                verify(companyRepository).findById(companyId);
                verify(companyRepository).save(any(Company.class));
        }

        @Test
        void 存在しない企業を更新しようとした場合は例外が発生する() {
                Long companyId = 999L;
                UpdateCompanyRequest request = new UpdateCompanyRequest(
                                "Updated Company", "REG123", Industry.FINANCE, "Osaka", Country.JAPAN, 1L);

                when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> partyService.updateCompany(companyId, request));

                assertTrue(exception.getMessage().contains("Company not found"));
                verify(companyRepository).findById(companyId);
                verify(companyRepository, never()).save(any(Company.class));
        }

        @Test
        void 借り手を正常に更新できる() {
                Long borrowerId = 1L;
                UpdateBorrowerRequest request = new UpdateBorrowerRequest(
                                "Updated Borrower", "updated@example.com", "987-654-3210",
                                null, Money.of(2000000), CreditRating.A, 1L);
                Borrower existingBorrower = new Borrower(
                                "Old Borrower", "old@example.com", "123-456-7890",
                                null, Money.of(1000000), CreditRating.AA);
                existingBorrower.setId(borrowerId);
                existingBorrower.setVersion(1L);
                Borrower updatedBorrower = new Borrower(
                                "Updated Borrower", "updated@example.com", "987-654-3210",
                                null, Money.of(2000000), CreditRating.A);
                updatedBorrower.setId(borrowerId);
                updatedBorrower.setVersion(2L);

                when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.of(existingBorrower));
                when(borrowerRepository.save(any(Borrower.class))).thenReturn(updatedBorrower);

                Borrower result = partyService.updateBorrower(borrowerId, request);

                assertNotNull(result);
                assertEquals("Updated Borrower", result.getName());
                assertEquals(2L, result.getVersion());
                verify(borrowerRepository).findById(borrowerId);
                verify(borrowerRepository).save(any(Borrower.class));
        }

        @Test
        void 存在しない借り手を更新しようとした場合は例外が発生する() {
                Long borrowerId = 999L;
                UpdateBorrowerRequest request = new UpdateBorrowerRequest(
                                "Updated Borrower", "updated@example.com", "987-654-3210",
                                null, Money.of(2000000), CreditRating.A, 1L);

                when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> partyService.updateBorrower(borrowerId, request));

                assertTrue(exception.getMessage().contains("Borrower not found"));
                verify(borrowerRepository).findById(borrowerId);
                verify(borrowerRepository, never()).save(any(Borrower.class));
        }

        @Test
        void 投資家を正常に更新できる() {
                Long investorId = 1L;
                UpdateInvestorRequest request = new UpdateInvestorRequest(
                                "Updated Investor", "updated@example.com", "987-654-3210",
                                null, BigDecimal.valueOf(10000000), InvestorType.INSURANCE, 1L);
                Investor existingInvestor = new Investor(
                                "Old Investor", "old@example.com", "123-456-7890",
                                null, BigDecimal.valueOf(5000000), InvestorType.BANK);
                existingInvestor.setId(investorId);
                existingInvestor.setVersion(1L);
                Investor updatedInvestor = new Investor(
                                "Updated Investor", "updated@example.com", "987-654-3210",
                                null, BigDecimal.valueOf(10000000), InvestorType.INSURANCE);
                updatedInvestor.setId(investorId);
                updatedInvestor.setVersion(2L);

                when(investorRepository.findById(investorId)).thenReturn(Optional.of(existingInvestor));
                when(investorRepository.save(any(Investor.class))).thenReturn(updatedInvestor);

                Investor result = partyService.updateInvestor(investorId, request);

                assertNotNull(result);
                assertEquals("Updated Investor", result.getName());
                assertEquals(2L, result.getVersion());
                verify(investorRepository).findById(investorId);
                verify(investorRepository).save(any(Investor.class));
        }

        @Test
        void 存在しない投資家を更新しようとした場合は例外が発生する() {
                Long investorId = 999L;
                UpdateInvestorRequest request = new UpdateInvestorRequest(
                                "Updated Investor", "updated@example.com", "987-654-3210",
                                null, BigDecimal.valueOf(10000000), InvestorType.INSURANCE, 1L);

                when(investorRepository.findById(investorId)).thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> partyService.updateInvestor(investorId, request));

                assertTrue(exception.getMessage().contains("Investor not found"));
                verify(investorRepository).findById(investorId);
                verify(investorRepository, never()).save(any(Investor.class));
        }

        // Delete Tests
        @Test
        void 企業を正常に削除できる() {
                Long companyId = 1L;

                when(companyRepository.existsById(companyId)).thenReturn(true);

                partyService.deleteCompany(companyId);

                verify(companyRepository).existsById(companyId);
                verify(companyRepository).deleteById(companyId);
        }

        @Test
        void 存在しない企業を削除しようとした場合は例外が発生する() {
                Long companyId = 999L;

                when(companyRepository.existsById(companyId)).thenReturn(false);

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> partyService.deleteCompany(companyId));

                assertTrue(exception.getMessage().contains("Company not found"));
                verify(companyRepository).existsById(companyId);
                verify(companyRepository, never()).deleteById(companyId);
        }

        @Test
        void 借り手を正常に削除できる() {
                Long borrowerId = 1L;

                when(borrowerRepository.existsById(borrowerId)).thenReturn(true);

                partyService.deleteBorrower(borrowerId);

                verify(borrowerRepository).existsById(borrowerId);
                verify(borrowerRepository).deleteById(borrowerId);
        }

        @Test
        void 存在しない借り手を削除しようとした場合は例外が発生する() {
                Long borrowerId = 999L;

                when(borrowerRepository.existsById(borrowerId)).thenReturn(false);

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> partyService.deleteBorrower(borrowerId));

                assertTrue(exception.getMessage().contains("Borrower not found"));
                verify(borrowerRepository).existsById(borrowerId);
                verify(borrowerRepository, never()).deleteById(borrowerId);
        }

        @Test
        void 投資家を正常に削除できる() {
                Long investorId = 1L;

                when(investorRepository.existsById(investorId)).thenReturn(true);

                partyService.deleteInvestor(investorId);

                verify(investorRepository).existsById(investorId);
                verify(investorRepository).deleteById(investorId);
        }

        @Test
        void 存在しない投資家を削除しようとした場合は例外が発生する() {
                Long investorId = 999L;

                when(investorRepository.existsById(investorId)).thenReturn(false);

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> partyService.deleteInvestor(investorId));

                assertTrue(exception.getMessage().contains("Investor not found"));
                verify(investorRepository).existsById(investorId);
                verify(investorRepository, never()).deleteById(investorId);
        }

        // =====================================
        // 楽観的排他制御のテストケース
        // =====================================

        @Test
        void 企業を楽観的ロッキングで正常に更新できる() {
                Long companyId = 1L;
                UpdateCompanyRequest request = new UpdateCompanyRequest(
                                "Updated Company",
                                "REG123",
                                Industry.IT,
                                "Address",
                                Country.JAPAN,
                                1L);

                Company existingCompany = new Company("Original Company", "REG123", Industry.IT, "Address",
                                Country.JAPAN);
                existingCompany.setVersion(1L);

                Company updatedCompany = new Company("Updated Company", "REG123", Industry.IT, "Address",
                                Country.JAPAN);
                updatedCompany.setVersion(2L);

                when(companyRepository.findById(companyId)).thenReturn(Optional.of(existingCompany));
                when(companyRepository.save(any(Company.class))).thenReturn(updatedCompany);

                Company result = partyService.updateCompany(companyId, request);

                assertNotNull(result);
                assertEquals("Updated Company", result.getCompanyName());
                verify(companyRepository).findById(companyId);
                verify(companyRepository).save(any(Company.class));
        }

        @Test
        void 企業の楽観的ロッキングでバージョン不一致時に例外が発生する() {
                Long companyId = 1L;
                UpdateCompanyRequest request = new UpdateCompanyRequest(
                                "Updated Company",
                                "REG123",
                                Industry.IT,
                                "Address",
                                Country.JAPAN,
                                1L); // リクエストのバージョンは1

                Company existingCompany = new Company("Original Company", "REG123", Industry.IT, "Address",
                                Country.JAPAN);
                existingCompany.setVersion(2L); // 実際のバージョンは2（不一致）

                when(companyRepository.findById(companyId)).thenReturn(Optional.of(existingCompany));
                // Spring Data JPAのOptimisticLockingFailureExceptionをシミュレート
                when(companyRepository.save(any(Company.class)))
                                .thenThrow(new OptimisticLockingFailureException("Version mismatch"));

                OptimisticLockingFailureException exception = assertThrows(
                                OptimisticLockingFailureException.class,
                                () -> partyService.updateCompany(companyId, request));

                assertEquals("Version mismatch", exception.getMessage());
                verify(companyRepository).findById(companyId);
                verify(companyRepository).save(any(Company.class));
        }

        @Test
        void 借り手を楽観的ロッキングで正常に更新できる() {
                Long borrowerId = 1L;
                UpdateBorrowerRequest request = new UpdateBorrowerRequest(
                                "Test Borrower",
                                "borrower@test.com",
                                "+81-3-1234-5678",
                                null, // companyId
                                Money.of(new BigDecimal("2000000")),
                                CreditRating.AA,
                                1L); // version

                Borrower existingBorrower = new Borrower("Test Borrower", "borrower@test.com", "+81-3-1234-5678", null,
                                Money.of(new BigDecimal("1000000")), CreditRating.AA);
                existingBorrower.setVersion(1L);

                Borrower updatedBorrower = new Borrower("Test Borrower", "borrower@test.com", "+81-3-1234-5678", null,
                                Money.of(new BigDecimal("2000000")), CreditRating.AA);
                updatedBorrower.setVersion(2L);

                when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.of(existingBorrower));
                when(borrowerRepository.save(any(Borrower.class))).thenReturn(updatedBorrower);

                Borrower result = partyService.updateBorrower(borrowerId, request);

                assertNotNull(result);
                assertEquals(Money.of(new BigDecimal("2000000")), result.getCreditLimit());
                verify(borrowerRepository).findById(borrowerId);
                verify(borrowerRepository).save(any(Borrower.class));
        }

        @Test
        void 投資家を楽観的ロッキングで正常に更新できる() {
                Long investorId = 1L;
                UpdateInvestorRequest request = new UpdateInvestorRequest(
                                "Test Investor",
                                "investor@test.com",
                                "+81-3-1234-5678",
                                null, // companyId
                                BigDecimal.valueOf(5000000),
                                InvestorType.BANK,
                                1L); // version

                Investor existingInvestor = new Investor("Test Investor", "investor@test.com", "+81-3-1234-5678", null,
                                BigDecimal.valueOf(3000000), InvestorType.BANK);
                existingInvestor.setVersion(1L);

                Investor updatedInvestor = new Investor("Test Investor", "investor@test.com", "+81-3-1234-5678", null,
                                BigDecimal.valueOf(5000000), InvestorType.BANK);
                updatedInvestor.setVersion(2L);

                when(investorRepository.findById(investorId)).thenReturn(Optional.of(existingInvestor));
                when(investorRepository.save(any(Investor.class))).thenReturn(updatedInvestor);

                Investor result = partyService.updateInvestor(investorId, request);

                assertNotNull(result);
                assertEquals(BigDecimal.valueOf(5000000), result.getInvestmentCapacity());
                verify(investorRepository).findById(investorId);
                verify(investorRepository).save(any(Investor.class));
        }
}
