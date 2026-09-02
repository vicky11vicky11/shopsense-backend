package com.shopsense.userservice.exceptions;

public class UserAccountSuspendedException extends RuntimeException {
    public UserAccountSuspendedException( String message ) {
        super(message);
    }
}
