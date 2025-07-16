package com.gamercommunity.news.repository;

import com.gamercommunity.global.enums.Platform;
import com.gamercommunity.news.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    // 중복 체크
    boolean existsByOriginalUrl(String originalUrl);

    // 플랫폼별 뉴스 조회
    List<News> findByPlatformOrderByPublishedAtDesc(@Param("platform") Platform platform);

    // 전체 뉴스 조회
    List<News> findAllByOrderByPublishedAtDesc();

    // 최신 뉴스 조회
    @Query("SELECT n FROM News n ORDER BY n.publishedAt DESC")
    List<News> findTopNews(@Param("limit") int limit);

}
