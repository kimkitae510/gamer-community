package com.gamercommunity.news.crawler;

import com.gamercommunity.news.entity.Platform;
import com.gamercommunity.news.entity.News;

import java.util.List;


public interface NewsCrawler {
    

    // 특정 플랫폼의 뉴스 수집
    List<News> crawl(Platform platform);
    
    //출처 이름
    String getSourceName();
    

    //플랫폼 목록
    List<Platform> getSupportedPlatforms();
}
