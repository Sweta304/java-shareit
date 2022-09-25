package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Data
public class BookingMapper {
    private Booking booking;

    public static BookingDto toBookingDto(Booking booking, Item item) {
        User booker = new User();
        booker.setId(booking.getBookerId());
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                item,
                booker,
                booking.getStatus()
        );
    }
}
