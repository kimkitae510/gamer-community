package com.gamercommunity.category.service;


import com.gamercommunity.aws.s3.service.S3Service;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final GenreRepository genreRepository;
    private final S3Service s3Service;

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

    // 자식 카테고리 장르 수정
    @Transactional
    public CategoryResponse updateChildCategoryGenere(Long categoryId, CategoryRequest categoryRequest) {
        Category category = findChildCategoryById(categoryId);

        // 장르 검증
        Set<Genre> genres = validateAndGetGenres(categoryRequest.getGenreId());
        category.updateGenres(genres);

        return CategoryResponse.fromChild(category);
    }

    // 자식 카테고리 이름 수정
    @Transactional
    public CategoryResponse updateChildCategoryName(Long categoryId, CategoryRequest categoryRequest) {
        Category category = findChildCategoryById(categoryId);
        category.updateName(categoryRequest.getName());

        return CategoryResponse.fromChild(category);
    }

    // 자식 카테고리 이미지 변경
    @Transactional
    public String replaceChildCategoryImage(Long categoryId, MultipartFile newImageFile) {
        Category category = findChildCategoryById(categoryId);

        String oldImageUrl = category.getImageUrl();

        // 기존 이미지 삭제
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            s3Service.deleteFile(oldImageUrl);
        }

        // 새 이미지 업로드
        String newImageUrl = s3Service.uploadFile(newImageFile, "category-images");

        category.changeImageUrl(newImageUrl);
        categoryRepository.save(category);

        return newImageUrl;
    }



    // =============================== 헬퍼 메서드 ==========================================================================

    private Category findChildCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("카테고리", categoryId));

        if (category.getParent() == null) {
            throw new InvalidRequestException("부모 카테고리는 수정할 수 없습니다.");
        }

        return category;
    }


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

}
