package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.IncorrectOwnerException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.ValidationException;

import java.util.List;

@Service
public interface ItemService {
    ItemDto addItem(ItemDto itemDto, Long owner) throws UserNotFoundException, ValidationException;

    ItemDto updateItem(Long itemId, Long owner, ItemDto itemDto) throws IncorrectOwnerException;

    ItemDto getItem(Long itemId);

    List<ItemDto> getItems(Long owner);

    List<ItemDto> searchItem(String text);
}
