package com.shopsense.catalogservice.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException( String message ) {
        super(message);
    }
}
