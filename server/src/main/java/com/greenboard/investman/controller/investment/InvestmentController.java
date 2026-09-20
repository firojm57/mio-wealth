package com.greenboard.investman.controller.investment;

import com.greenboard.investman.service.investment.InvestmentService;
import com.greenboard.investman.vo.investment.InvestmentRequestVO;
import com.greenboard.investman.vo.investment.InvestmentVO;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/investments")
@Tag(name = "2. Investments", description = "Full CRUD operations for tenant portfolio holdings, assets, and transactions")
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
    @Operation(summary = "Create Investment", description = "Records a new investment transaction in the active tenant schema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Investment created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failure or invalid category code"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InvestmentVO> createInvestment(@Valid @RequestBody InvestmentRequestVO request) {
        log.info("Creating investment (symbol: {}, amount: {})", request.getSymbol(), request.getAmount());
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
        log.info("Updating investment #{} (symbol: {}, amount: {})", id, request.getSymbol(), request.getAmount());
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
}
