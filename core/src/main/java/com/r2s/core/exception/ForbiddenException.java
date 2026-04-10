package com.r2s.core.exception;

import org.springframework.http.HttpStatus;

//[?] 403 Forbindden
public class ForbiddenException extends BaseException {
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
