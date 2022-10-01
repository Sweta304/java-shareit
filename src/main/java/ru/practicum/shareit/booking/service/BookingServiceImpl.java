package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookStatus;
import ru.practicum.shareit.booking.BookingNotFoundException;
import ru.practicum.shareit.booking.IncorrectBookingException;
import ru.practicum.shareit.booking.IncorrectBookingStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingJpaRepository;
import ru.practicum.shareit.item.ItemNotAvailableException;
import ru.practicum.shareit.item.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemJpaRepository;
import ru.practicum.shareit.user.IncorrectOwnerException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.repository.UserJpaRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.BookingMapper.toBookingDto;

@Service
public class BookingServiceImpl implements BookingService {

    private BookingJpaRepository bookingJpaRepository;
    private ItemJpaRepository itemJpaRepository;
    UserJpaRepository userJpaRepository;

    public BookingServiceImpl(BookingJpaRepository bookingJpaRepository, ItemJpaRepository itemJpaRepository, UserJpaRepository userJpaRepository) {
        this.bookingJpaRepository = bookingJpaRepository;
        this.itemJpaRepository = itemJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public BookingDto addBooking(Booking booking, Long owner) throws ItemNotAvailableException, ItemNotFoundException, IncorrectBookingException, UserNotFoundException {
        Optional<Item> item = itemJpaRepository.findById(booking.getItemId());
        if (item.isEmpty()) {
            throw new ItemNotFoundException("Вещи не существует");
        } else if (!item.get().getAvailable()) {
            throw new ItemNotAvailableException("Данная вещь недоступна");
        } else if (booking.getStart().isBefore(LocalDateTime.now())
                || booking.getEnd().isBefore(LocalDateTime.now())
                || booking.getEnd().isBefore(booking.getStart())) {
            throw new IncorrectBookingException("Неправильно задано время бронирования");
        } else if (userJpaRepository.findById(owner).isEmpty()) {
            throw new UserNotFoundException("Пользователя не существует");
        } else if (item.get().getOwner().equals(owner)) {
            throw new ItemNotFoundException("Вы не можете забронировать собственную вещь");
        }
        booking.setStatus(BookStatus.WAITING);
        booking.setBookerId(owner);
        return toBookingDto(bookingJpaRepository.save(booking), item.get(), userJpaRepository.findById(owner).get());
    }

    @Override
    public BookingDto setBookingStatus(Long bookingId, Boolean approved, Long owner) throws IncorrectOwnerException, IncorrectBookingException, ItemNotFoundException {
        Optional<Booking> bookingOpt = bookingJpaRepository.findById(bookingId);
        Optional<Item> itemOpt;
        Booking booking;
        Item item;

        if (bookingOpt.isPresent()) {
            booking = bookingOpt.get();
            itemOpt = itemJpaRepository.findById(bookingOpt.get().getItemId());
            if (itemOpt.isPresent()) {
                item = itemOpt.get();
            } else {
                throw new ItemNotFoundException("Вещи не существует");
            }
        } else {
            throw new IncorrectBookingException("Проверьте корректность данных");
        }

        if (!item.getOwner().equals(owner)) {
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
        return toBookingDto(bookingJpaRepository.save(booking), item, userJpaRepository.findById(booking.getBookerId()).get());
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long owner) throws IncorrectOwnerException, BookingNotFoundException {
        if (bookingJpaRepository.findById(bookingId).isEmpty()) {
            throw new BookingNotFoundException("Бронирования не существует");
        }

        Booking booking = bookingJpaRepository.findById(bookingId).get();
        Item item = itemJpaRepository.findById(booking.getItemId()).get();

        if (!item.getOwner().equals(owner) && !booking.getBookerId().equals(owner)) {
            throw new IncorrectOwnerException("Вещь не принадлежит указанному пользователю");
        }

        return toBookingDto(booking, item, userJpaRepository.findById(booking.getBookerId()).get());
    }

    @Override
    public List<BookingDto> getAllBookings(Long bookerId, String rawState) throws UserNotFoundException, IncorrectBookingStatusException {
        BookStatus state;
        try {
            state = BookStatus.valueOf(rawState);
        } catch (IllegalArgumentException e) {
            throw new IncorrectBookingStatusException("некорректный статус бронирования");
        }
        List<BookingDto> bookings = new ArrayList<>();
        if (userJpaRepository.findById(bookerId).isEmpty()) {
            throw new UserNotFoundException("Пользователя не существует");
        }

        switch (state.toString()) {
            case "ALL":
                bookings = bookingJpaRepository.findByBookerId(bookerId)
                        .stream()
                        .map(x -> toBookingDto(x, itemJpaRepository.findById(x.getItemId()).get(), userJpaRepository.findById(x.getBookerId()).get()))
                        .sorted(Comparator.comparing(BookingDto::getStart)
                                .reversed())
                        .collect(Collectors.toList());
                break;
            case "FUTURE":
                bookings = bookingJpaRepository.findByBookerIdAndStartIsAfter(bookerId)
                        .stream()
                        .map(x -> toBookingDto(x, itemJpaRepository.findById(x.getItemId()).get(), userJpaRepository.findById(x.getBookerId()).get()))
                        .sorted(Comparator.comparing(BookingDto::getStart)
                                .reversed())
                        .collect(Collectors.toList());
                break;
            case "PAST":
                bookings = bookingJpaRepository.findByBookerIdAndEndIsBefore(bookerId)
                        .stream()
                        .map(x -> toBookingDto(x, itemJpaRepository.findById(x.getItemId()).get(), userJpaRepository.findById(x.getBookerId()).get()))
                        .sorted(Comparator.comparing(BookingDto::getStart)
                                .reversed())
                        .collect(Collectors.toList());
                break;
            case "CURRENT":
                bookings = bookingJpaRepository.findByBookerIdAndCurrentState(bookerId)
                        .stream()
                        .map(x -> toBookingDto(x, itemJpaRepository.findById(x.getItemId()).get(), userJpaRepository.findById(x.getBookerId()).get()))
                        .sorted(Comparator.comparing(BookingDto::getStart)
                                .reversed())
                        .collect(Collectors.toList());
                break;
            case "WAITING":
            case "REJECTED":
                bookings = bookingJpaRepository.findByBookerIdAndStatus(bookerId, state)
                        .stream()
                        .map(x -> toBookingDto(x, itemJpaRepository.findById(x.getItemId()).get(), userJpaRepository.findById(x.getBookerId()).get()))
                        .sorted(Comparator.comparing(BookingDto::getStart)
                                .reversed())
                        .collect(Collectors.toList());
                break;
            default:
                throw new IncorrectBookingStatusException("Unknown state: UNSUPPORTED_STATUS");

        }
        return bookings;
    }

    @Override
    public List<BookingDto> getAllBookingsByOwnerItems(Long owner, String state) throws UserNotFoundException, IncorrectBookingStatusException {
        List<Booking> rawBookings = bookingJpaRepository.findAll()
                .stream()
                .filter(x -> itemJpaRepository.findById(x.getItemId()).get().getOwner().equals(owner))
                .collect(Collectors.toList());
        List<BookingDto> bookings = new ArrayList<>();
        try {
            BookStatus status = BookStatus.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new IncorrectBookingStatusException("некорректный статус бронирования");
        }

        if (userJpaRepository.findById(owner).isEmpty()) {
            throw new UserNotFoundException("Пользователя не существует");
        }

        switch (state) {
            case "ALL":
                bookings = rawBookings
                        .stream()
                        .map(x -> toBookingDto(x, itemJpaRepository.findById(x.getItemId()).get(), userJpaRepository.findById(x.getBookerId()).get()))
                        .sorted(Comparator.comparing(BookingDto::getStart)
                                .reversed())
                        .collect(Collectors.toList());
                break;
            case "FUTURE":
                bookings = rawBookings
                        .stream()
                        .filter(x -> x.getStart().isAfter(LocalDateTime.now()))
                        .map(x -> toBookingDto(x, itemJpaRepository.findById(x.getItemId()).get(), userJpaRepository.findById(x.getBookerId()).get()))
                        .sorted(Comparator.comparing(BookingDto::getStart)
                                .reversed())
                        .collect(Collectors.toList());
                break;
            case "PAST":
                bookings = rawBookings
                        .stream()
                        .filter(x -> x.getEnd().isBefore(LocalDateTime.now()))
                        .map(x -> toBookingDto(x, itemJpaRepository.findById(x.getItemId()).get(), userJpaRepository.findById(x.getBookerId()).get()))
                        .sorted(Comparator.comparing(BookingDto::getStart)
                                .reversed())
                        .collect(Collectors.toList());
                break;
            case "CURRENT":
                bookings = rawBookings
                        .stream()
                        .filter(x -> (x.getEnd().isAfter(LocalDateTime.now())) && x.getStart().isBefore(LocalDateTime.now()))
                        .map(x -> toBookingDto(x, itemJpaRepository.findById(x.getItemId()).get(), userJpaRepository.findById(x.getBookerId()).get()))
                        .sorted(Comparator.comparing(BookingDto::getStart)
                                .reversed())
                        .collect(Collectors.toList());
                break;
            case "WAITING":
                bookings = rawBookings
                        .stream()
                        .filter(x -> x.getStatus().equals(BookStatus.WAITING))
                        .map(x -> toBookingDto(x, itemJpaRepository.findById(x.getItemId()).get(), userJpaRepository.findById(x.getBookerId()).get()))
                        .sorted(Comparator.comparing(BookingDto::getStart)
                                .reversed())
                        .collect(Collectors.toList());
                break;
            case "REJECTED":
                bookings = rawBookings
                        .stream()
                        .filter(x -> x.getStatus().equals(BookStatus.REJECTED))
                        .map(x -> toBookingDto(x, itemJpaRepository.findById(x.getItemId()).get(), userJpaRepository.findById(x.getBookerId()).get()))
                        .sorted(Comparator.comparing(BookingDto::getStart)
                                .reversed())
                        .collect(Collectors.toList());
                break;
            default:
                throw new IncorrectBookingStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookings;
    }
}
