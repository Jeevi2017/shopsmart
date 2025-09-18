package com.shopsmart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CustomerHasActiveOrdersException extends RuntimeException {

    public CustomerHasActiveOrdersException(String message) {
        super(message);
    }

    public CustomerHasActiveOrdersException(String message, Throwable cause) {
        super(message, cause);
    }
}
