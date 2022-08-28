package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.IncorrectOwnerException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.ValidationException;

import java.util.List;

@Service
public interface ItemService {
    public ItemDto addItem(ItemDto itemDto, Long owner) throws UserNotFoundException, ValidationException;

    public ItemDto updateItem(Long itemId, Long owner, ItemDto itemDto) throws IncorrectOwnerException;

    public ItemDto getItem(Long itemId);

    public List<ItemDto> getItems(Long owner);

    public List<ItemDto> searchItem(String text);
}
