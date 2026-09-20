package com.greenboard.investman.controller.balance;

import com.greenboard.investman.service.balance.AssetService;
import com.greenboard.investman.service.balance.LiabilityService;
import com.greenboard.investman.service.balance.PortfolioService;
import com.greenboard.investman.vo.balance.AssetItemVO;
import com.greenboard.investman.vo.balance.BalanceMetricsVO;
import com.greenboard.investman.vo.balance.BalanceSummaryVO;
import com.greenboard.investman.vo.balance.LiabilityItemVO;
import com.greenboard.investman.vo.balance.LiabilityRequestVO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/balance")
public class BalanceController {

    private static final Logger log = LoggerFactory.getLogger(BalanceController.class);

    private final PortfolioService portfolioService;
    private final AssetService assetService;
    private final LiabilityService liabilityService;

    public BalanceController(PortfolioService portfolioService,
                             AssetService assetService,
                             LiabilityService liabilityService) {
        this.portfolioService = portfolioService;
        this.assetService = assetService;
        this.liabilityService = liabilityService;
    }

    @GetMapping("/summary")
    public ResponseEntity<BalanceMetricsVO> getBalanceSummary(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Fetching balance metrics for user '{}'", principal.getName());
        return ResponseEntity.ok(portfolioService.getSummaryMetrics(principal.getName()));
    }

    @GetMapping("/assets")
    public ResponseEntity<List<AssetItemVO>> getAssets(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Fetching assets for user '{}'", principal.getName());
        return ResponseEntity.ok(assetService.getAssetsForUser(principal.getName()));
    }

    @GetMapping("/liabilities")
    public ResponseEntity<List<LiabilityItemVO>> getLiabilities(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Fetching liabilities for user '{}'", principal.getName());
        return ResponseEntity.ok(liabilityService.getLiabilitiesForUser(principal.getName()));
    }

    @PostMapping("/liabilities")
    public ResponseEntity<LiabilityItemVO> createLiability(Principal principal,
                                                           @Valid @RequestBody LiabilityRequestVO request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Creating liability for user '{}' (name: {}, amount: {})",
                principal.getName(), request.getName(), request.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(liabilityService.createLiability(principal.getName(), request));
    }

    @DeleteMapping("/liabilities/{id}")
    public ResponseEntity<Void> deleteLiability(Principal principal, @PathVariable String id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Deleting liability '{}' for user '{}'", id, principal.getName());
        liabilityService.deleteLiability(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<BalanceSummaryVO> getFullBalance(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Fetching full balance summary for user '{}'", principal.getName());
        return ResponseEntity.ok(portfolioService.getFullBalanceSummary(principal.getName()));
    }
}
