package com.gamercommunity.global.exception.custom;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String entity, Long id) {
        super(String.format("%s를 찾을 수 없습니다. id=%d", entity, id));
    }

    public EntityNotFoundException(String entity, String identifier) {
        super(String.format("%s를 찾을 수 없습니다. %s", entity, identifier));
    }
}
