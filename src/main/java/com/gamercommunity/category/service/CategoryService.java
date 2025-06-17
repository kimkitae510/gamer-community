package com.gamercommunity.category.service;


import com.gamercommunity.category.dto.CategoryRequest;
import com.gamercommunity.category.dto.CategoryResponse;
import com.gamercommunity.category.entity.Category;
import com.gamercommunity.category.repository.CategoryRepository;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.global.exception.custom.InvalidRequestException;
import com.gamercommunity.genre.entity.Genre;
import com.gamercommunity.genre.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final GenreRepository genreRepository;

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

        Set<Genre> genres = validateAndGetGenres(categoryRequest.getGenreId());

        Category category = Category.builder()
                .name(categoryRequest.getName())
                .parent(parent)
                .genres(genres)
                .writable(categoryRequest.isWritable())
                .build();

        Category saved = categoryRepository.save(category);
        return CategoryResponse.fromChild(saved);
    }
    // 자식카테고리 생성 및 수정 할 때 장르 검증용도
    private Set<Genre> validateAndGetGenres(List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            throw new InvalidRequestException("장르는 최소 1개 이상 선택해야 합니다.");
        }

        List<Genre> foundGenres = genreRepository.findAllById(genreIds);

        if (foundGenres.size() != genreIds.size()) {
            throw new EntityNotFoundException("일부 장르를 찾을 수 없습니다.");
        }

        return new HashSet<>(foundGenres);
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

    // 장르별 카테고리 목록 조회
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByGenre(Long parentId, Long genreId) {

        categoryRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("부모 카테고리 ID가 존재하지 않음"));


        genreRepository.findById(genreId)
                .orElseThrow(() -> new EntityNotFoundException("장르가 존재하지 않음"));


        List<Category> categories = categoryRepository.findByParentIdAndGenreIdWithGenres(parentId, genreId);

        return categories.stream()
                .map(CategoryResponse::fromChild)
                .toList();
    }


}
