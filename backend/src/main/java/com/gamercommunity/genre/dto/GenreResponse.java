package com.gamercommunity.genre.dto;

import com.gamercommunity.genre.entity.Genre;

public record GenreResponse(
        Long id,
        String name
) {
    public static GenreResponse from(Genre genre) {
        return new GenreResponse(
                genre.getId(),
                genre.getName()
        );
    }
}
