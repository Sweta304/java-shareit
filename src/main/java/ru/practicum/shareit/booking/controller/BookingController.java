package ru.practicum.shareit.booking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.BookingNotFoundException;
import ru.practicum.shareit.booking.IncorrectBookingException;
import ru.practicum.shareit.booking.IncorrectBookingStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.ItemNotAvailableException;
import ru.practicum.shareit.item.ItemNotFoundException;
import ru.practicum.shareit.user.IncorrectOwnerException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.utils.PaginationNotCorrectException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto addBooking(@RequestBody @Valid BookingIncomingDto bookingIncomingDto, @RequestHeader("X-Sharer-User-Id") Long booker) throws ItemNotAvailableException, ItemNotFoundException, IncorrectBookingException, UserNotFoundException {
        return bookingService.addBooking(bookingIncomingDto, booker);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto setBookingStatus(@PathVariable Long bookingId, @RequestParam Boolean approved, @RequestHeader("X-Sharer-User-Id") Long owner) throws IncorrectOwnerException, IncorrectBookingException, ItemNotFoundException {
        return bookingService.setBookingStatus(bookingId, approved, owner);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@PathVariable Long bookingId, @RequestHeader("X-Sharer-User-Id") Long owner) throws IncorrectOwnerException, BookingNotFoundException, ItemNotFoundException {
        return bookingService.getBookingById(bookingId, owner);
    }

    @GetMapping
    public List<BookingDto> getAllBookings(@RequestHeader("X-Sharer-User-Id") Long owner, @RequestParam(required = false, defaultValue = "ALL") String state, @RequestParam(required = false, defaultValue = "0") Integer from, @RequestParam(required = false, defaultValue = "20") Integer size) throws UserNotFoundException, IncorrectBookingStatusException, PaginationNotCorrectException {
        return bookingService.getAllBookings(owner, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingsByOwnerItems(@RequestHeader("X-Sharer-User-Id") Long owner, @RequestParam(required = false, defaultValue = "ALL") String state, @RequestParam(required = false, defaultValue = "0") Integer from, @RequestParam(required = false, defaultValue = "20") Integer size) throws UserNotFoundException, IncorrectBookingStatusException, PaginationNotCorrectException {
        return bookingService.getAllBookingsByOwnerItems(owner, state, from, size);
    }
}
