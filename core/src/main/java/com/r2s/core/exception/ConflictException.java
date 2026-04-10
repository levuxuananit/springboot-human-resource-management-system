package com.r2s.core.exception;

import org.springframework.http.HttpStatus;

// [?] 409 Conflict
public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
