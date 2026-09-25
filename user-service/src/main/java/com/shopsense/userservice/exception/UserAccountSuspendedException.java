package com.shopsense.userservice.exception;

public class UserAccountSuspendedException extends RuntimeException {
    public UserAccountSuspendedException( String message ) {
        super(message);
    }
}
