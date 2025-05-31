package com.example.syndicatelending.party.service;

import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.party.dto.*;
import com.example.syndicatelending.party.entity.*;
import com.example.syndicatelending.party.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
                "Test Company", "REG123", "Tech", "Tokyo", "Japan");
        Company savedCompany = new Company("Test Company", "REG123", "Tech", "Tokyo", "Japan");

        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

        Company result = partyService.createCompany(request);

        assertNotNull(result);
        assertEquals("Test Company", result.getCompanyName());
        assertEquals("REG123", result.getRegistrationNumber());
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void 企業IDで企業を取得できる() {
        String businessId = "test-company-id";
        Company company = new Company("Test Company", null, null, null, null);
        when(companyRepository.findByBusinessId(businessId)).thenReturn(Optional.of(company));

        Company result = partyService.getCompanyById(businessId);

        assertNotNull(result);
        assertEquals("Test Company", result.getCompanyName());
        verify(companyRepository).findByBusinessId(businessId);
    }

    @Test
    void 存在しない企業IDで例外が発生する() {
        String businessId = "non-existent-id";
        when(companyRepository.findByBusinessId(businessId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> partyService.getCompanyById(businessId)
        );

        assertTrue(exception.getMessage().contains(businessId));
    }

    @Test
    void 借り手を正常に作成できる() {
        CreateBorrowerRequest request = new CreateBorrowerRequest(
                "Test Borrower", "test@example.com", "123-456-7890",
                null, BigDecimal.valueOf(1000000), "AA");
        Borrower savedBorrower = new Borrower(
                "Test Borrower", "test@example.com", "123-456-7890",
                null, BigDecimal.valueOf(1000000), "AA");

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
        String companyId = "test-company-id";
        CreateBorrowerRequest request = new CreateBorrowerRequest(
                "Test Borrower", "test@example.com", "123-456-7890",
                companyId, BigDecimal.ZERO, "AA");

        when(companyRepository.existsByBusinessId(companyId)).thenReturn(true);
        Borrower savedBorrower = new Borrower(
                "Test Borrower", "test@example.com", "123-456-7890",
                companyId, BigDecimal.ZERO, "AA");
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(savedBorrower);

        Borrower result = partyService.createBorrower(request);

        assertNotNull(result);
        assertEquals(companyId, result.getCompanyId());
        verify(companyRepository).existsByBusinessId(companyId);
        verify(borrowerRepository).save(any(Borrower.class));
    }

    @Test
    void 存在しない企業IDの借り手作成で例外が発生する() {
        String companyId = "non-existent-company";
        CreateBorrowerRequest request = new CreateBorrowerRequest(
                "Test Borrower", "test@example.com", "123-456-7890",
                companyId, BigDecimal.ZERO, "AA");

        when(companyRepository.existsByBusinessId(companyId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> partyService.createBorrower(request)
        );

        assertTrue(exception.getMessage().contains(companyId));
        verify(companyRepository).existsByBusinessId(companyId);
        verify(borrowerRepository, never()).save(any(Borrower.class));
    }

    @Test
    void 投資家を正常に作成できる() {
        CreateInvestorRequest request = new CreateInvestorRequest(
                "Test Investor", "investor@example.com", "987-654-3210",
                null, BigDecimal.valueOf(5000000), "Bank");
        Investor savedInvestor = new Investor(
                "Test Investor", "investor@example.com", "987-654-3210",
                null, BigDecimal.valueOf(5000000), "Bank");

        when(investorRepository.save(any(Investor.class))).thenReturn(savedInvestor);

        Investor result = partyService.createInvestor(request);

        assertNotNull(result);
        assertEquals("Test Investor", result.getName());
        assertEquals("investor@example.com", result.getEmail());
        assertEquals(BigDecimal.valueOf(5000000), result.getInvestmentCapacity());
        assertEquals("Bank", result.getInvestorType());
        verify(investorRepository).save(any(Investor.class));
    }

    @Test
    void 全ての企業を取得できる() {
        List<Company> companies = List.of(
                new Company("Company 1", null, null, null, null),
                new Company("Company 2", null, null, null, null)
        );
        when(companyRepository.findAll()).thenReturn(companies);

        List<Company> result = partyService.getAllCompanies();

        assertEquals(2, result.size());
        assertEquals("Company 1", result.get(0).getCompanyName());
        assertEquals("Company 2", result.get(1).getCompanyName());
        verify(companyRepository).findAll();
    }

    @Test
    void アクティブな投資家のみ取得できる() {
        List<Investor> activeInvestors = List.of(
                new Investor("Investor 1", null, null, null, null, null),
                new Investor("Investor 2", null, null, null, null, null)
        );
        when(investorRepository.findByIsActiveTrue()).thenReturn(activeInvestors);

        List<Investor> result = partyService.getActiveInvestors();

        assertEquals(2, result.size());
        assertEquals("Investor 1", result.get(0).getName());
        assertEquals("Investor 2", result.get(1).getName());
        verify(investorRepository).findByIsActiveTrue();
    }
}