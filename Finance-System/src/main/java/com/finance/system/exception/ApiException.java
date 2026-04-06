package com.finance.system.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;

import lombok.Getter;

import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@AllArgsConstructor
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public ApiException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
