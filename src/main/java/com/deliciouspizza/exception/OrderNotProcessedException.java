package com.deliciouspizza.exception;

public class OrderNotProcessedException extends RuntimeException {

    public OrderNotProcessedException(String message) {
        super(message);
    }

    public OrderNotProcessedException(String message, Throwable cause) {
        super(message, cause);
    }

}
