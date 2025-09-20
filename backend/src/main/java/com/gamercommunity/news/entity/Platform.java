package com.gamercommunity.news.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Platform {
    PLAYSTATION("플레이스테이션"),
    NINTENDO("닌텐도"),
    XBOX("엑스박스"),
    PC("PC"),
    MOBILE("모바일");

    private final String displayName;
}
