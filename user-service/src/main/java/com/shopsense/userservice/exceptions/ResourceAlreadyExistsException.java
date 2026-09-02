package com.shopsense.userservice.exceptions;

public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException( String message ) {
        super(message);
    }
}
