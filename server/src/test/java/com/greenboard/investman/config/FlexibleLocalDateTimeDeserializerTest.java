package com.greenboard.investman.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.greenboard.investman.vo.investment.InvestmentRequestVO;
import com.greenboard.investman.vo.investment.MarkSoldRequestVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FlexibleLocalDateTimeDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());
        objectMapper.registerModule(module);
    }

    @Test
    void testDeserializeDateOnlyString() throws Exception {
        String json = "{\"assetName\":\"Bluechip\",\"categoryCode\":\"STOCKS\",\"buyingPrice\":50000.0,\"investmentDate\":\"2025-01-25\"}";
        InvestmentRequestVO request = objectMapper.readValue(json, InvestmentRequestVO.class);

        assertNotNull(request.getInvestmentDate());
        assertEquals(2025, request.getInvestmentDate().getYear());
        assertEquals(1, request.getInvestmentDate().getMonthValue());
        assertEquals(25, request.getInvestmentDate().getDayOfMonth());
        assertEquals(0, request.getInvestmentDate().getHour());
        assertEquals(0, request.getInvestmentDate().getMinute());
    }

    @Test
    void testDeserializeFullIsoString() throws Exception {
        String json = "{\"assetName\":\"Bluechip\",\"categoryCode\":\"STOCKS\",\"buyingPrice\":50000.0,\"investmentDate\":\"2025-01-25T14:30:00\"}";
        InvestmentRequestVO request = objectMapper.readValue(json, InvestmentRequestVO.class);

        assertNotNull(request.getInvestmentDate());
        assertEquals(14, request.getInvestmentDate().getHour());
        assertEquals(30, request.getInvestmentDate().getMinute());
    }

    @Test
    void testDeserializeMarkSoldWithDateOnly() throws Exception {
        String json = "{\"sellingPrice\":65000.0,\"soldDate\":\"2025-02-15\"}";
        MarkSoldRequestVO request = objectMapper.readValue(json, MarkSoldRequestVO.class);

        assertNotNull(request.getSoldDate());
        assertEquals(2025, request.getSoldDate().getYear());
        assertEquals(2, request.getSoldDate().getMonthValue());
        assertEquals(15, request.getSoldDate().getDayOfMonth());
    }
}
