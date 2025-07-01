package com.deliciouspizza.exception;

public class OrderProductNotFoundException extends RuntimeException {

    public OrderProductNotFoundException(long orderId, long productId) {
        super("Order product not found with orderId: " + orderId + "and productId: " + productId);
    }

    public OrderProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
