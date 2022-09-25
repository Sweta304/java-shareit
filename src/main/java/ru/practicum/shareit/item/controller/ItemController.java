package ru.practicum.shareit.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.IncorrectBookingException;
import ru.practicum.shareit.item.IncorrectCommentException;
import ru.practicum.shareit.item.ItemNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBooking;
import ru.practicum.shareit.item.dto.ItemWithBookingDatesDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.IncorrectOwnerException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.ValidationException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto addItem(@RequestBody @Valid ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long owner) throws UserNotFoundException, ValidationException {
        return itemService.addItem(itemDto, owner);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId, @RequestBody @Valid ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long owner) throws IncorrectOwnerException, ItemNotFoundException {
        return itemService.updateItem(itemId, owner, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemWithBooking getItem(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long owner) throws ItemNotFoundException {
        return itemService.getItem(itemId, owner);
    }

    @GetMapping
    public List<ItemWithBookingDatesDto> getItems(@RequestHeader("X-Sharer-User-Id") Long owner) throws UserNotFoundException {
        return itemService.getItems(owner);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam String text) {
        return itemService.searchItem(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestBody @Valid Comment comment, @PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long owner) throws IncorrectBookingException, IncorrectCommentException {
        return itemService.addComment(comment, itemId, owner);
    }
}
