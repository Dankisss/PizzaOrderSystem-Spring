package com.deliciouspizza.exception.handler;

import com.deliciouspizza.exception.InvalidCountException;
import com.deliciouspizza.exception.OrderNotFoundException;
import com.deliciouspizza.exception.OrderNotProcessedException;
import com.deliciouspizza.exception.OrderProductAlreadyExistsException;
import com.deliciouspizza.exception.OrderProductNotFoundException;
import com.deliciouspizza.exception.ProductNotFoundException;
import com.deliciouspizza.exception.UserAlreadyExistsException;
import com.deliciouspizza.exception.UserNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

     /**
     * Handles UserNotFoundException and returns a 404 Not Found status.
     *
     * @param ex      The UserNotFoundException that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity with a 404 status and a custom error body.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).substring(4));

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles UserAlreadyExistsException and returns a 409 Conflict status.
     * This is typically used when a unique constraint (like email or username) is violated.
     *
     * @param ex      The UserAlreadyExistsException that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity with a 409 status and a custom error body.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExistsException(UserAlreadyExistsException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).substring(4));

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Handles jakarta.validation.ConstraintViolationException and returns a 400 Bad Request status.
     * This typically occurs when @Validated is used on service methods or when validating entities.
     *
     * @param ex      The ConstraintViolationException that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity with a 400 status and a custom error body.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");

        List<String> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        body.put("messages", errors); // Use "messages" for a list of errors
        body.put("path", request.getDescription(false).substring(4));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles MethodArgumentNotValidException for @Valid on @RequestBody or @ModelAttribute DTOs
     * in controllers. This is the most common validation exception for REST APIs.
     *
     * @param ex      The MethodArgumentNotValidException that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity with a 400 status and a custom error body.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Error");

        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        body.put("messages", errors); // Use "messages" for a list of errors
        body.put("path", request.getDescription(false).substring(4));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles ProductNotFoundException and returns a 404 Not Found status.
     *
     * @param ex      The ProductNotFoundException that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity with a 404 status and a custom error body.
     */
    @ExceptionHandler(ProductNotFoundException.class) // Handles ProductNotFoundException
    public ResponseEntity<Object> handleProductNotFoundException(ProductNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage()); // Use the message from your exception
        body.put("path", request.getDescription(false).substring(4));

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles DataIntegrityViolationException and returns a 400 Bad Request status.
     * This catches database-level constraint violations (e.g., unique key, foreign key, not-null).
     *
     * @param ex      The DataIntegrityViolationException that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity with a 400 status and a custom error body.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Data Integrity Violation");

        String errorMessage = "The request could not be processed due to a data integrity violation.";

        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            String causeMessage = ex.getCause().getMessage().toLowerCase();
            if (causeMessage.contains("unique constraint")) {
                errorMessage = "A record with this unique identifier already exists.";
            } else if (causeMessage.contains("foreign key constraint")) {
                errorMessage = "The requested action references data that does not exist or cannot be modified.";
            } else if (causeMessage.contains("not-null constraint")) {
                errorMessage = "A required field is missing or null.";
            }
        }
        body.put("message", errorMessage);
        body.put("path", request.getDescription(false).substring(4));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles ProductCategoryMismatchException and returns a 400 Bad Request status.
     * This indicates an attempt to change the category of an existing product, which is disallowed
     * in the single-table inheritance strategy.
     *
     * @param ex      The ProductCategoryMismatchException that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity with a 400 status and a custom error body.
     */
    @ExceptionHandler(OrderProductAlreadyExistsException.ProductCategoryMismatchException.class) // Dedicated handler for this specific exception
    public ResponseEntity<Object> handleProductCategoryMismatchException(OrderProductAlreadyExistsException.ProductCategoryMismatchException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value()); // Returns 400 Bad Request
        body.put("error", "Invalid Product Update");
        body.put("message", ex.getMessage()); // The specific message about category mismatch
        body.put("path", request.getDescription(false).substring(4));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles OrderProductAlreadyExistsException and returns a 409 Conflict status.
     * This indicates that the client tried to add a product to an order where it already exists.
     *
     * @param ex      The OrderProductAlreadyExistsException that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity with a 409 status and a custom error body.
     */
    @ExceptionHandler(OrderProductAlreadyExistsException.class)
    public ResponseEntity<Object> handleOrderProductAlreadyExistsException(
            OrderProductAlreadyExistsException ex, WebRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value()); // Returns 409 Conflict
        body.put("error", "Conflict");
        body.put("message", ex.getMessage()); // The message from the exception
        body.put("path", request.getDescription(false).substring(4)); // Extracts the URI path

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Handles OrderNotFoundException and returns a 404 Not Found status.
     * Triggered when a specific order cannot be found.
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Object> handleOrderNotFoundException(
            OrderNotFoundException ex, WebRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value()); // Returns 404
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).substring(4));

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles OrderProductNotFoundException and returns a 404 Not Found status.
     * Triggered when a specific product within an order cannot be found.
     */
    @ExceptionHandler(OrderProductNotFoundException.class)
    public ResponseEntity<Object> handleOrderProductNotFoundException(
            OrderProductNotFoundException ex, WebRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value()); // Returns 404
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).substring(4));

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles OrderNotProcessedException and returns a 400 Bad Request status.
     * This is typically thrown when an business rule for processing an order is violated
     * (e.g., trying to process an order that is already in a terminal state).
     *
     * @param ex      The OrderNotProcessedException that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity with a 400 status and a custom error body.
     */
    @ExceptionHandler(OrderNotProcessedException.class)
    public ResponseEntity<Object> handleOrderNotProcessedException(
            OrderNotProcessedException ex, WebRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value()); // Returns 400
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).substring(4));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles InvalidCountException and returns a 400 Bad Request status.
     * This is typically thrown when a provided count or quantity is not valid
     * according to business rules (e.g., quantity <= 0).
     *
     * @param ex      The InvalidCountException that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity with a 400 status and a custom error body.
     */
    @ExceptionHandler(InvalidCountException.class)
    public ResponseEntity<Object> handleInvalidCountException(
            InvalidCountException ex, WebRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value()); // Returns 400
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).substring(4));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

}
