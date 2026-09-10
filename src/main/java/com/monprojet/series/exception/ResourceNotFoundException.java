// ResourceNotFoundException.java
package com.monprojet.series.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}