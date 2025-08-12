package com.gamercommunity.genre.service;

import com.gamercommunity.global.exception.custom.DuplicateEntityException;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.genre.dto.GenreRequest;
import com.gamercommunity.genre.dto.GenreResponse;
import com.gamercommunity.genre.entity.Genre;
import com.gamercommunity.genre.repository.GenreRepository;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenreService {

    private final GenreRepository genreRepository;
    private final UserRepository userRepository;

    // 장르 추가
    @Transactional
    public GenreResponse createGenre(GenreRequest request, String loginId) {
        checkLevel3Permission(loginId);

        if (genreRepository.findByName(request.name()).isPresent()) {
            throw new DuplicateEntityException("추가하려는 장르중복");
        }

        Genre genre = Genre.builder()
                .name(request.name())
                .build();

        Genre savedGenre = genreRepository.save(genre);
        return GenreResponse.from(savedGenre);
    }

    // 장르 삭제
    @Transactional
    public void deleteGenre(Long genreId, String loginId) {
        checkLevel3Permission(loginId);
        
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 장르 ID 없음"));

        genreRepository.delete(genre);
    }


    // 장르 수정
    @Transactional
    public GenreResponse updateGenre(Long genreId, GenreRequest request, String loginId) {
        checkLevel3Permission(loginId);
        
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new EntityNotFoundException("수정해야 할 장르 id 존재x"));

        // 다른 장르가 같은 이름을 사용하는지 체크
        genreRepository.findByName(request.name())
                .ifPresent(existingGenre -> {
                    if (!existingGenre.getId().equals(genreId)) {
                        throw new DuplicateEntityException("같은 이름의 장르 존재");
                    }
                });

        genre.updateName(request.name());
        return GenreResponse.from(genre);
    }

    // 모든 장르 리스트 조회
    public List<GenreResponse> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(GenreResponse::from)
                .collect(Collectors.toList());
    }
    
    // 권한 체크
    private void checkLevel3Permission(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + loginId));
        
        if (!user.getGrade().isLevel3OrAbove()) {
            throw new AccessDeniedException("레벨 3 이상만 장르를 관리할 수 있습니다.");
        }
    }
}

