package com.proj.librotrack.Exception;

/**
 * Thrown when a library rule is broken, e.g. "all copies are already
 * checked out" or "book already returned"  -> HTTP 409 Conflict
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
