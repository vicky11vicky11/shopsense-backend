package com.shopsense.userservice.exceptions;

public class UserAccountBlockedException extends RuntimeException {
    public UserAccountBlockedException( String message ) {
        super(message);
    }
}
