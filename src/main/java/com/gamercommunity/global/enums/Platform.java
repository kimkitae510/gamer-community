package com.gamercommunity.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Platform {
    PLAYSTATION("플레이스테이션"),
    XBOX("엑스박스"),
    NINTENDO("닌텐도"),
    STEAM("스팀"),
    PC("PC");

    private final String description;
}
