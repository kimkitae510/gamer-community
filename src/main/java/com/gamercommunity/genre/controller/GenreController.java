package com.gamercommunity.genre.controller;

import com.gamercommunity.auth.util.SecurityUtil;
import com.gamercommunity.genre.dto.GenreRequest;
import com.gamercommunity.genre.dto.GenreResponse;
import com.gamercommunity.genre.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    // 장르 생성
    @PostMapping
    public ResponseEntity<GenreResponse> createGenre(@RequestBody @Valid GenreRequest request) {
        String loginId = SecurityUtil.getRequiredLoginId();
        GenreResponse response = genreService.createGenre(request, loginId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 장르 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
        String loginId = SecurityUtil.getRequiredLoginId();
        genreService.deleteGenre(id, loginId);
        return ResponseEntity.noContent().build();
    }

    // 장르 수정
    @PutMapping("/{id}")
    public ResponseEntity<GenreResponse> updateGenre(
            @PathVariable Long id,
            @RequestBody @Valid GenreRequest request) {
        String loginId = SecurityUtil.getRequiredLoginId();
        GenreResponse response = genreService.updateGenre(id, request, loginId);
        return ResponseEntity.ok(response);
    }

    // 장르 리스트
    @GetMapping
    public ResponseEntity<List<GenreResponse>> getAllGenres() {
        List<GenreResponse> genres = genreService.getAllGenres();
        return ResponseEntity.ok(genres);
    }
}
