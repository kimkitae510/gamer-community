package com.gamercommunity.category.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.category.dto.CategoryRequest;
import com.gamercommunity.category.dto.CategoryResponse;
import com.gamercommunity.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        String loginId = SecurityUtil.getRequiredLoginId();
        return ResponseEntity.ok(categoryService.createParentCategory(request, loginId));
    }


    // 자식 카테고리 생성
    @PostMapping("/parents/{parentId}/children")
    public ResponseEntity<CategoryResponse> createChildCategory(@PathVariable Long parentId,  @RequestBody CategoryRequest categoryRequest) {
        String loginId = SecurityUtil.getRequiredLoginId();
        categoryRequest.setParentId(parentId);
        return ResponseEntity.ok(categoryService.createChildCategory(categoryRequest, loginId));
    }

    // 게임기종별(ps5,닌텐도,엑스박스 등) 부모 카테고리 목록 조회
    @GetMapping("/parents")
    public ResponseEntity<List<CategoryResponse>> getParentCategories() {
        return ResponseEntity.ok(categoryService.findParents());
    }

    // 자식 카테고리 목록 조회 (게임 목록)
    @GetMapping("/{parentId}/children")
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

    // 자식 카테고리 이미지 파일 업로드
    @PutMapping("/children/{childCategoryId}/image/upload")
    public ResponseEntity<String> uploadChildCategoryImage(
            @PathVariable Long childCategoryId,
            @RequestParam("image") MultipartFile imageFile) {

        String loginId = SecurityUtil.getRequiredLoginId();
        String newImageUrl = categoryService.replaceChildCategoryImage(childCategoryId, imageFile, loginId);
        return ResponseEntity.ok(newImageUrl);
    }

    // 자식 카테고리 장르 수정
    @PutMapping("/{categoryId}/genres/update")
    public ResponseEntity<CategoryResponse> updateChildCategoryGenere(@PathVariable Long categoryId, @RequestBody CategoryRequest categoryRequest) {
        String loginId = SecurityUtil.getRequiredLoginId();
        return ResponseEntity.ok(categoryService.updateChildCategoryGenere(categoryId, categoryRequest, loginId));
    }

    // 자식 카테고리 이름 수정
    @PutMapping("/{categoryId}/name/update")
    public ResponseEntity<CategoryResponse> updateChildCategoryName(@PathVariable Long categoryId, @RequestBody CategoryRequest categoryRequest) {
        String loginId = SecurityUtil.getRequiredLoginId();
        return ResponseEntity.ok(categoryService.updateChildCategoryName(categoryId, categoryRequest, loginId));
    }

    // 자식카테고리 삭제
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteChildCategory(@PathVariable Long categoryId) {
        String loginId = SecurityUtil.getRequiredLoginId();
        categoryService.deleteChildrenCategory(categoryId, loginId);
        return ResponseEntity.ok("게시글이 삭제되었습니다.");
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
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sortBy) {
        return ResponseEntity.ok(categoryService.getCategoriesWithSort(parentId, page, size, sortBy));
    }

    // 장르별 카테고리 목록 조회 (정렬 + 페이징)
    @GetMapping("/parents/{parentId}/genres/{genreId}/sorted")
    public ResponseEntity<Page<CategoryResponse>> getCategoriesByGenreWithSort(
            @PathVariable Long parentId,
            @PathVariable Long genreId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sortBy) {
        return ResponseEntity.ok(categoryService.getCategoriesByGenreWithSort(parentId, genreId, page, size, sortBy));
    }

}
