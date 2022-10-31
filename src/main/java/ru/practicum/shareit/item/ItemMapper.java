package ru.practicum.shareit.item;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Data
public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        if (item.getRequest() != null) {
            itemDto.setRequestId(item.getRequest().getId());
        }
        return itemDto;
    }

    public static Item fromItemDto(ItemDto item, User owner, ItemRequest itemRequest) {
        return new Item(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                owner,
                itemRequest
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
