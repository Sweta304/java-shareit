package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Data
public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking, Item item, User booker) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                item,
                booker,
                booking.getStatus()
        );
    }

    public static Booking fromBookingIncomingDto(BookingIncomingDto bookingIncomingDto, Long owner) {
        Booking booking = new Booking();
        booking.setBookerId(owner);
        booking.setStatus(BookStatus.WAITING);
        booking.setItemId(bookingIncomingDto.getItemId());
        booking.setStart(bookingIncomingDto.getStart());
        booking.setEnd(bookingIncomingDto.getEnd());
        return booking;
    }
}
