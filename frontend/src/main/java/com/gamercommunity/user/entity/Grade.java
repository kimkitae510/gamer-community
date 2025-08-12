package com.gamercommunity.user.entity;

public enum Grade {
    LEVEL1(1),
    LEVEL2(2),
    LEVEL3(3);

    private final int level;

    Grade(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isLevel3OrAbove() {
        return this.level >= 3;
    }
}
