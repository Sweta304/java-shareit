package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.IncorrectBookingException;
import ru.practicum.shareit.item.IncorrectCommentException;
import ru.practicum.shareit.item.ItemNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBooking;
import ru.practicum.shareit.requests.RequestNotFoundException;
import ru.practicum.shareit.user.IncorrectOwnerException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.ValidationException;


import java.util.List;

@Service
public interface ItemService {
    ItemDto addItem(ItemDto itemDto, Long owner) throws UserNotFoundException, ValidationException, RequestNotFoundException;

    ItemDto updateItem(Long itemId, Long owner, ItemDto itemDto) throws IncorrectOwnerException, ItemNotFoundException;

    ItemWithBooking getItem(Long itemId, Long owner) throws ItemNotFoundException;

    List<ItemWithBooking> getItems(Long owner, Integer from, Integer size) throws UserNotFoundException;

    List<ItemDto> searchItem(String text, Integer from, Integer size) ;

    CommentDto addComment(CommentDto comment, Long itemId, Long owner) throws IncorrectBookingException, IncorrectCommentException;
}
