package ru.practicum.shareit.exception.exception;

public class BadRequestStateException extends RuntimeException {
    public BadRequestStateException(String message) {
        super(message);
    }
}
