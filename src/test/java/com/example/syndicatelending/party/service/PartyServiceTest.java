package com.example.syndicatelending.party.service;

import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.party.dto.*;
import com.example.syndicatelending.party.entity.*;
import com.example.syndicatelending.party.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
                                null, BigDecimal.valueOf(1000000), CreditRating.AA);
                Borrower savedBorrower = new Borrower(
                                "Test Borrower", "test@example.com", "123-456-7890",
                                null, BigDecimal.valueOf(1000000), CreditRating.AA);

                when(borrowerRepository.save(any(Borrower.class))).thenReturn(savedBorrower);

                Borrower result = partyService.createBorrower(request);

                assertNotNull(result);
                assertEquals("Test Borrower", result.getName());
                assertEquals("test@example.com", result.getEmail());
                assertEquals(BigDecimal.valueOf(1000000), result.getCreditLimit());
                verify(borrowerRepository).save(any(Borrower.class));
        }

        @Test
        void 企業IDが指定された借り手作成時に企業存在確認を行う() {
                String companyId = "1";
                CreateBorrowerRequest request = new CreateBorrowerRequest(
                                "Test Borrower", "test@example.com", "123-456-7890",
                                companyId, BigDecimal.ZERO, CreditRating.AA);

                when(companyRepository.existsById(1L)).thenReturn(true);
                Borrower savedBorrower = new Borrower(
                                "Test Borrower", "test@example.com", "123-456-7890",
                                companyId, BigDecimal.ZERO, CreditRating.AA);
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
                                companyId, BigDecimal.ZERO, CreditRating.AA);

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
                                null, new BigDecimal("60000000"), CreditRating.AA); // AAの上限は50000000
                // creditLimitOverrideはデフォルトfalse
                assertThrows(com.example.syndicatelending.common.application.exception.BusinessRuleViolationException.class,
                                () -> partyService.createBorrower(request));
                verify(borrowerRepository, never()).save(any(Borrower.class));
        }

        @Test
        void creditLimitOverrideがtrueなら上限超過でも登録できる() {
                CreateBorrowerRequest request = new CreateBorrowerRequest(
                                "Test Borrower", "test@example.com", "123-456-7890",
                                null, new BigDecimal("60000000"), CreditRating.AA);
                request.setCreditLimitOverride(true);
                Borrower savedBorrower = new Borrower(
                                "Test Borrower", "test@example.com", "123-456-7890",
                                null, new BigDecimal("60000000"), CreditRating.AA);
                when(borrowerRepository.save(any(Borrower.class))).thenReturn(savedBorrower);
                Borrower result = partyService.createBorrower(request);
                assertNotNull(result);
                assertEquals(new BigDecimal("60000000"), result.getCreditLimit());
                verify(borrowerRepository).save(any(Borrower.class));
        }
}
