package com.greenboard.investman.service.investment;

import com.greenboard.investman.model.category.FinancialCategory;
import com.greenboard.investman.model.investment.Investment;
import com.greenboard.investman.repository.category.FinancialCategoryRepository;
import com.greenboard.investman.repository.investment.InvestmentRepository;
import com.greenboard.investman.service.investment.impl.InvestmentServiceImpl;
import com.greenboard.investman.service.tag.TagService;
import com.greenboard.investman.vo.investment.InvestmentRequestVO;
import com.greenboard.investman.vo.investment.InvestmentSummaryVO;
import com.greenboard.investman.vo.investment.InvestmentVO;
import com.greenboard.investman.vo.investment.UpdatePercentageRequestVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.greenboard.investman.model.common.DomainType;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceValuationTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private FinancialCategoryRepository categoryRepository;

    @Mock
    private TagService tagService;

    private InvestmentService investmentService;

    @BeforeEach
    void setUp() {
        investmentService = new InvestmentServiceImpl(investmentRepository, categoryRepository, tagService);
    }

    @Test
    void testCreateInvestmentCalculatesPositiveUnrealizedValuation() {
        FinancialCategory category = FinancialCategory.builder().code("STOCKS").name("Stocks").domain(DomainType.INVESTMENT).build();
        when(categoryRepository.findById("STOCKS")).thenReturn(Optional.of(category));

        when(investmentRepository.save(any(Investment.class))).thenAnswer(inv -> {
            Investment item = inv.getArgument(0);
            item.setId("inv-1");
            return item;
        });

        InvestmentRequestVO request = InvestmentRequestVO.builder()
                .assetName("Nifty Index")
                .categoryCode("STOCKS")
                .buyingPrice(100000.0)
                .quantity(10)
                .unitPrice(10000.0)
                .currentPercentageChange(15.0)
                .tags("Index, Long-term")
                .build();

        InvestmentVO result = investmentService.createInvestment(request);

        assertNotNull(result);
        assertEquals(100000.0, result.getBuyingPrice());
        assertEquals(15.0, result.getCurrentPercentageChange());
        assertEquals(115000.0, result.getCurrentValue());
        assertEquals(15000.0, result.getUnrealizedProfitLoss());
        assertFalse(result.isSold());

        verify(tagService, times(1)).ensureTagsExist("Index, Long-term", "INVESTMENT");
    }

    @Test
    void testCreateInvestmentCalculatesNegativeUnrealizedValuation() {
        FinancialCategory category = FinancialCategory.builder().code("STOCKS").name("Stocks").domain(DomainType.INVESTMENT).build();
        when(categoryRepository.findById("STOCKS")).thenReturn(Optional.of(category));

        when(investmentRepository.save(any(Investment.class))).thenAnswer(inv -> {
            Investment item = inv.getArgument(0);
            item.setId("inv-2");
            return item;
        });

        InvestmentRequestVO request = InvestmentRequestVO.builder()
                .assetName("Midcap Fund")
                .categoryCode("STOCKS")
                .buyingPrice(50000.0)
                .currentPercentageChange(-10.0)
                .build();

        InvestmentVO result = investmentService.createInvestment(request);

        assertEquals(50000.0, result.getBuyingPrice());
        assertEquals(-10.0, result.getCurrentPercentageChange());
        assertEquals(45000.0, result.getCurrentValue());
        assertEquals(-5000.0, result.getUnrealizedProfitLoss());
    }

    @Test
    void testUpdateCurrentPercentageUpdatesHolding() {
        Investment existing = Investment.builder()
                .id("inv-123")
                .assetName("Tech Stock")
                .buyingPrice(20000.0)
                .percentChange(0.0)
                .isSold(false)
                .investmentDate(LocalDateTime.now())
                .build();

        when(investmentRepository.findById("inv-123")).thenReturn(Optional.of(existing));
        when(investmentRepository.save(any(Investment.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdatePercentageRequestVO req = UpdatePercentageRequestVO.builder().percentChange(25.0).build();
        InvestmentVO updated = investmentService.updateCurrentPercentage("inv-123", req);

        assertEquals(25.0, updated.getPercentChange());
        assertEquals(25000.0, updated.getCurrentValue());
        assertEquals(5000.0, updated.getUnrealizedProfitLoss());
    }

    @Test
    void testGetInvestmentSummaryComputesAccurateMetrics() {
        FinancialCategory stocks = FinancialCategory.builder().code("STOCKS").name("Stocks").domain(DomainType.INVESTMENT).build();
        FinancialCategory crypto = FinancialCategory.builder().code("CRYPTO").name("Crypto").domain(DomainType.INVESTMENT).build();

        Investment inv1 = Investment.builder()
                .id("inv-1")
                .assetName("Stock A")
                .category(stocks)
                .buyingPrice(100000.0)
                .percentChange(18.0)
                .isSold(false)
                .build();

        Investment inv2 = Investment.builder()
                .id("inv-2")
                .assetName("Crypto B")
                .category(crypto)
                .buyingPrice(50000.0)
                .percentChange(10.0)
                .isSold(false)
                .build();

        when(investmentRepository.findAll()).thenReturn(List.of(inv1, inv2));

        InvestmentSummaryVO summary = investmentService.getInvestmentSummary();

        assertNotNull(summary);
        assertEquals(150000.0, summary.getTotalInvested());
        assertEquals(173000.0, summary.getCurrentPortfolioValue());
        assertEquals(23000.0, summary.getUnrealizedProfitLoss());
        assertEquals(15.33, summary.getUnrealizedProfitLossPercentage());
        assertEquals(0.0, summary.getRealizedProfitLoss());
        assertEquals(0.0, summary.getRealizedProfitLossPercentage());
        assertEquals(2, summary.getTotalHoldingsCount());
        assertEquals(2, summary.getActiveHoldingsCount());
        assertEquals(0, summary.getSoldHoldingsCount());
    }

    @Test
    void testSummarySeparatesRealizedAndUnrealizedProfitLoss() {
        // Active holding with +10% gain
        Investment active = Investment.builder()
                .id("inv-active")
                .assetName("Active Stock")
                .buyingPrice(100000.0)
                .percentChange(10.0)
                .isSold(false)
                .build();

        // Sold holding bought for 50k, sold for 60k (+10k profit, +20%)
        Investment sold = Investment.builder()
                .id("inv-sold")
                .assetName("Sold Stock")
                .buyingPrice(50000.0)
                .sellingPrice(60000.0)
                .isSold(true)
                .build();

        when(investmentRepository.findAll()).thenReturn(List.of(active, sold));

        InvestmentSummaryVO summary = investmentService.getInvestmentSummary();

        assertNotNull(summary);
        assertEquals(150000.0, summary.getTotalInvested());
        assertEquals(170000.0, summary.getCurrentPortfolioValue());
        assertEquals(10000.0, summary.getUnrealizedProfitLoss());
        assertEquals(10.0, summary.getUnrealizedProfitLossPercentage());
        assertEquals(10000.0, summary.getRealizedProfitLoss());
        assertEquals(20.0, summary.getRealizedProfitLossPercentage());
        assertEquals(2, summary.getTotalHoldingsCount());
        assertEquals(1, summary.getActiveHoldingsCount());
        assertEquals(1, summary.getSoldHoldingsCount());
    }

    @Test
    void testSummaryHandlesNegativeValuationsAndSoldLosses() {
        // Active holding with -20% valuation
        Investment active = Investment.builder()
                .id("inv-loss-active")
                .assetName("Loss Stock")
                .buyingPrice(100000.0)
                .percentChange(-20.0)
                .isSold(false)
                .build();

        // Sold holding bought for 50k, sold for 40k (-10k deficit, -20%)
        Investment sold = Investment.builder()
                .id("inv-loss-sold")
                .assetName("Loss Realized")
                .buyingPrice(50000.0)
                .sellingPrice(40000.0)
                .isSold(true)
                .build();

        when(investmentRepository.findAll()).thenReturn(List.of(active, sold));

        InvestmentSummaryVO summary = investmentService.getInvestmentSummary();

        assertNotNull(summary);
        assertEquals(150000.0, summary.getTotalInvested());
        assertEquals(120000.0, summary.getCurrentPortfolioValue());
        assertEquals(-20000.0, summary.getUnrealizedProfitLoss());
        assertEquals(-20.0, summary.getUnrealizedProfitLossPercentage());
        assertEquals(-10000.0, summary.getRealizedProfitLoss());
        assertEquals(-20.0, summary.getRealizedProfitLossPercentage());
    }

    @Test
    void testSummaryWithZeroInvestments() {
        when(investmentRepository.findAll()).thenReturn(List.of());

        InvestmentSummaryVO summary = investmentService.getInvestmentSummary();

        assertNotNull(summary);
        assertEquals(0.0, summary.getTotalInvested());
        assertEquals(0.0, summary.getCurrentPortfolioValue());
        assertEquals(0.0, summary.getUnrealizedProfitLoss());
        assertEquals(0.0, summary.getUnrealizedProfitLossPercentage());
        assertEquals(0.0, summary.getRealizedProfitLoss());
        assertEquals(0.0, summary.getRealizedProfitLossPercentage());
        assertEquals(0, summary.getTotalHoldingsCount());
    }

    @Test
    void testGetInvestmentsComputesAllocationPercentage() {
        FinancialCategory stocks = FinancialCategory.builder().code("STOCKS").name("Stocks").domain(DomainType.INVESTMENT).build();

        Investment inv1 = Investment.builder()
                .id("inv-1")
                .assetName("Stock A")
                .category(stocks)
                .buyingPrice(75000.0)
                .percentChange(10.0)
                .isSold(false)
                .build();

        Investment inv2 = Investment.builder()
                .id("inv-2")
                .assetName("Stock B")
                .category(stocks)
                .buyingPrice(25000.0)
                .percentChange(0.0)
                .isSold(false)
                .build();

        when(investmentRepository.findAllByOrderByInvestmentDateDesc()).thenReturn(List.of(inv1, inv2));

        List<InvestmentVO> list = investmentService.getInvestments();

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(75.0, list.get(0).getAllocationPercentage());
        assertEquals(25.0, list.get(1).getAllocationPercentage());
    }

    @Test
    void testCreateInvestmentPrioritizesNonZeroPercentChangeWhenBothSupplied() {
        FinancialCategory category = FinancialCategory.builder().code("STOCKS").name("Stocks").domain(DomainType.INVESTMENT).build();
        when(categoryRepository.findById("STOCKS")).thenReturn(Optional.of(category));

        when(investmentRepository.save(any(Investment.class))).thenAnswer(inv -> {
            Investment item = inv.getArgument(0);
            item.setId("inv-3");
            return item;
        });

        // Simulating the bug scenario: percentChange is 12.5 and currentPercentageChange was 0, or vice versa
        InvestmentRequestVO request = InvestmentRequestVO.builder()
                .assetName("Growth Fund")
                .categoryCode("STOCKS")
                .buyingPrice(10000.0)
                .percentChange(12.5)
                .currentPercentageChange(0.0)
                .build();

        InvestmentVO result = investmentService.createInvestment(request);

        assertEquals(12.5, result.getPercentChange());
        assertEquals(11250.0, result.getCurrentValue());
        assertEquals(1250.0, result.getUnrealizedProfitLoss());
    }
}
