package ru.practicum.shareit.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingJpaRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.booking.BookingMapper.toBookingDto;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BookingServiceTest {
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Mock
    private BookingJpaRepository bookingRepository;
    @Mock
    private ItemJpaRepository itemJpaRepository;
    @Mock
    private UserJpaRepository userJpaRepository;
    private BookingIncomingDto bookingIncomingDto;
    private BookingDto bookingDto;
    private Booking booking;
    private Booking waitingBooking;
    private Booking secondBooking;
    private Item item;
    private User user;
    private User owner;
    private BookStatus bookStatus;
    private MyPageable page;
    private Sort sort;

    @BeforeEach
    void beforeEach() {
        bookingIncomingDto = new BookingIncomingDto(LocalDateTime.of(2022, 11, 15, 10, 15),
                LocalDateTime.of(2022, 12, 15, 10, 15),
                1L);
        item = new Item();
        item.setId(1L);
        item.setAvailable(true);
        user = new User();
        user.setId(1L);
        owner = new User();
        owner.setId(2L);
        item.setOwner(owner);
        bookStatus = BookStatus.APPROVED;
        booking = new Booking(1L,
                LocalDateTime.of(2022, 11, 15, 10, 15),
                LocalDateTime.of(2022, 12, 15, 10, 15),
                item,
                user,
                bookStatus);
        waitingBooking = new Booking(1L,
                LocalDateTime.of(2022, 11, 15, 10, 15),
                LocalDateTime.of(2022, 12, 15, 10, 15),
                item,
                user,
                BookStatus.WAITING);
        secondBooking = new Booking(2L,
                LocalDateTime.of(2022, 11, 16, 10, 15),
                LocalDateTime.of(2022, 12, 16, 10, 15),
                item,
                user,
                BookStatus.WAITING);
        bookingDto = toBookingDto(booking);
        sort = Sort.by(Sort.Direction.DESC, "start");
        page = new MyPageable(0, 2, sort);
    }

    @AfterEach
    void afterEach() {
        item.setId(1L);
        item.setAvailable(true);
        user.setId(1L);
        owner.setId(2L);
        item.setOwner(owner);
        booking.setStatus(BookStatus.APPROVED);
        bookingDto.setStatus(BookStatus.APPROVED);
        bookingIncomingDto.setStart(LocalDateTime.of(2022, 11, 15, 10, 15));
    }

    @Test
    void addBooking() throws Exception {
        when(bookingRepository.save(any())).thenReturn(booking);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));

        BookingDto bookingDtoSample = bookingService.addBooking(bookingIncomingDto, 1L);

        assertNotNull(bookingDtoSample);
        assertEquals(bookingDtoSample.getId(), bookingDto.getId());
        assertEquals(bookingDtoSample.getStart(), bookingDto.getStart());
        assertEquals(bookingDtoSample.getEnd(), bookingDto.getEnd());
        assertEquals(bookingDtoSample.getItem(), bookingDto.getItem());
        assertEquals(bookingDtoSample.getBooker(), bookingDto.getBooker());
        assertEquals(bookingDtoSample.getStatus(), bookingDto.getStatus());
    }

    @Test
    void addBookingByItemOwner() {
        item.setOwner(user);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(ItemNotFoundException.class, () -> bookingService.addBooking(bookingIncomingDto, 1L));
    }

    @Test
    void addBookingItemNotFound() {
        when(bookingRepository.save(booking)).thenReturn(booking);
        assertThrows(ItemNotFoundException.class, () -> bookingService.addBooking(bookingIncomingDto, 1L));
    }

    @Test
    void addBookingItemNotAvailable() {
        item.setAvailable(false);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(ItemNotAvailableException.class, () -> bookingService.addBooking(bookingIncomingDto, 1L));
    }

    @Test
    void addBookingIncorrectTime() {
        bookingIncomingDto.setStart(LocalDateTime.now().minusDays(1));
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(IncorrectBookingException.class, () -> bookingService.addBooking(bookingIncomingDto, 1L));
    }

    @Test
    void setBookingStatusApproved() throws Exception {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(waitingBooking));
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto bookingDtoSample = bookingService.setBookingStatus(1L, true, 2L);

        assertNotNull(bookingDtoSample);
        assertEquals(bookingDtoSample.getId(), bookingDto.getId());
        assertEquals(bookingDtoSample.getStart(), bookingDto.getStart());
        assertEquals(bookingDtoSample.getEnd(), bookingDto.getEnd());
        assertEquals(bookingDtoSample.getItem(), bookingDto.getItem());
        assertEquals(bookingDtoSample.getBooker(), bookingDto.getBooker());
        assertEquals(bookingDtoSample.getStatus(), bookingDto.getStatus());
    }

    @Test
    void setBookingStatusRejected() throws Exception {
        bookingDto.setStatus(BookStatus.REJECTED);
        booking.setStatus(BookStatus.REJECTED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(waitingBooking));
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto bookingDtoSample = bookingService.setBookingStatus(1L, false, 2L);

        assertNotNull(bookingDtoSample);
        assertEquals(bookingDtoSample.getId(), bookingDto.getId());
        assertEquals(bookingDtoSample.getStart(), bookingDto.getStart());
        assertEquals(bookingDtoSample.getEnd(), bookingDto.getEnd());
        assertEquals(bookingDtoSample.getItem(), bookingDto.getItem());
        assertEquals(bookingDtoSample.getBooker(), bookingDto.getBooker());
        assertEquals(bookingDtoSample.getStatus(), bookingDto.getStatus());
    }

    @Test
    void setBookingStatusItemNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        assertThrows(ItemNotFoundException.class, () -> bookingService.setBookingStatus(1L, true, 1L));
    }

    @Test
    void setBookingStatusIncorrectBooking() {
        assertThrows(IncorrectBookingException.class, () -> bookingService.setBookingStatus(1L, true, 1L));
    }

    @Test
    void setBookingStatusNotOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(IncorrectOwnerException.class, () -> bookingService.setBookingStatus(1L, true, 1L));
    }

    @Test
    void setBookingIncorrectStatus() {
        item.setOwner(user);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(IncorrectBookingException.class, () -> bookingService.setBookingStatus(1L, true, 1L));
    }

    @Test
    void getBookingById() throws Exception {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));

        BookingDto bookingDtoSample = bookingService.getBookingById(1L, 1L);

        assertNotNull(bookingDtoSample);
        assertEquals(bookingDtoSample.getId(), bookingDto.getId());
        assertEquals(bookingDtoSample.getStart(), bookingDto.getStart());
        assertEquals(bookingDtoSample.getEnd(), bookingDto.getEnd());
        assertEquals(bookingDtoSample.getItem(), bookingDto.getItem());
        assertEquals(bookingDtoSample.getBooker(), bookingDto.getBooker());
        assertEquals(bookingDtoSample.getStatus(), bookingDto.getStatus());
    }

    @Test
    void getBookingByIdNotFound() {
        assertThrows(BookingNotFoundException.class, () -> bookingService.getBookingById(1L, 1L));
    }

    @Test
    void getBookingByIdItemNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        assertThrows(ItemNotFoundException.class, () -> bookingService.getBookingById(1L, 1L));
    }

    @Test
    void getBookingByIdIncorrectOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(IncorrectOwnerException.class, () -> bookingService.getBookingById(1L, 3L));
    }

    @Test
    void getAllBookingsIncorrectState() {
        assertThrows(IncorrectBookingStatusException.class, () -> bookingService.getAllBookings(1L, "status", 0, 10));
    }

    @Test
    void getAllBookingsUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> bookingService.getAllBookings(1L, "ALL", 0, 10));
    }

    @Test
    void getAllBookingsIncorrectPagination() {
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(PaginationNotCorrectException.class, () -> bookingService.getAllBookings(1L, "ALL", -1, 10));
    }

    @Test
    void getAllBookingsPagination() throws Exception {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerId(1L, page)).thenReturn(requestPage);

        List<BookingDto> bookings = bookingService.getAllBookings(1L, "ALL", 0, 2);

        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getId(), booking.getId());
    }

    @Test
    void getFutureBookingsPagination() throws Exception {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStartIsAfter(any(), any(), any())).thenReturn(requestPage);

        List<BookingDto> bookings = bookingService.getAllBookings(1L, "FUTURE", 0, 2);

        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getId(), booking.getId());
    }

    @Test
    void getPastBookingsPagination() throws Exception {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndEndIsBefore(any(), any(), any())).thenReturn(requestPage);

        List<BookingDto> bookings = bookingService.getAllBookings(1L, "PAST", 0, 2);

        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getId(), booking.getId());
    }

    @Test
    void getCurrentBookingsPagination() throws Exception {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(any(), any(), any(), any())).thenReturn(requestPage);

        List<BookingDto> bookings = bookingService.getAllBookings(1L, "CURRENT", 0, 2);

        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getId(), booking.getId());
    }

    @Test
    void getWaitingBookingsPagination() throws Exception {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStatus(1L, BookStatus.WAITING, page)).thenReturn(requestPage);

        List<BookingDto> bookings = bookingService.getAllBookings(1L, "WAITING", 0, 2);

        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getId(), booking.getId());
    }

    @Test
    void getRejectedBookingsPagination() throws Exception {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStatus(1L, BookStatus.REJECTED, page)).thenReturn(requestPage);

        List<BookingDto> bookings = bookingService.getAllBookings(1L, "REJECTED", 0, 2);

        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getId(), booking.getId());
    }

    @Test
    void getBookingsUnsupportedStatus() {
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStatus(1L, BookStatus.CANCELED, sort)).thenReturn(List.of(booking, secondBooking));

        assertThrows(IncorrectBookingStatusException.class, () -> bookingService.getAllBookings(1L, "CANCELED", 0, 2));
    }

    @Test
    void getAllBookingsByOwnerIncorrectStatus() {
        when(bookingRepository.findAll()).thenReturn(List.of(booking, secondBooking));
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(IncorrectBookingStatusException.class, () -> bookingService.getAllBookingsByOwnerItems(1L, "STATE", 0, 2));
    }

    @Test
    void getAllBookingsByOwnerNotFoundUser() {
        when(bookingRepository.findAll()).thenReturn(List.of(booking, secondBooking));
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(UserNotFoundException.class, () -> bookingService.getAllBookingsByOwnerItems(1L, "WAITING", 0, 2));
    }

    @Test
    void getAllBookingsByOwnerIncorrectPagination() {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(bookingRepository.findAll()).thenReturn(List.of(booking, secondBooking));
        when(bookingRepository.findAll(page)).thenReturn(requestPage);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(PaginationNotCorrectException.class, () -> bookingService.getAllBookingsByOwnerItems(1L, "ALL", 0, 0));
    }

    @Test
    void getAllBookingsByOwnerAllPagination() throws Exception {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(bookingRepository.findAll()).thenReturn(List.of(booking, secondBooking));
        when(bookingRepository.findAll(page)).thenReturn(requestPage);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(2L)).thenReturn(Optional.of(user));
        List<BookingDto> bookings = bookingService.getAllBookingsByOwnerItems(2L, "ALL", 0, 2);

        assertEquals(2, bookings.size());
        assertEquals(bookings.get(0).getId(), secondBooking.getId());
    }

    @Test
    void getAllBookingsByOwnerAll() throws Exception {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(bookingRepository.findAll(page)).thenReturn(requestPage);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(2L)).thenReturn(Optional.of(user));
        List<BookingDto> bookings = bookingService.getAllBookingsByOwnerItems(2L, "ALL", 0, 2);

        assertEquals(2, bookings.size());
        assertEquals(bookings.get(0).getId(), secondBooking.getId());
    }

    @Test
    void getAllBookingsByOwnerFuturePagination() throws Exception {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(bookingRepository.findAll()).thenReturn(List.of(booking, secondBooking));
        when(bookingRepository.findAll(page)).thenReturn(requestPage);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(2L)).thenReturn(Optional.of(user));
        List<BookingDto> bookings = bookingService.getAllBookingsByOwnerItems(2L, "FUTURE", 0, 2);

        assertEquals(2, bookings.size());
        assertEquals(bookings.get(0).getId(), secondBooking.getId());
    }

    @Test
    void getAllBookingsByOwnerFuture() throws Exception {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(bookingRepository.findAll(page)).thenReturn(requestPage);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(2L)).thenReturn(Optional.of(user));
        List<BookingDto> bookings = bookingService.getAllBookingsByOwnerItems(2L, "FUTURE", 0, 2);

        assertEquals(2, bookings.size());
        assertEquals(bookings.get(0).getId(), secondBooking.getId());
    }

    @Test
    void getAllBookingsByOwnerPastPagination() throws Exception {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(bookingRepository.findAll()).thenReturn(List.of(booking, secondBooking));
        when(bookingRepository.findAll(page)).thenReturn(requestPage);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(2L)).thenReturn(Optional.of(user));
        List<BookingDto> bookings = bookingService.getAllBookingsByOwnerItems(2L, "PAST", 0, 2);

        assertEquals(0, bookings.size());
    }

    @Test
    void getAllBookingsByOwnerPast() throws Exception {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        booking.setStart(LocalDateTime.of(2022, 8, 15, 10, 15));
        booking.setEnd(LocalDateTime.of(2022, 8, 16, 10, 15));
        when(bookingRepository.findAll(page)).thenReturn(requestPage);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(2L)).thenReturn(Optional.of(user));
        List<BookingDto> bookings = bookingService.getAllBookingsByOwnerItems(2L, "PAST", 0, 2);

        assertEquals(1, bookings.size());
        booking.setStart(LocalDateTime.of(2022, 11, 15, 10, 15));
        booking.setEnd(LocalDateTime.of(2022, 12, 15, 10, 15));
    }


    @Test
    void getAllBookingsByOwnerCurrentPagination() throws Exception {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(bookingRepository.findAll()).thenReturn(List.of(booking, secondBooking));
        when(bookingRepository.findAll(page)).thenReturn(requestPage);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(2L)).thenReturn(Optional.of(user));
        List<BookingDto> bookings = bookingService.getAllBookingsByOwnerItems(2L, "CURRENT", 0, 2);

        assertEquals(0, bookings.size());
    }

    @Test
    void getAllBookingsByOwnerCurrent() throws Exception {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(bookingRepository.findAll(page)).thenReturn(requestPage);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(2L)).thenReturn(Optional.of(user));
        List<BookingDto> bookings = bookingService.getAllBookingsByOwnerItems(2L, "CURRENT", 0, 2);

        assertEquals(0, bookings.size());
    }

    @Test
    void getAllBookingsByOwnerWaitingPagination() throws Exception {
        booking.setStatus(BookStatus.WAITING);
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(bookingRepository.findAll()).thenReturn(List.of(booking, secondBooking));
        when(bookingRepository.findAll(page)).thenReturn(requestPage);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(2L)).thenReturn(Optional.of(user));
        List<BookingDto> bookings = bookingService.getAllBookingsByOwnerItems(2L, "WAITING", 0, 2);

        assertEquals(2, bookings.size());
    }

    @Test
    void getAllBookingsByOwnerWaiting() throws Exception {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(bookingRepository.findAll(page)).thenReturn(requestPage);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(2L)).thenReturn(Optional.of(user));
        List<BookingDto> bookings = bookingService.getAllBookingsByOwnerItems(2L, "WAITING", 0, 2);

        assertEquals(1, bookings.size());
    }

    @Test
    void getAllBookingsByOwnerRejectedPagination() throws Exception {
        booking.setStatus(BookStatus.REJECTED);
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(bookingRepository.findAll()).thenReturn(List.of(booking, secondBooking));
        when(bookingRepository.findAll(page)).thenReturn(requestPage);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(2L)).thenReturn(Optional.of(user));
        List<BookingDto> bookings = bookingService.getAllBookingsByOwnerItems(2L, "REJECTED", 0, 2);

        assertEquals(1, bookings.size());
    }

    @Test
    void getAllBookingsByOwnerRejected() throws Exception {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(bookingRepository.findAll(page)).thenReturn(requestPage);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(2L)).thenReturn(Optional.of(user));
        List<BookingDto> bookings = bookingService.getAllBookingsByOwnerItems(2L, "REJECTED", 0, 2);

        assertEquals(0, bookings.size());
    }

    @Test
    void getAllBookingsByOwnerUnsupportedStatus() {
        Page<Booking> requestPage = new PageImpl<>(List.of(booking, secondBooking));
        when(bookingRepository.findAll(page)).thenReturn(requestPage);
        when(itemJpaRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userJpaRepository.findById(2L)).thenReturn(Optional.of(user));
        assertThrows(IncorrectBookingStatusException.class, () -> bookingService.getAllBookingsByOwnerItems(2L, "APPROVED", 0, 2));
    }
}
