package com.greenboard.investman.service.investment;

import com.greenboard.investman.model.category.FinancialCategory;
import com.greenboard.investman.model.investment.Investment;
import com.greenboard.investman.repository.category.FinancialCategoryRepository;
import com.greenboard.investman.repository.investment.InvestmentRepository;
import com.greenboard.investman.service.investment.impl.InvestmentServiceImpl;
import com.greenboard.investman.service.tag.TagService;
import com.greenboard.investman.vo.investment.InvestmentRequestVO;
import com.greenboard.investman.vo.investment.InvestmentVO;
import com.greenboard.investman.vo.investment.UpdatePercentageRequestVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
                .currentPercentageChange(0.0)
                .isSold(false)
                .investmentDate(LocalDateTime.now())
                .build();

        when(investmentRepository.findById("inv-123")).thenReturn(Optional.of(existing));
        when(investmentRepository.save(any(Investment.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdatePercentageRequestVO req = UpdatePercentageRequestVO.builder().currentPercentageChange(25.0).build();
        InvestmentVO updated = investmentService.updateCurrentPercentage("inv-123", req);

        assertEquals(25.0, updated.getCurrentPercentageChange());
        assertEquals(25000.0, updated.getCurrentValue());
        assertEquals(5000.0, updated.getUnrealizedProfitLoss());
    }
}
