package com.r2s.core.exception;

import org.springframework.http.HttpStatus;

// [!] 404 NotFound
public class NotFoundException extends BaseException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
