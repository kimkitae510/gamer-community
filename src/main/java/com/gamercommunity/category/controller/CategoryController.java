package com.gamercommunity.category.controller;

import com.gamercommunity.category.dto.CategoryRequest;
import com.gamercommunity.category.dto.CategoryResponse;
import com.gamercommunity.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    // 상위 카테고리 생성
    @PostMapping("/parents")
    public ResponseEntity<CategoryResponse> createParentCategory(@PathVariable Long parentId,
                                                                 @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.createParentCategory(request));
    }


    // 자식 카테고리 생성
    @PostMapping("/parents/{parentId}/children")
    public ResponseEntity<CategoryResponse> createChildCategory(@PathVariable Long parentId,  @RequestBody CategoryRequest categoryRequest) {
        categoryRequest.setParentId(parentId);
        return ResponseEntity.ok(categoryService.createChildCategory(categoryRequest));
    }
}
