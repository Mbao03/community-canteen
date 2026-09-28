package com.example.kitchen.exception;


public class OperationFailureException extends RuntimeException {
    public OperationFailureException() {
    }

    public OperationFailureException(String message) {
        super(message);
    }
}
