package com.gamercommunity.global.exception.custom;

public class AiUsageLimitExceededException extends RuntimeException {
    public AiUsageLimitExceededException(String message) {
        super(message);
    }
}
