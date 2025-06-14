package com.gamercommunity.genre.service;

import com.gamercommunity.exception.custom.DuplicateEntityException;
import com.gamercommunity.exception.custom.EntityNotFoundException;
import com.gamercommunity.genre.dto.GenreRequest;
import com.gamercommunity.genre.dto.GenreResponse;
import com.gamercommunity.genre.entity.Genre;
import com.gamercommunity.genre.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenreService {

    private final GenreRepository genreRepository;

    // 장르 추가
    @Transactional
    public GenreResponse createGenre(GenreRequest request) {
        // 중복 체크
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
    public void deleteGenre(Long genreId) {
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 장르 ID 없음"));

        genreRepository.delete(genre);
    }
}

