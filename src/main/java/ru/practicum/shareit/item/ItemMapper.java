package ru.practicum.shareit.item;

import lombok.Data;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBooking;
import ru.practicum.shareit.item.dto.ItemWithBookingDatesDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Data
public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getId()
        );
    }

    public static Item fromItemDto(ItemDto item, Long owner) {
        return new Item(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                owner,
                null
        );
    }

    public static ItemWithBookingDatesDto toItemWithBookingDatesDto(Item item, Booking last, Booking next, List<CommentDto> comments) {
        return new ItemWithBookingDatesDto(
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getId(),
                last,
                next,
                comments
        );
    }

    public static ItemWithBooking toItemWithBooking(Item item, Booking last, Booking next, List<CommentDto> comments) {
        return new ItemWithBooking(
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getId(),
                last,
                next,
                comments
        );
    }
}
