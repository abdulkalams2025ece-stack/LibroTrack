package com.proj.librotrack.Exception;

/**
 * Thrown when a Book / Student / IssueRecord id does not exist  -> HTTP 404
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
