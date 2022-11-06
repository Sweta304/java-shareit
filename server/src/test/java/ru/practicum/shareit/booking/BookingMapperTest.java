package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;
import static ru.practicum.shareit.user.UserMapper.toUserDto;


public class BookingMapperTest {

    private BookingMapper bookingMapper = new BookingMapper();
    private Item item;
    private User user;
    private BookingIncomingDto bookingIncomingDto;
    private Booking booking;
    private BookingDto bookingDto;
    private BookerDto bookerDto;


    @BeforeEach
    void beforeEach() {
        item = new Item();
        item.setId(1L);
        item.setAvailable(true);
        user = new User();
        user.setId(1L);
        user.setName("name");
        bookingIncomingDto = new BookingIncomingDto(LocalDateTime.of(2022, 11, 15, 10, 15),
                LocalDateTime.of(2022, 12, 15, 10, 15),
                1L);
        booking = new Booking(1L,
                LocalDateTime.of(2022, 11, 15, 10, 15),
                LocalDateTime.of(2022, 12, 15, 10, 15),
                item,
                user,
                BookStatus.APPROVED);
        bookingDto = new BookingDto(1L,
                LocalDateTime.of(2022, 11, 15, 10, 15),
                LocalDateTime.of(2022, 12, 15, 10, 15),
                toItemDto(item),
                toUserDto(user),
                BookStatus.APPROVED);
        bookerDto = new BookerDto(1L,
                LocalDateTime.of(2022, 11, 15, 10, 15),
                LocalDateTime.of(2022, 12, 15, 10, 15),
                toItemDto(item),
                user.getId(),
                BookStatus.APPROVED);

    }

    @Test
    void fromBookingIncomingDto() {
        Booking testBooking = BookingMapper.fromBookingIncomingDto(bookingIncomingDto, user, item);
        assertEquals(booking.getStart(), testBooking.getStart());
        assertEquals(booking.getEnd(), testBooking.getEnd());
        assertEquals(booking.getItem(), testBooking.getItem());
        assertEquals(booking.getBooker(), testBooking.getBooker());
        assertEquals(BookStatus.WAITING, testBooking.getStatus());
    }

    @Test
    void toBookerDto() {
        BookerDto testBookerDto = BookingMapper.toBookerDto(booking, item, user.getId());
        assertEquals(bookerDto.getId(), testBookerDto.getId());
        assertEquals(bookerDto.getStart(), testBookerDto.getStart());
        assertEquals(bookerDto.getEnd(), testBookerDto.getEnd());
        assertEquals(bookerDto.getItem(), testBookerDto.getItem());
        assertEquals(bookerDto.getBookerId(), testBookerDto.getBookerId());
        assertEquals(bookerDto.getStatus(), testBookerDto.getStatus());
    }

    @Test
    void toBookingDto() {
        BookingDto testBookingDto = BookingMapper.toBookingDto(booking);
        assertEquals(bookingDto.getId(), testBookingDto.getId());
        assertEquals(bookerDto.getStart(), testBookingDto.getStart());
        assertEquals(bookingDto.getEnd(), testBookingDto.getEnd());
        assertEquals(bookingDto.getItem(), testBookingDto.getItem());
        assertEquals(bookingDto.getBooker(), testBookingDto.getBooker());
        assertEquals(bookingDto.getStatus(), testBookingDto.getStatus());
    }
}
