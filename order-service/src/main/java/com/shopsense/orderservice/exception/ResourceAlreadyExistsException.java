package com.shopsense.orderservice.exception;

public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException( String message ) {
        super(message);
    }
}