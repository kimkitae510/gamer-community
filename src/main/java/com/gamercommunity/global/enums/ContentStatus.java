package com.gamercommunity.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentStatus {
    ACTIVE(1, "활성"),
    DELETED(0, "삭제됨");

    private final int code;
    private final String description;

    public boolean isDeleted() {
        return this == DELETED;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}
