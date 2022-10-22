package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.BookingNotFoundException;
import ru.practicum.shareit.booking.IncorrectBookingException;
import ru.practicum.shareit.booking.IncorrectBookingStatusException;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemNotAvailableException;
import ru.practicum.shareit.item.ItemNotFoundException;
import ru.practicum.shareit.user.IncorrectOwnerException;
import ru.practicum.shareit.user.UserNotFoundException;

import java.util.List;


public interface BookingService {
    Booking addBooking(BookingIncomingDto bookingIncomingDto, Long owner) throws ItemNotAvailableException, ItemNotFoundException, IncorrectBookingException, UserNotFoundException;

    Booking setBookingStatus(Long bookingId, Boolean approved, Long owner) throws IncorrectOwnerException, IncorrectBookingException, ItemNotFoundException;

    Booking getBookingById(Long bookingId, Long owner) throws IncorrectOwnerException, BookingNotFoundException, ItemNotFoundException;

    List<Booking> getAllBookings(Long owner, String state) throws UserNotFoundException, IncorrectBookingStatusException;

    List<Booking> getAllBookingsByOwnerItems(Long owner, String state) throws UserNotFoundException, IncorrectBookingStatusException;
}
