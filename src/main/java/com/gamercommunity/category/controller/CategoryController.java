package com.gamercommunity.category.controller;

import com.gamercommunity.category.dto.CategoryRequest;
import com.gamercommunity.category.dto.CategoryResponse;
import com.gamercommunity.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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

    // 게임기종별(ps5,닌텐도,엑스박스 등) 부모 카테고리 목록 조회
    @GetMapping("/parents")
    public ResponseEntity<List<CategoryResponse>> getParentCategories() {
        return ResponseEntity.ok(categoryService.findParents());
    }

    // 장르별 카테고리 목록
    @GetMapping("/genres/{genreId}")
    public List<CategoryResponse> getCategoriesByGenre(
            @RequestParam Long parentId, @PathVariable Long genreId) {
        return categoryService.findChild(parentId);
    }

}
