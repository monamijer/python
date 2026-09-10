// BusinessException.java
// Thrown when a request violates a business rule (RG1, RG7...) rather than
// being simply malformed — mapped to HTTP 409 Conflict by the exception handler.
package com.monprojet.series.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}