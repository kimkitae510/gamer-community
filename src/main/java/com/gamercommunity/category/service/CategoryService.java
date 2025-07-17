package com.gamercommunity.category.service;


import com.gamercommunity.aws.s3.service.S3Service;
import com.gamercommunity.category.dto.CategoryRequest;
import com.gamercommunity.category.dto.CategoryResponse;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
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
    public CategoryResponse createParentCategory(CategoryRequest categoryRequest, String loginId) {
        checkLevel3Permission(loginId);
        
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
    public CategoryResponse createChildCategory(CategoryRequest categoryRequest, String loginId) {
        checkLevel3Permission(loginId);
        
        Category parent = categoryRepository.findById(categoryRequest.getParentId())
                .orElseThrow(() -> new EntityNotFoundException("부모 카테고리를 찾을 수 없습니다. id=" + categoryRequest.getParentId()));

        if (parent.getParent() != null) {
            throw new InvalidRequestException("자식 카테고리에는 하위 카테고리를 생성할 수 없습니다.");
        }

        // 카테고리 생성
        Category category = Category.builder()
                .name(categoryRequest.getName())
                .parent(parent)
                .writable(categoryRequest.isWritable())
                .build();

        Category saved = categoryRepository.save(category);


        Set<Genre> genres = validateAndGetGenres(categoryRequest.getGenreId());
        for (Genre genre : genres) {
            CategoryGenre categoryGenre = CategoryGenre.builder()
                    .category(saved)
                    .genre(genre)
                    .build();
            categoryGenreRepository.save(categoryGenre);
        }

        return toCategoryResponse(saved);
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
                .map(this::toCategoryResponse)
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
                .map(this::toCategoryResponse)
                .toList();
    }

    // 자식 카테고리 장르 수정
    @Transactional
    public CategoryResponse updateChildCategoryGenere(Long categoryId, CategoryRequest categoryRequest, String loginId) {
        checkLevel3Permission(loginId);
        
        Category category = findChildCategoryById(categoryId);

        categoryGenreRepository.deleteByCategoryId(categoryId);

        Set<Genre> genres = validateAndGetGenres(categoryRequest.getGenreId());
        for (Genre genre : genres) {
            CategoryGenre categoryGenre = CategoryGenre.builder()
                    .category(category)
                    .genre(genre)
                    .build();
            categoryGenreRepository.save(categoryGenre);
        }

        return toCategoryResponse(category);
    }

    // 자식 카테고리 이름 수정
    @Transactional
    public CategoryResponse updateChildCategoryName(Long categoryId, CategoryRequest categoryRequest, String loginId) {
        checkLevel3Permission(loginId);
        
        Category category = findChildCategoryById(categoryId);
        category.updateName(categoryRequest.getName());

        return toCategoryResponse(category);
    }

    // 자식 카테고리 이미지 변경
    @Transactional
    public String replaceChildCategoryImage(Long categoryId, MultipartFile newImageFile, String loginId) {
        checkLevel3Permission(loginId);
        
        Category category = findChildCategoryById(categoryId);

        String oldImageUrl = category.getImageUrl();

        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            s3Service.deleteFile(oldImageUrl);
        }

        String newImageUrl = s3Service.uploadFile(newImageFile, "category-images");

        category.changeImageUrl(newImageUrl);
        categoryRepository.save(category);

        return newImageUrl;
    }

    // 자식 카테고리 삭제
    @Transactional
    public void deleteChildrenCategory(Long categoryId, String loginId) {
        checkLevel3Permission(loginId);
        
        Category category = findChildCategoryById(categoryId);

        categoryRepository.delete(category);
    }

    // 단일 자식 카테고리 조회
    @Transactional(readOnly = true)
    public CategoryResponse findChildById(Long childId) {
        Category category = categoryRepository.findById(childId)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));

        return toCategoryResponse(category);
    }



    // =============================== 헬퍼 메서드 ==========================================================================

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
        List<GenreResponse> genreResponses = genres.stream()
                .map(GenreResponse::from)
                .collect(Collectors.toList());
        
        return CategoryResponse.fromChild(category, genreResponses);
    }

}
