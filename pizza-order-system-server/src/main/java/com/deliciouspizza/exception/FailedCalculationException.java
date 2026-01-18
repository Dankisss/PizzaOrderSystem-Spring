package com.deliciouspizza.exception;

public class FailedCalculationException extends RuntimeException {

    public FailedCalculationException(String message) {
        super(message);
    }

    public FailedCalculationException(String message, Throwable cause) {
        super(message, cause);
    }

}
