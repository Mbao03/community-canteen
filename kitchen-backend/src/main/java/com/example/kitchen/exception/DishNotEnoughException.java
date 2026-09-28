package com.example.kitchen.exception;


public class DishNotEnoughException extends RuntimeException {
    public DishNotEnoughException() {
        super();
    }

    public DishNotEnoughException(String message) {
        super(message);
    }
}
