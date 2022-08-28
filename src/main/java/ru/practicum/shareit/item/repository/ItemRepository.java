package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.IncorrectOwnerException;
import ru.practicum.shareit.user.ValidationException;

import java.util.List;


public interface ItemRepository {
    public ItemDto addItem(ItemDto itemDto, Long owner) throws ValidationException;

    public ItemDto updateItem(Long itemId, Long owner, ItemDto itemDto) throws IncorrectOwnerException;

    public ItemDto getItem(Long itemId);

    public List<ItemDto> getItems(Long owner);

    public List<ItemDto> searchItem(String text);

    public Item getItemById(Long id);
}
