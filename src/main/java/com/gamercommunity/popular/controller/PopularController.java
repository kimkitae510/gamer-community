package com.gamercommunity.popular.controller;

import com.gamercommunity.popular.dto.TrendingPostResponse;
import com.gamercommunity.popular.service.PopularScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/popular")
@RequiredArgsConstructor
public class PopularController {

    private final PopularScoreService popularScoreService;


    //실시간 인기글 목록 조회
    @GetMapping("/trending")
    public ResponseEntity<List<TrendingPostResponse>> getTrendingPosts() {
        List<TrendingPostResponse> trendingPosts = popularScoreService.getTrendingPosts();
        return ResponseEntity.ok(trendingPosts);
    }

    //실시간 인기글 상위 10개 조회
    @GetMapping("/trending/top10")
    public ResponseEntity<List<TrendingPostResponse>> getTop10TrendingPosts() {
        List<TrendingPostResponse> trendingPosts = popularScoreService.getTop10TrendingPosts();
        return ResponseEntity.ok(trendingPosts);
    }
}
