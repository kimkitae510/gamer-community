package com.gamercommunity.genre.repository;

import com.gamercommunity.genre.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    Optional<Genre> findByName(String name);

    // 장르 이름 수정 (직접 UPDATE)
    @Modifying
    @Query("UPDATE Genre g SET g.name = :name WHERE g.id = :genreId")
    int updateName(@Param("genreId") Long genreId, @Param("name") String name);
}
