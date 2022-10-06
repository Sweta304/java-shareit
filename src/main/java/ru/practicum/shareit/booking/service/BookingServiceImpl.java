package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingJpaRepository;
import ru.practicum.shareit.item.ItemNotAvailableException;
import ru.practicum.shareit.item.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemJpaRepository;
import ru.practicum.shareit.user.IncorrectOwnerException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.BookingMapper.fromBookingIncomingDto;
import static ru.practicum.shareit.booking.BookingMapper.toBookingDto;

@Service
public class BookingServiceImpl implements BookingService {

    private BookingJpaRepository bookingJpaRepository;
    private ItemJpaRepository itemJpaRepository;
    private UserJpaRepository userJpaRepository;

    public BookingServiceImpl(BookingJpaRepository bookingJpaRepository, ItemJpaRepository itemJpaRepository, UserJpaRepository userJpaRepository) {
        this.bookingJpaRepository = bookingJpaRepository;
        this.itemJpaRepository = itemJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public BookingDto addBooking(BookingIncomingDto bookingIncomingDto, Long owner) throws ItemNotAvailableException, ItemNotFoundException, IncorrectBookingException, UserNotFoundException {
        Item item = itemJpaRepository.findById(bookingIncomingDto.getItemId()).orElseThrow(() -> new ItemNotFoundException("Вещи не существует"));
        User user = userJpaRepository.findById(owner).orElseThrow(() -> new UserNotFoundException("Пользователя не существует"));
        Booking booking = fromBookingIncomingDto(bookingIncomingDto, user, item);
        if (!item.getAvailable()) {
            throw new ItemNotAvailableException("Данная вещь недоступна");
        } else if (booking.getStart().isBefore(LocalDateTime.now())
                || booking.getEnd().isBefore(LocalDateTime.now())
                || booking.getEnd().isBefore(booking.getStart())) {
            throw new IncorrectBookingException("Неправильно задано время бронирования");
        } else if (item.getOwner().getId().equals(owner)) {
            throw new ItemNotFoundException("Вы не можете забронировать собственную вещь");
        }
        return toBookingDto(bookingJpaRepository.save(booking), item, user);
    }

    @Override
    public BookingDto setBookingStatus(Long bookingId, Boolean approved, Long owner) throws IncorrectOwnerException, IncorrectBookingException, ItemNotFoundException {
        Booking booking = bookingJpaRepository.findById(bookingId).orElseThrow(() -> new IncorrectBookingException("Проверьте корректность данных"));
        Item item = itemJpaRepository.findById(booking.getItem().getId()).orElseThrow(() -> new ItemNotFoundException("Вещи не существует"));

        if (!item.getOwner().getId().equals(owner)) {
            throw new IncorrectOwnerException("Вещь не принадлежит указанному пользователю");
        }

        if (booking.getStatus().equals(BookStatus.WAITING)) {
            if (approved) {
                booking.setStatus(BookStatus.APPROVED);
            } else {
                booking.setStatus(BookStatus.REJECTED);
            }
        } else {
            throw new IncorrectBookingException("Невозможно подтвердить бронирование вещи");
        }
        return toBookingDto(bookingJpaRepository.save(booking), item, userJpaRepository.findById(booking.getBooker().getId()).get());
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long owner) throws IncorrectOwnerException, BookingNotFoundException, ItemNotFoundException {

        Booking booking = bookingJpaRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException("Бронирования не существует"));
        Item item = itemJpaRepository.findById(booking.getItem().getId()).orElseThrow(() -> new ItemNotFoundException("Вещи не существует"));

        if (!item.getOwner().getId().equals(owner) && !booking.getBooker().getId().equals(owner)) {
            throw new IncorrectOwnerException("Вещь не принадлежит указанному пользователю");
        }

        return toBookingDto(booking, item, userJpaRepository.findById(booking.getBooker().getId()).get());
    }

    @Override
    public List<Booking> getAllBookings(Long bookerId, String rawState) throws UserNotFoundException, IncorrectBookingStatusException {
        try {
            if (rawState.equals("ALL") ||
                    rawState.equals("FUTURE") ||
                    rawState.equals("CURRENT") ||
                    rawState.equals("PAST")) {
                BookCondition.valueOf(rawState);
            } else {
                BookStatus.valueOf(rawState);
            }
        } catch (IllegalArgumentException e) {
            throw new IncorrectBookingStatusException("некорректный статус бронирования");
        }
        List<Booking> bookings;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        if (userJpaRepository.findById(bookerId).isEmpty()) {
            throw new UserNotFoundException("Пользователя не существует");
        }
        switch (rawState) {
            case "ALL":
                bookings = bookingJpaRepository.findByBookerId(bookerId, sort);
                break;
            case "FUTURE":
                bookings = bookingJpaRepository.findByBookerIdAndStartIsAfter(bookerId);
                break;
            case "PAST":
                bookings = bookingJpaRepository.findByBookerIdAndEndIsBefore(bookerId);
                break;
            case "CURRENT":
                bookings = bookingJpaRepository.findByBookerIdAndCurrentState(bookerId);
                break;
            case "WAITING":
            case "REJECTED":
                bookings = bookingJpaRepository.findByBookerIdAndStatus(bookerId, BookStatus.valueOf(rawState), sort);
                break;
            default:
                throw new IncorrectBookingStatusException("Unknown state: UNSUPPORTED_STATUS");

        }
        return bookings;
    }

    @Override
    public List<Booking> getAllBookingsByOwnerItems(Long owner, String state) throws UserNotFoundException, IncorrectBookingStatusException {
        List<Booking> rawBookings = bookingJpaRepository.findAll()
                .stream()
                .filter(x -> itemJpaRepository.findById(x.getItem().getId()).get().getOwner().getId().equals(owner))
                .sorted(Comparator.comparing(Booking::getStart)
                        .reversed())
                .collect(Collectors.toList());
        List<Booking> bookings;
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

        if (userJpaRepository.findById(owner).isEmpty()) {
            throw new UserNotFoundException("Пользователя не существует");
        }

        switch (state) {
            case "ALL":
                bookings = rawBookings;
                break;
            case "FUTURE":
                bookings = rawBookings
                        .stream()
                        .filter(x -> x.getStart().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
                break;
            case "PAST":
                bookings = rawBookings
                        .stream()
                        .filter(x -> x.getEnd().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());
                break;
            case "CURRENT":
                bookings = rawBookings
                        .stream()
                        .filter(x -> (x.getEnd().isAfter(LocalDateTime.now())) && x.getStart().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());
                break;
            case "WAITING":
                bookings = rawBookings
                        .stream()
                        .filter(x -> x.getStatus().equals(BookStatus.WAITING))
                        .collect(Collectors.toList());
                break;
            case "REJECTED":
                bookings = rawBookings
                        .stream()
                        .filter(x -> x.getStatus().equals(BookStatus.REJECTED))
                        .collect(Collectors.toList());
                break;
            default:
                throw new IncorrectBookingStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookings;
    }
}
