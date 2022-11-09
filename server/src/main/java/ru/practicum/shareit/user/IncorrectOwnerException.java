package ru.practicum.shareit.user;

public class IncorrectOwnerException extends Exception {

    public IncorrectOwnerException(String message) {
        super(message);
    }
}
