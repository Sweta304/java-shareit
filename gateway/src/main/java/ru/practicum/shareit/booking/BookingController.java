package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.exception.IncorrectBookingStatusException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader("X-Sharer-User-Id") @Positive Long booker,
                                           @RequestBody @Valid BookingIncomingDto bookingIncomingDto) {
        log.info("Creating booking {}, userId={}", bookingIncomingDto, booker);
        return bookingClient.bookItem(booker, bookingIncomingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> setBookingStatus(@RequestHeader("X-Sharer-User-Id") @Positive Long owner,
                                                   @PathVariable @Positive Long bookingId,
                                                   @RequestParam Boolean approved) {
        log.info("Set booking {} approved={}, userId={}", bookingId, approved, owner);
        return bookingClient.setBookingStatus(owner, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader("X-Sharer-User-Id") @Positive Long owner,
                                                 @PathVariable Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, owner);
        return bookingClient.getBooking(owner, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllBookings(@RequestHeader("X-Sharer-User-Id") @Positive Long owner,
                                                 @RequestParam(required = false, defaultValue = "ALL") String state,
                                                 @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                                 @RequestParam(required = false, defaultValue = "20") @Positive Integer size) throws IncorrectBookingStatusException {
        try {
            if (state.equals("ALL") ||
                    state.equals("FUTURE") ||
                    state.equals("CURRENT") ||
                    state.equals("PAST")) {
                BookCondition.valueOf(state);
            } else {
                BookStatus.valueOf(state);
            }
        } catch (IllegalArgumentException e) {
            throw new IncorrectBookingStatusException("некорректный статус бронирования");
        }
        log.info("Get booking with state {}, userId={}, from={}, size={}", state, owner, from, size);
        return bookingClient.getBookings(owner, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllBookingsByOwnerItems(@RequestHeader("X-Sharer-User-Id") @Positive Long owner,
                                                             @RequestParam(required = false, defaultValue = "ALL") String state,
                                                             @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                                             @RequestParam(required = false, defaultValue = "20") @Positive Integer size) throws IncorrectBookingStatusException {
        try {
            if (state.equals("ALL") ||
                    state.equals("FUTURE") ||
                    state.equals("CURRENT") ||
                    state.equals("PAST")) {
                BookCondition.valueOf(state);
            } else {
                BookStatus.valueOf(state);
            }
        } catch (IllegalArgumentException e) {
            throw new IncorrectBookingStatusException("некорректный статус бронирования");
        }
        log.info("Get booking by owner with state {}, userId={}, from={}, size={}", state, owner, from, size);
        return bookingClient.getBookingsByOwnerItems(owner, state, from, size);
    }
}
