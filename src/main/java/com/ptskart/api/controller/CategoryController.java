package com.ptskart.api.controller;

import com.ptskart.api.dto.ApiResponse;
import com.ptskart.api.entity.Category;
import com.ptskart.api.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Categories retrieved successfully", categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(cat -> ResponseEntity.ok(ApiResponse.ok("Category found", cat)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody Category category) {
        if (category.getSlug() == null || category.getSlug().isBlank()) {
            category.setSlug(category.getName().toLowerCase().replace(" ", "-"));
        }
        Category saved = categoryRepository.save(category);
        return ResponseEntity.ok(ApiResponse.ok("Category created successfully", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable Long id, @RequestBody Category details) {
        return categoryRepository.findById(id).map(cat -> {
            cat.setName(details.getName());
            cat.setSlug(details.getSlug());
            cat.setDescription(details.getDescription());
            cat.setStatus(details.getStatus());
            cat.setIcon(details.getIcon());
            Category updated = categoryRepository.save(cat);
            return ResponseEntity.ok(ApiResponse.ok("Category updated successfully", updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted successfully"));
    }
}

