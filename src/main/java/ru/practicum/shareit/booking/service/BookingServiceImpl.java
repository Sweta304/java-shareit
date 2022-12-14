package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import ru.practicum.shareit.utils.MyPageable;
import ru.practicum.shareit.utils.PaginationNotCorrectException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.BookingMapper.fromBookingIncomingDto;
import static ru.practicum.shareit.booking.BookingMapper.toBookingDto;
import static ru.practicum.shareit.utils.PaginationValidation.validatePagination;

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
        Item item = itemJpaRepository.findById(bookingIncomingDto.getItemId()).orElseThrow(() -> new ItemNotFoundException("???????? ???? ????????????????????"));
        User user = userJpaRepository.findById(owner).orElseThrow(() -> new UserNotFoundException("???????????????????????? ???? ????????????????????"));
        Booking booking = fromBookingIncomingDto(bookingIncomingDto, user, item);
        if (!item.getAvailable()) {
            throw new ItemNotAvailableException("???????????? ???????? ????????????????????");
        } else if (booking.getStart().isBefore(LocalDateTime.now())
                || booking.getEnd().isBefore(LocalDateTime.now())
                || booking.getEnd().isBefore(booking.getStart())) {
            throw new IncorrectBookingException("?????????????????????? ???????????? ?????????? ????????????????????????");
        } else if (item.getOwner().getId().equals(owner)) {
            throw new ItemNotFoundException("???? ???? ???????????? ?????????????????????????? ?????????????????????? ????????");
        }
        return toBookingDto(bookingJpaRepository.save(booking));
    }

    @Override
    public BookingDto setBookingStatus(Long bookingId, Boolean approved, Long owner) throws IncorrectOwnerException, IncorrectBookingException, ItemNotFoundException {
        Booking booking = bookingJpaRepository.findById(bookingId).orElseThrow(() -> new IncorrectBookingException("?????????????????? ???????????????????????? ????????????"));
        Item item = itemJpaRepository.findById(booking.getItem().getId()).orElseThrow(() -> new ItemNotFoundException("???????? ???? ????????????????????"));

        if (!item.getOwner().getId().equals(owner)) {
            throw new IncorrectOwnerException("???????? ???? ?????????????????????? ???????????????????? ????????????????????????");
        }

        if (booking.getStatus().equals(BookStatus.WAITING)) {
            if (approved) {
                booking.setStatus(BookStatus.APPROVED);
            } else {
                booking.setStatus(BookStatus.REJECTED);
            }
        } else {
            throw new IncorrectBookingException("???????????????????? ?????????????????????? ???????????????????????? ????????");
        }
        return toBookingDto(bookingJpaRepository.save(booking));
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long owner) throws IncorrectOwnerException, BookingNotFoundException, ItemNotFoundException {

        Booking booking = bookingJpaRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException("???????????????????????? ???? ????????????????????"));
        Item item = itemJpaRepository.findById(booking.getItem().getId()).orElseThrow(() -> new ItemNotFoundException("???????? ???? ????????????????????"));

        if (!item.getOwner().getId().equals(owner) && !booking.getBooker().getId().equals(owner)) {
            throw new IncorrectOwnerException("???????? ???? ?????????????????????? ???????????????????? ????????????????????????");
        }

        return toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookings(Long bookerId, String rawState, Integer from, Integer size) throws UserNotFoundException, IncorrectBookingStatusException, PaginationNotCorrectException {
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
            throw new IncorrectBookingStatusException("???????????????????????? ???????????? ????????????????????????");
        }
        List<Booking> bookings;
        if (userJpaRepository.findById(bookerId).isEmpty()) {
            throw new UserNotFoundException("???????????????????????? ???? ????????????????????");
        }
        if (!validatePagination(from, size)) {
            throw new PaginationNotCorrectException("???????????????????????? ?????????????? ?????????????????????????? ????????????");
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
    public List<BookingDto> getAllBookingsByOwnerItems(Long owner, String state, Integer from, Integer size) throws UserNotFoundException, IncorrectBookingStatusException, PaginationNotCorrectException {
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
            throw new IncorrectBookingStatusException("???????????????????????? ???????????? ????????????????????????");
        }

        if (userJpaRepository.findById(owner).isEmpty()) {
            throw new UserNotFoundException("???????????????????????? ???? ????????????????????");
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        if (!validatePagination(from, size)) {
            throw new PaginationNotCorrectException("???????????????????????? ?????????????? ?????????????????????????? ????????????");
        }
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
