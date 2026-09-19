package com.greenboard.investman.controller.investment;

import com.greenboard.investman.service.investment.InvestmentService;
import com.greenboard.investman.vo.investment.InvestmentRequestVO;
import com.greenboard.investman.vo.investment.InvestmentVO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/investments")
public class InvestmentController {

    private static final Logger log = LoggerFactory.getLogger(InvestmentController.class);

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @GetMapping
    public ResponseEntity<List<InvestmentVO>> getInvestments(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Fetching investments for user '{}'", principal.getName());
        return ResponseEntity.ok(investmentService.getInvestmentsForUser(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<InvestmentVO> createInvestment(Principal principal,
                                                         @Valid @RequestBody InvestmentRequestVO request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Creating investment for user '{}' (action: {}, amount: {})", principal.getName(), request.getAction(), request.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(investmentService.createInvestment(principal.getName(), request));
    }
}
