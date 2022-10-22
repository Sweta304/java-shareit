package ru.practicum.shareit.item;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

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

    public static Item fromItemDto(ItemDto item, User owner) {
        return new Item(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                owner,
                null
        );
    }

    public static ItemWithBooking toItemWithBooking(Item item, BookerDto last, BookerDto next, List<CommentDto> comments) {
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
