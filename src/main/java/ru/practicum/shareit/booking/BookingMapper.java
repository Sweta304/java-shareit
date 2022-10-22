package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Data
public class BookingMapper {

    public static Booking fromBookingIncomingDto(BookingIncomingDto bookingIncomingDto, User owner, Item item) {
        Booking booking = new Booking();
        booking.setBooker(owner);
        booking.setStatus(BookStatus.WAITING);
        booking.setItem(item);
        booking.setStart(bookingIncomingDto.getStart());
        booking.setEnd(bookingIncomingDto.getEnd());
        return booking;
    }

    public static BookerDto toBookerDto(Booking booking, Item item, Long booker) {
        return new BookerDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                item,
                booker,
                booking.getStatus()
        );
    }

    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem(),
                booking.getBooker(),
                booking.getStatus());
    }
}
