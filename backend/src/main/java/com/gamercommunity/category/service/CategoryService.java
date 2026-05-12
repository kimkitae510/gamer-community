package com.gamercommunity.category.service;

import com.gamercommunity.aws.s3.service.S3Service;
import com.gamercommunity.category.dto.CategoryCreateRequest;
import com.gamercommunity.category.dto.CategoryResponse;
import com.gamercommunity.category.dto.CategoryUpdateGenreRequest;
import com.gamercommunity.category.dto.CategoryUpdateNameRequest;
import com.gamercommunity.category.entity.Category;
import com.gamercommunity.category.entity.CategoryGenre;
import com.gamercommunity.category.repository.CategoryGenreRepository;
import com.gamercommunity.category.repository.CategoryRepository;
import com.gamercommunity.genre.dto.GenreResponse;
import com.gamercommunity.genre.entity.Genre;
import com.gamercommunity.genre.repository.GenreRepository;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.global.exception.custom.InvalidRequestException;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryGenreRepository categoryGenreRepository;
    private final GenreRepository genreRepository;
    private final S3Service s3Service;
    private final UserRepository userRepository;

    // 부모 카테고리 생성
    @Transactional
    public CategoryResponse createParentCategory(CategoryCreateRequest request, String loginId) {
        checkLevel3Permission(loginId);

        Category category = Category.builder()
                .name(request.getName())
                .parent(null)
                .writable(request.isWritable())
                .build();

        return CategoryResponse.fromParent(categoryRepository.save(category));
    }

    // 자식 카테고리 생성
    @Transactional
    public CategoryResponse createChildCategory(CategoryCreateRequest request, Long parentId, String loginId) {
        checkLevel3Permission(loginId);

        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("부모 카테고리를 찾을 수 없습니다. id=" + parentId));

        if (parent.getParent() != null) {
            throw new InvalidRequestException("자식 카테고리에는 하위 카테고리를 생성할 수 없습니다.");
        }

        Category category = Category.builder()
                .name(request.getName())
                .parent(parent)
                .writable(request.isWritable())
                .build();

        Category saved = categoryRepository.save(category);

        categoryRepository.incrementChildCount(parent.getId());
        manageNewCategory(saved);

        Set<Genre> genres = validateAndGetGenres(request.getGenreId());
        for (Genre genre : genres) {
            categoryGenreRepository.save(CategoryGenre.builder()
                    .category(saved)
                    .genre(genre)
                    .build());
        }

        List<GenreResponse> genreResponses = genres.stream().map(GenreResponse::from).toList();
        return CategoryResponse.fromChild(saved, genreResponses);
    }

    // 부모 카테고리 목록 조회
    public List<CategoryResponse> findParents() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(CategoryResponse::fromParent)
                .toList();
    }

    // 자식 카테고리 목록 조회
    public List<CategoryResponse> findChild(Long parentId) {
        return toCategoryResponseList(categoryRepository.findByParentIdOrderByCreatedAtDesc(parentId));
    }

    // 장르별 카테고리 목록 조회
    public List<CategoryResponse> getCategoriesByGenre(Long parentId, Long genreId) {
        if (!categoryRepository.existsById(parentId)) {
            throw new EntityNotFoundException("부모 카테고리", parentId);
        }
        if (!genreRepository.existsById(genreId)) {
            throw new EntityNotFoundException("장르", genreId);
        }

        Pageable pageable = PageRequest.of(0, 100, Sort.by("createdAt").descending());
        return toCategoryResponseList(categoryRepository.findByParentIdAndGenreId(parentId, genreId, pageable).getContent());
    }

    // 자식 카테고리 이름 수정
    @Transactional
    public CategoryResponse updateChildCategoryName(Long categoryId, CategoryUpdateNameRequest request, String loginId) {
        checkLevel3Permission(loginId);
        Category category = findChildCategoryById(categoryId);

        categoryRepository.updateName(categoryId, request.getName());

        return toCategoryResponse(category);
    }

    // 자식 카테고리 장르 수정
    @Transactional
    public CategoryResponse updateChildCategoryGenre(Long categoryId, CategoryUpdateGenreRequest request, String loginId) {
        checkLevel3Permission(loginId);
        Category category = findChildCategoryById(categoryId);

        categoryGenreRepository.deleteByCategoryId(categoryId);

        Set<Genre> genres = validateAndGetGenres(request.getGenreId());
        for (Genre genre : genres) {
            categoryGenreRepository.save(CategoryGenre.builder()
                    .category(category)
                    .genre(genre)
                    .build());
        }

        List<GenreResponse> genreResponses = genres.stream().map(GenreResponse::from).toList();
        return CategoryResponse.fromChild(category, genreResponses);
    }

    // 자식 카테고리 이미지 교체
    @Transactional
    public String replaceChildCategoryImage(Long categoryId, MultipartFile newImageFile, String loginId) {
        checkLevel3Permission(loginId);
        Category category = findChildCategoryById(categoryId);

        String oldImageUrl = category.getImageUrl();
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            s3Service.deleteFile(oldImageUrl);
        }

        String newImageUrl = s3Service.uploadFile(newImageFile, "category-images");
        categoryRepository.updateImageUrl(categoryId, newImageUrl);

        return newImageUrl;
    }

    // 자식 카테고리 삭제
    @Transactional
    public void deleteChildrenCategory(Long categoryId, String loginId) {
        checkLevel3Permission(loginId);
        Category category = findChildCategoryById(categoryId);

        // 관련 데이터 먼저 삭제 (테이블 FK 제약조건 회피)
        categoryGenreRepository.deleteByCategoryId(categoryId);

        categoryRepository.decrementChildCount(category.getParent().getId());
        categoryRepository.delete(category);
    }

    // 단일 자식 카테고리 조회
    public CategoryResponse findChildById(Long childId) {
        Category category = categoryRepository.findById(childId)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));
        return toCategoryResponse(category);
    }

    // 신설 게시판 목록 조회
    public List<CategoryResponse> getNewCategories() {
        return toCategoryResponseList(
                categoryRepository.findNewCategories().stream().limit(10).toList()
        );
    }

    // 부모 카테고리별 게임 목록 (정렬 + 페이징)
    public Page<CategoryResponse> getCategoriesWithSort(Long parentId, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, resolveSort(sortBy));
        Page<Category> categoryPage = categoryRepository.findByParentId(parentId, pageable);
        return new PageImpl<>(toCategoryResponseList(categoryPage.getContent()), pageable, categoryPage.getTotalElements());
    }

    // 장르별 카테고리 목록 (정렬 + 페이징)
    public Page<CategoryResponse> getCategoriesByGenreWithSort(Long parentId, Long genreId, int page, int size, String sortBy) {
        if (!categoryRepository.existsById(parentId)) {
            throw new EntityNotFoundException("부모 카테고리", parentId);
        }
        if (!genreRepository.existsById(genreId)) {
            throw new EntityNotFoundException("장르", genreId);
        }

        Pageable pageable = PageRequest.of(page, size, resolveSort(sortBy));
        Page<Category> categoryPage = categoryRepository.findByParentIdAndGenreId(parentId, genreId, pageable);
        return new PageImpl<>(toCategoryResponseList(categoryPage.getContent()), pageable, categoryPage.getTotalElements());
    }

    // =============================== 헬퍼 메서드 ===============================

    private Sort resolveSort(String sortBy) {
        return switch (sortBy) {
            case "popular" -> Sort.by("postCount").descending();
            case "rating"  -> Sort.by("rating").descending();
            case "oldest"  -> Sort.by("createdAt").ascending();
            default        -> Sort.by("createdAt").descending();
        };
    }

    private void manageNewCategory(Category newCategory) {
        newCategory.markAsNew();
        if (categoryRepository.countNewCategories() > 10) {
            List<Category> oldest = categoryRepository.findOldestNewCategory();
            if (!oldest.isEmpty()) {
                oldest.get(0).unmarkAsNew();
            }
        }
    }

    private void checkLevel3Permission(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + loginId));
        if (!user.getGrade().isLevel3OrAbove()) {
            throw new AccessDeniedException("레벨 3 이상만 게시판을 관리할 수 있습니다.");
        }
    }

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

    private CategoryResponse toCategoryResponse(Category category) {
        List<Genre> genres = categoryGenreRepository.findGenresByCategoryId(category.getId());
        return CategoryResponse.fromChild(category, genres.stream().map(GenreResponse::from).toList());
    }

    private List<CategoryResponse> toCategoryResponseList(List<Category> categories) {
        if (categories.isEmpty()) return List.of();

        List<Long> categoryIds = categories.stream().map(Category::getId).toList();
        List<CategoryGenre> categoryGenres = categoryGenreRepository.findByCategoryIdIn(categoryIds);

        Map<Long, List<Genre>> genreMap = categoryGenres.stream()
                .collect(Collectors.groupingBy(
                        cg -> cg.getCategory().getId(),
                        Collectors.mapping(CategoryGenre::getGenre, Collectors.toList())
                ));

        return categories.stream()
                .map(cat -> CategoryResponse.fromChild(cat,
                        genreMap.getOrDefault(cat.getId(), List.of())
                                .stream().map(GenreResponse::from).toList()))
                .toList();
    }
}
