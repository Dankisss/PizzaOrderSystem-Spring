package com.deliciouspizza.exception;

public class InvalidCountException extends RuntimeException {

    public InvalidCountException(String message) {
        super(message);
    }

    public InvalidCountException(String message, Throwable cause) {
        super(message, cause);
    }

}
