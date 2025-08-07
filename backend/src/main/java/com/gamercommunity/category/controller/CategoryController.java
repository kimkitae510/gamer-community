package com.gamercommunity.category.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.category.dto.CategoryCreateRequest;
import com.gamercommunity.category.dto.CategoryResponse;
import com.gamercommunity.category.dto.CategoryUpdateGenreRequest;
import com.gamercommunity.category.dto.CategoryUpdateNameRequest;
import com.gamercommunity.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 부모 카테고리 생성
    @PostMapping("/parents")
    public ResponseEntity<CategoryResponse> createParentCategory(@RequestBody @Valid CategoryCreateRequest request) {
        String loginId = SecurityUtil.getRequiredLoginId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createParentCategory(request, loginId));
    }

    // 자식 카테고리 생성
    @PostMapping("/parents/{parentId}/children")
    public ResponseEntity<CategoryResponse> createChildCategory(
            @PathVariable Long parentId,
            @RequestBody @Valid CategoryCreateRequest request) {
        String loginId = SecurityUtil.getRequiredLoginId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createChildCategory(request, parentId, loginId));
    }

    // 부모 카테고리 목록 조회 (게임기종별: PS5, 닌텐도 등)
    @GetMapping("/parents")
    public ResponseEntity<List<CategoryResponse>> getParentCategories() {
        return ResponseEntity.ok(categoryService.findParents());
    }

    // 자식 카테고리 목록 조회 (게임 목록)
    @GetMapping("/parents/{parentId}/children")
    public ResponseEntity<List<CategoryResponse>> getChildCategories(@PathVariable Long parentId) {
        return ResponseEntity.ok(categoryService.findChild(parentId));
    }

    // 장르별 카테고리 목록 조회
    @GetMapping("/parents/{parentId}/genres/{genreId}")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByGenre(
            @PathVariable Long parentId,
            @PathVariable Long genreId) {
        return ResponseEntity.ok(categoryService.getCategoriesByGenre(parentId, genreId));
    }

    // 자식 카테고리 이름 수정
    @PatchMapping("/children/{categoryId}/name")
    public ResponseEntity<CategoryResponse> updateChildCategoryName(
            @PathVariable Long categoryId,
            @RequestBody @Valid CategoryUpdateNameRequest request) {
        String loginId = SecurityUtil.getRequiredLoginId();
        return ResponseEntity.ok(categoryService.updateChildCategoryName(categoryId, request, loginId));
    }

    // 자식 카테고리 장르 수정
    @PatchMapping("/children/{categoryId}/genres")
    public ResponseEntity<CategoryResponse> updateChildCategoryGenre(
            @PathVariable Long categoryId,
            @RequestBody @Valid CategoryUpdateGenreRequest request) {
        String loginId = SecurityUtil.getRequiredLoginId();
        return ResponseEntity.ok(categoryService.updateChildCategoryGenre(categoryId, request, loginId));
    }

    // 자식 카테고리 이미지 교체
    @PatchMapping("/children/{categoryId}/image")
    public ResponseEntity<String> updateChildCategoryImage(
            @PathVariable Long categoryId,
            @RequestParam("image") MultipartFile imageFile) {
        String loginId = SecurityUtil.getRequiredLoginId();
        String newImageUrl = categoryService.replaceChildCategoryImage(categoryId, imageFile, loginId);
        return ResponseEntity.ok(newImageUrl);
    }

    // 자식 카테고리 삭제
    @DeleteMapping("/children/{categoryId}")
    public ResponseEntity<Void> deleteChildCategory(@PathVariable Long categoryId) {
        String loginId = SecurityUtil.getRequiredLoginId();
        categoryService.deleteChildrenCategory(categoryId, loginId);
        return ResponseEntity.noContent().build();
    }

    // 단일 자식 카테고리 조회
    @GetMapping("/children/{childId}")
    public ResponseEntity<CategoryResponse> getChildCategory(@PathVariable Long childId) {
        return ResponseEntity.ok(categoryService.findChildById(childId));
    }

    // 신설 게시판 목록 조회 (최대 10개)
    @GetMapping("/new")
    public ResponseEntity<List<CategoryResponse>> getNewCategories() {
        return ResponseEntity.ok(categoryService.getNewCategories());
    }

    // 부모 카테고리별 게임 목록 조회 (정렬 + 페이징)
    @GetMapping("/parents/{parentId}/sorted")
    public ResponseEntity<Page<CategoryResponse>> getCategoriesWithSort(
            @PathVariable Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "latest") String sortBy) {
        return ResponseEntity.ok(categoryService.getCategoriesWithSort(parentId, page, 4, sortBy));
    }

    // 장르별 카테고리 목록 조회 (정렬 + 페이징)
    @GetMapping("/parents/{parentId}/genres/{genreId}/sorted")
    public ResponseEntity<Page<CategoryResponse>> getCategoriesByGenreWithSort(
            @PathVariable Long parentId,
            @PathVariable Long genreId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "latest") String sortBy) {
        return ResponseEntity.ok(categoryService.getCategoriesByGenreWithSort(parentId, genreId, page, 4, sortBy));
    }
}
