package com.gamercommunity.global.exception.custom;

public class NewsCollectionException extends RuntimeException {
    public NewsCollectionException(String message) {
        super(message);
    }

    public NewsCollectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
