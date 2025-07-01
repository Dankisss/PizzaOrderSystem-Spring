package com.deliciouspizza.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super("Product with a property not found: " + message);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
