package com.deliciouspizza.exception;

public class OrderProductAlreadyExistsException extends RuntimeException {

    public OrderProductAlreadyExistsException(long orderId, long productId) {
        super("Order: " + orderId + " already has a product: " + productId);
    }

    public OrderProductAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class ProductCategoryMismatchException extends RuntimeException {

        public ProductCategoryMismatchException(String message) {
            super(message);
        }

        public ProductCategoryMismatchException(String message, Throwable cause) {
            super(message, cause);
        }

    }
}
