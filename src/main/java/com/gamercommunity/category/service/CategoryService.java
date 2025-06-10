package com.gamercommunity.category.service;


import com.gamercommunity.category.dto.CategoryRequest;
import com.gamercommunity.category.dto.CategoryResponse;
import com.gamercommunity.category.entity.Category;
import com.gamercommunity.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new IllegalArgumentException("부모 카테고리를 찾을 수 없습니다. id=" + categoryRequest.getParentId()));

        if (parent.getParent() != null) {
            throw new IllegalArgumentException("자식 카테고리에는 하위 카테고리를 생성할 수 없습니다.");
        }

        Category category = Category.builder()
                .name(categoryRequest.getName())
                .parent(parent)
                .writable(categoryRequest.isWritable())
                .build();

        Category saved = categoryRepository.save(category);
        return CategoryResponse.fromChild(saved);
    }

}
