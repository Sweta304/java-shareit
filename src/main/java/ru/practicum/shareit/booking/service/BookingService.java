package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingNotFoundException;
import ru.practicum.shareit.booking.IncorrectBookingException;
import ru.practicum.shareit.booking.IncorrectBookingStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.item.ItemNotAvailableException;
import ru.practicum.shareit.item.ItemNotFoundException;
import ru.practicum.shareit.user.IncorrectOwnerException;
import ru.practicum.shareit.user.UserNotFoundException;

import java.util.List;

@Service
public interface BookingService {
    BookingDto addBooking(BookingIncomingDto bookingIncomingDto, Long owner) throws ItemNotAvailableException, ItemNotFoundException, IncorrectBookingException, UserNotFoundException;

    BookingDto setBookingStatus(Long bookingId, Boolean approved, Long owner) throws IncorrectOwnerException, IncorrectBookingException, ItemNotFoundException;

    BookingDto getBookingById(Long bookingId, Long owner) throws IncorrectOwnerException, BookingNotFoundException;
    List<BookingDto> getAllBookings(Long owner, String state) throws UserNotFoundException, IncorrectBookingStatusException;
    List<BookingDto> getAllBookingsByOwnerItems(Long owner, String state) throws UserNotFoundException, IncorrectBookingStatusException;
}
