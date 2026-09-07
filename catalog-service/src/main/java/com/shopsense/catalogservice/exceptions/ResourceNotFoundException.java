package com.shopsense.catalogservice.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException( String message ) {
        super(message);
    }
}
