package com.gamercommunity.news.controller;

import com.gamercommunity.global.enums.Platform;
import com.gamercommunity.news.dto.NewsResponse;
import com.gamercommunity.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    // 플랫폼별 뉴스 조회
    @GetMapping("/platform/{platform}")
    public ResponseEntity<List<NewsResponse>> getNewsByPlatform(
            @PathVariable Platform platform) {
        return ResponseEntity.ok(newsService.getNewsByPlatform(platform));
    }
}
