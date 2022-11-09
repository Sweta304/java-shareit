package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookStatus;
import ru.practicum.shareit.booking.BookingNotFoundException;
import ru.practicum.shareit.booking.IncorrectBookingException;
import ru.practicum.shareit.booking.IncorrectBookingStatusException;
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
import ru.practicum.shareit.utils.MyPageable;


import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.BookingMapper.fromBookingIncomingDto;
import static ru.practicum.shareit.booking.BookingMapper.toBookingDto;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingJpaRepository bookingJpaRepository;
    private final ItemJpaRepository itemJpaRepository;
    private final UserJpaRepository userJpaRepository;

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
        return toBookingDto(bookingJpaRepository.save(booking));
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
        return toBookingDto(bookingJpaRepository.save(booking));
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long owner) throws IncorrectOwnerException, BookingNotFoundException, ItemNotFoundException {

        Booking booking = bookingJpaRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException("Бронирования не существует"));
        Item item = itemJpaRepository.findById(booking.getItem().getId()).orElseThrow(() -> new ItemNotFoundException("Вещи не существует"));

        if (!item.getOwner().getId().equals(owner) && !booking.getBooker().getId().equals(owner)) {
            throw new IncorrectOwnerException("Вещь не принадлежит указанному пользователю");
        }

        return toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookings(Long bookerId, String rawState, Integer from, Integer size) throws UserNotFoundException, IncorrectBookingStatusException {
        List<Booking> bookings;
        if (userJpaRepository.findById(bookerId).isEmpty()) {
            throw new UserNotFoundException("Пользователя не существует");
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        Pageable page = new MyPageable(from, size, sort);
        Page<Booking> requestPage;

        switch (rawState) {
            case "ALL":
                requestPage = bookingJpaRepository.findByBookerId(bookerId, page);
                break;
            case "FUTURE":
                requestPage = bookingJpaRepository.findByBookerIdAndStartIsAfter(bookerId, LocalDateTime.now(), page);
                break;
            case "PAST":
                requestPage = bookingJpaRepository.findByBookerIdAndEndIsBefore(bookerId, LocalDateTime.now(), page);
                break;
            case "CURRENT":
                requestPage = bookingJpaRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(bookerId, LocalDateTime.now(), LocalDateTime.now(), page);
                break;
            case "WAITING":
            case "REJECTED":
                requestPage = bookingJpaRepository.findByBookerIdAndStatus(bookerId, BookStatus.valueOf(rawState), page);
                break;
            default:
                throw new IncorrectBookingStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        bookings = requestPage.getContent();
        return bookings.stream()
                .map(x -> toBookingDto(x))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllBookingsByOwnerItems(Long owner, String state, Integer from, Integer size) throws UserNotFoundException, IncorrectBookingStatusException {
        List<Booking> bookings;

        if (userJpaRepository.findById(owner).isEmpty()) {
            throw new UserNotFoundException("Пользователя не существует");
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        Pageable page = new MyPageable(from, size, sort);
        Page<Booking> requestPage = bookingJpaRepository.findAll(page);
        List<Booking> rawBookings = requestPage.getContent()
                .stream()
                .filter(x -> itemJpaRepository.findById(x.getItem().getId()).get().getOwner().getId().equals(owner))
                .sorted(Comparator.comparing(Booking::getStart)
                        .reversed())
                .collect(Collectors.toList());

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
        return bookings.stream()
                .map(x -> toBookingDto(x))
                .collect(Collectors.toList());
    }
}
