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


}
