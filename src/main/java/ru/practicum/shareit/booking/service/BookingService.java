package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.BookingNotFoundException;
import ru.practicum.shareit.booking.IncorrectBookingException;
import ru.practicum.shareit.booking.IncorrectBookingStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.item.ItemNotAvailableException;
import ru.practicum.shareit.item.ItemNotFoundException;
import ru.practicum.shareit.user.IncorrectOwnerException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.utils.PaginationNotCorrectException;

import java.util.List;


public interface BookingService {
    BookingDto addBooking(BookingIncomingDto bookingIncomingDto, Long owner) throws ItemNotAvailableException, ItemNotFoundException, IncorrectBookingException, UserNotFoundException;

    BookingDto setBookingStatus(Long bookingId, Boolean approved, Long owner) throws IncorrectOwnerException, IncorrectBookingException, ItemNotFoundException;

    BookingDto getBookingById(Long bookingId, Long owner) throws IncorrectOwnerException, BookingNotFoundException, ItemNotFoundException;

    List<BookingDto> getAllBookings(Long owner, String state, Integer from, Integer size) throws UserNotFoundException, IncorrectBookingStatusException, PaginationNotCorrectException;

    List<BookingDto> getAllBookingsByOwnerItems(Long owner, String state, Integer from, Integer size) throws UserNotFoundException, IncorrectBookingStatusException, PaginationNotCorrectException;
}
