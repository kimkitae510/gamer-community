package com.gamercommunity.genre.service;

import com.gamercommunity.exception.custom.DuplicateEntityException;
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
            throw new DuplicateEntityException("장르중복");
        }

        Genre genre = Genre.builder()
                .name(request.name())
                .build();

        Genre savedGenre = genreRepository.save(genre);
        return GenreResponse.from(savedGenre);
    }
}

