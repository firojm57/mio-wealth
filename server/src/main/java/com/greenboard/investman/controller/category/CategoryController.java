package com.greenboard.investman.controller.category;

import com.greenboard.investman.model.common.DomainType;
import com.greenboard.investman.service.category.CategoryService;
import com.greenboard.investman.vo.category.CategoryVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryVO>> getCategories(@RequestParam(required = false) DomainType domain) {
        log.info("Fetching financial categories for domain: {}", domain != null ? domain : "ALL");
        return ResponseEntity.ok(categoryService.getCategoriesByDomain(domain));
    }
}
