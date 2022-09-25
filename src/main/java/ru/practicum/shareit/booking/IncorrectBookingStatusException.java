package ru.practicum.shareit.booking;

public class IncorrectBookingStatusException extends Exception {

    public IncorrectBookingStatusException(String message) {
        super(message);
    }
}
