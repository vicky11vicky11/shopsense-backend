package com.shopsense.userservice.exception;

public class UserAccountBlockedException extends RuntimeException {
    public UserAccountBlockedException( String message ) {
        super(message);
    }
}
