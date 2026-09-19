package com.greenboard.investman.controller.balance;

import com.greenboard.investman.service.balance.PortfolioService;
import com.greenboard.investman.vo.balance.BalanceSummaryVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/balance")
public class BalanceController {

    private static final Logger log = LoggerFactory.getLogger(BalanceController.class);

    private final PortfolioService portfolioService;

    public BalanceController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public ResponseEntity<BalanceSummaryVO> getBalance(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Fetching balance summary for user '{}'", principal.getName());
        return ResponseEntity.ok(portfolioService.getBalanceSummary(principal.getName()));
    }
}
