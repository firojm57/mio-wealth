package com.greenboard.investman.service.investment.impl;

import com.greenboard.investman.model.investment.Investment;
import com.greenboard.investman.model.user.User;
import com.greenboard.investman.repository.investment.InvestmentRepository;
import com.greenboard.investman.repository.user.UserRepository;
import com.greenboard.investman.service.investment.InvestmentService;
import com.greenboard.investman.vo.investment.InvestmentRequestVO;
import com.greenboard.investman.vo.investment.InvestmentVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class InvestmentServiceImpl implements InvestmentService {

    private static final Logger log = LoggerFactory.getLogger(InvestmentServiceImpl.class);

    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;

    public InvestmentServiceImpl(InvestmentRepository investmentRepository, UserRepository userRepository) {
        this.investmentRepository = investmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestmentVO> getInvestmentsForUser(String userId) {
        List<Investment> investments = investmentRepository.findByUser_UserId(userId);

        if (investments.isEmpty()) {
            // Return baseline standard portfolio holdings for new users
            return getDefaultHoldings();
        }

        List<InvestmentVO> result = new ArrayList<>();
        for (Investment inv : investments) {
            result.add(InvestmentVO.builder()
                    .id(inv.getId())
                    .name(inv.getRemarks() != null ? inv.getRemarks() : "Asset Investment")
                    .symbol("INV-" + inv.getId())
                    .allocation("10%")
                    .shares(String.valueOf(inv.getQuantity()))
                    .price("$" + String.format("%.2f", inv.getAmount() / (inv.getQuantity() > 0 ? inv.getQuantity() : 1)))
                    .value("$" + String.format("%,.0f", inv.getAmount()))
                    .returnRate("+5.0%")
                    .up(true)
                    .build());
        }
        return result;
    }

    @Override
    @Transactional
    public InvestmentVO createInvestment(String userId, InvestmentRequestVO request) {
        User user = userRepository.findById(userId).orElse(null);

        Investment investment = new Investment();
        investment.setAmount(request.getAmount());
        investment.setQuantity(request.getQuantity());
        investment.setRemarks(request.getRemarks());
        investment.setAction(request.getAction() != null ? request.getAction() : "BUY");
        investment.setInvestmentDate(LocalDateTime.now());
        investment.setUser(user);

        Investment saved = investmentRepository.save(investment);

        return InvestmentVO.builder()
                .id(saved.getId())
                .name(saved.getRemarks() != null ? saved.getRemarks() : "Investment")
                .symbol("INV-" + saved.getId())
                .allocation("5%")
                .shares(String.valueOf(saved.getQuantity()))
                .price("$" + String.format("%.2f", saved.getAmount() / (saved.getQuantity() > 0 ? saved.getQuantity() : 1)))
                .value("$" + String.format("%,.0f", saved.getAmount()))
                .returnRate("+0.0%")
                .up(true)
                .build();
    }

    private List<InvestmentVO> getDefaultHoldings() {
        return Arrays.asList(
                InvestmentVO.builder().name("Apple Inc.").symbol("AAPL").allocation("18%").shares("120").price("$178.50").value("$21,420").returnRate("+12.4%").up(true).build(),
                InvestmentVO.builder().name("Microsoft Corp.").symbol("MSFT").allocation("12%").shares("85").price("$415.20").value("$35,292").returnRate("+8.7%").up(true).build(),
                InvestmentVO.builder().name("Vanguard S&P 500 ETF").symbol("VOO").allocation("15%").shares("110").price("$465.10").value("$51,161").returnRate("+5.3%").up(true).build(),
                InvestmentVO.builder().name("Tesla Inc.").symbol("TSLA").allocation("8%").shares("70").price("$175.40").value("$12,278").returnRate("-4.2%").up(false).build()
        );
    }
}
