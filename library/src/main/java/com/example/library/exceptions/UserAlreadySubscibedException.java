package com.example.library.exceptions;

public class UserAlreadySubscibedException extends RuntimeException {
    public UserAlreadySubscibedException(String message) {
        super(message);
    }
}
