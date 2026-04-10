package com.r2s.core.exception;

import org.springframework.http.HttpStatus;

// [!] 401 Unauthorized
public class UnauthenticatedException extends BaseException {
    public UnauthenticatedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
