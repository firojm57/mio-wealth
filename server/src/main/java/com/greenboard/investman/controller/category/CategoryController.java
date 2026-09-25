package com.greenboard.investman.controller.category;

import com.greenboard.investman.model.common.DomainType;
import com.greenboard.investman.service.category.CategoryService;
import com.greenboard.investman.vo.category.CategoryRequestVO;
import com.greenboard.investman.vo.category.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "4. Category Catalog", description = "Tenant financial category catalog partitioned by domain")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "List Categories", description = "Retrieves financial categories for the active tenant, optionally filtered by domain.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryVO>> getCategories(
            @Parameter(description = "Optional financial domain filter (INVESTMENT, SAVING, EXPENSE, LIABILITY)")
            @RequestParam(required = false) DomainType domain) {
        log.info("Fetching financial categories for domain: {}", domain != null ? domain : "ALL");
        return ResponseEntity.ok(categoryService.getCategoriesByDomain(domain));
    }

    @PostMapping
    @Operation(summary = "Create Category", description = "Creates a new custom financial category in the active tenant schema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or code already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CategoryVO> createCategory(@Valid @RequestBody CategoryRequestVO request) {
        log.info("Creating custom category '{}' ({})", request.getCode(), request.getName());
        CategoryVO created = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
