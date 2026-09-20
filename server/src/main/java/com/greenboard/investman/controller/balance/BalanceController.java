package com.greenboard.investman.controller.balance;

import com.greenboard.investman.service.balance.AssetService;
import com.greenboard.investman.service.balance.LiabilityService;
import com.greenboard.investman.service.balance.PortfolioService;
import com.greenboard.investman.vo.balance.AssetItemVO;
import com.greenboard.investman.vo.balance.BalanceMetricsVO;
import com.greenboard.investman.vo.balance.BalanceSummaryVO;
import com.greenboard.investman.vo.balance.LiabilityItemVO;
import com.greenboard.investman.vo.balance.LiabilityRequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/balance")
@Tag(name = "3. Balance Sheet", description = "Portfolio balance metrics, asset breakdowns, and liability debt management")
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
    @Operation(summary = "Get Portfolio Metrics Summary", description = "Calculates total assets, total liabilities, net worth, and equity ratio for the active tenant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary metrics calculated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<BalanceMetricsVO> getBalanceSummary() {
        log.info("Fetching balance metrics");
        return ResponseEntity.ok(portfolioService.getSummaryMetrics());
    }

    @GetMapping("/assets")
    @Operation(summary = "Get Asset Holdings Breakdown", description = "Aggregates investment and saving assets from the active tenant's schema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of asset holdings"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<AssetItemVO>> getAssets() {
        log.info("Fetching assets");
        return ResponseEntity.ok(assetService.getAssets());
    }

    @GetMapping("/liabilities")
    @Operation(summary = "Get Liabilities Breakdown", description = "Retrieves all debt and loan obligations for the active tenant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of liabilities"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<LiabilityItemVO>> getLiabilities() {
        log.info("Fetching liabilities");
        return ResponseEntity.ok(liabilityService.getLiabilities());
    }

    @PostMapping("/liabilities")
    @Operation(summary = "Create Liability", description = "Records a new loan or liability obligation in the active tenant's schema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Liability created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<LiabilityItemVO> createLiability(@Valid @RequestBody LiabilityRequestVO request) {
        log.info("Creating liability (name: {}, amount: {})", request.getName(), request.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(liabilityService.createLiability(request));
    }

    @DeleteMapping("/liabilities/{id}")
    @Operation(summary = "Delete Liability", description = "Deletes a liability obligation by its UUID from the active tenant's schema.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Liability deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteLiability(@PathVariable String id) {
        log.info("Deleting liability '{}'", id);
        liabilityService.deleteLiability(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get Full Composite Balance", description = "Composite endpoint returning summary metrics, assets list, and liabilities list in a single response.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Composite balance summary"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<BalanceSummaryVO> getFullBalance() {
        log.info("Fetching full balance summary");
        return ResponseEntity.ok(portfolioService.getFullBalanceSummary());
    }
}
