package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.IncorrectOwnerException;
import ru.practicum.shareit.user.ValidationException;

import java.util.List;


public interface ItemRepository {
    ItemDto addItem(ItemDto itemDto, Long owner) throws ValidationException;

    ItemDto updateItem(Long itemId, Long owner, ItemDto itemDto) throws IncorrectOwnerException;

    ItemDto getItem(Long itemId);

    List<ItemDto> getItems(Long owner);

    List<ItemDto> searchItem(String text);

    Item getItemById(Long id);
}
