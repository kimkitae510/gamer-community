package com.gamercommunity.category.service;


import com.gamercommunity.category.dto.CategoryRequest;
import com.gamercommunity.category.dto.CategoryResponse;
import com.gamercommunity.category.entity.Category;
import com.gamercommunity.category.repository.CategoryRepository;
import com.gamercommunity.exception.custom.EntityNotFoundException;
import com.gamercommunity.exception.custom.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 부모 카테고리 생성
    @Transactional
    public CategoryResponse createParentCategory(CategoryRequest categoryRequest) {
        Category category = Category.builder()
                .name(categoryRequest.getName())
                .parent(null)
                .writable(categoryRequest.isWritable())
                .build();

        Category saved = categoryRepository.save(category);
        return CategoryResponse.fromParent(saved);
    }


    // 자식 카테고리 생성
    @Transactional
    public CategoryResponse createChildCategory(CategoryRequest categoryRequest) {
        Category parent = categoryRepository.findById(categoryRequest.getParentId())
                .orElseThrow(() -> new EntityNotFoundException("부모 카테고리를 찾을 수 없습니다. id=" + categoryRequest.getParentId()));

        if (parent.getParent() != null) {
            throw new InvalidRequestException("자식 카테고리에는 하위 카테고리를 생성할 수 없습니다.");
        }

        Category category = Category.builder()
                .name(categoryRequest.getName())
                .parent(parent)
                .writable(categoryRequest.isWritable())
                .build();

        Category saved = categoryRepository.save(category);
        return CategoryResponse.fromChild(saved);
    }

    // 게임기종별(ps5,닌텐도,엑스박스 등) 부모 카테고리 목록 조회
    @Transactional(readOnly = true)
    public List<CategoryResponse> findParents() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(CategoryResponse::fromParent)
                .toList();
    }

    // 자식 카테고리(게임기종별 게임들) 리스트 조회
    @Transactional(readOnly = true)
    public List<CategoryResponse> findChild(Long parentId) {
        List<Category> categories = categoryRepository.findByParentIdOrderByCreatedAtDesc(parentId);

        return categories.stream()
                .map(CategoryResponse::fromChild)
                .toList();
    }

}
