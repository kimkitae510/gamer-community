package com.gamercommunity.global.exception.custom;

public class DuplicateEntityException extends RuntimeException {

    public DuplicateEntityException(String message) {
        super(message);
    }

    public DuplicateEntityException(String entity, String value) {
        super(String.format("이미 존재하는 %s입니다: %s", entity, value));
    }
}
