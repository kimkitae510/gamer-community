package com.gamercommunity.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentStatus {
    ACTIVE("활성"),
    DELETED("삭제됨");

    private final String description;

    public boolean isDeleted() {
        return this == DELETED;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}
