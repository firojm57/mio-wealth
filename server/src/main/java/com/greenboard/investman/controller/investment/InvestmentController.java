package com.greenboard.investman.controller.investment;

import com.greenboard.investman.service.investment.InvestmentService;
import com.greenboard.investman.vo.investment.InvestmentRequestVO;
import com.greenboard.investman.vo.investment.InvestmentVO;
import com.greenboard.investman.vo.investment.MarkSoldRequestVO;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/investments")
@Tag(name = "2. Investments", description = "Full CRUD operations and liquidation lifecycle for tenant investments")
public class InvestmentController {

    private static final Logger log = LoggerFactory.getLogger(InvestmentController.class);

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @GetMapping
    @Operation(summary = "List Investments", description = "Retrieves all investment holdings in the active tenant's schema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of investments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT Bearer token required")
    })
    public ResponseEntity<List<InvestmentVO>> getInvestments() {
        log.info("Fetching investments");
        return ResponseEntity.ok(investmentService.getInvestments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Investment by ID", description = "Retrieves a single investment holding by its UUID from the tenant schema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Investment found"),
            @ApiResponse(responseCode = "400", description = "Investment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InvestmentVO> getInvestmentById(@PathVariable String id) {
        log.info("Fetching investment #{}", id);
        return ResponseEntity.ok(investmentService.getInvestmentById(id));
    }

    @PostMapping
    @Operation(summary = "Create Investment", description = "Records a new investment in the active tenant schema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Investment created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failure or invalid category code"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InvestmentVO> createInvestment(@Valid @RequestBody InvestmentRequestVO request) {
        log.info("Creating investment '{}' (category: {}, buyingPrice: {})", request.getAssetName(), request.getCategoryCode(), request.getBuyingPrice());
        return ResponseEntity.status(HttpStatus.CREATED).body(investmentService.createInvestment(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Investment", description = "Updates an existing investment holding in the active tenant schema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Investment updated successfully"),
            @ApiResponse(responseCode = "400", description = "Investment not found or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InvestmentVO> updateInvestment(@PathVariable String id,
                                                         @Valid @RequestBody InvestmentRequestVO request) {
        log.info("Updating investment #{} ('{}', buyingPrice: {})", id, request.getAssetName(), request.getBuyingPrice());
        return ResponseEntity.ok(investmentService.updateInvestment(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Investment", description = "Permanently deletes an investment holding by its UUID from the active tenant schema.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Investment deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Investment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteInvestment(@PathVariable String id) {
        log.info("Deleting investment #{}", id);
        investmentService.deleteInvestment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/sold")
    @Operation(summary = "Mark Investment as Sold", description = "Liquidation quick action recording selling price and sale date, calculating realized P&L.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Investment marked as sold successfully"),
            @ApiResponse(responseCode = "400", description = "Investment not found or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InvestmentVO> markAsSold(@PathVariable String id,
                                                   @Valid @RequestBody MarkSoldRequestVO request) {
        log.info("Marking investment #{} as sold for price {}", id, request.getSellingPrice());
        return ResponseEntity.ok(investmentService.markAsSold(id, request));
    }

    @PatchMapping("/{id}/percentage")
    @Operation(summary = "Update Investment Current % Change", description = "Quick action updating performance percentage change to dynamically derive unrealized valuation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current percentage change updated successfully"),
            @ApiResponse(responseCode = "400", description = "Investment not found or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InvestmentVO> updateCurrentPercentage(@PathVariable String id,
                                                                @Valid @RequestBody com.greenboard.investman.vo.investment.UpdatePercentageRequestVO request) {
        log.info("Updating % change for investment #{} to {}%", id, request.getCurrentPercentageChange());
        return ResponseEntity.ok(investmentService.updateCurrentPercentage(id, request));
    }
}
