package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.IncorrectOwnerException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.ValidationException;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {


    private ItemRepository itemRepository;
    private UserRepository userRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Long owner) throws UserNotFoundException, ValidationException {
        if (userRepository.getUserById(owner) == null) {
            throw new UserNotFoundException("Пользователя не существует с id" + owner + "не существует");
        } else if (!ItemDto.validateItem(itemDto)) {
            throw new ValidationException("параметры вещи заданы некорректно");
        }
        return itemRepository.addItem(itemDto, owner);
    }

    @Override
    public ItemDto updateItem(Long itemId, Long owner, ItemDto itemDto) throws IncorrectOwnerException {
        Item item = itemRepository.getItemById(itemId);
        if (!(item.getOwner() == owner)) {
            throw new IncorrectOwnerException("Вещь не принадлежит указанному пользователю");
        }
        return itemRepository.updateItem(itemId, owner, itemDto);
    }

    @Override
    public ItemDto getItem(Long itemId) {
        return itemRepository.getItem(itemId);
    }

    @Override
    public List<ItemDto> getItems(Long owner) {
        return itemRepository.getItems(owner);
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        if (text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.searchItem(text);
    }
}
