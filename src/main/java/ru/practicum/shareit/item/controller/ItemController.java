package ru.practicum.shareit.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.IncorrectBookingException;
import ru.practicum.shareit.item.IncorrectCommentException;
import ru.practicum.shareit.item.ItemNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBooking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.requests.RequestNotFoundException;
import ru.practicum.shareit.user.IncorrectOwnerException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.ValidationException;
import ru.practicum.shareit.utils.PaginationNotCorrectException;

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
    public ItemDto addItem(@RequestBody @Valid ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long owner) throws UserNotFoundException, ValidationException, RequestNotFoundException {
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
    public List<ItemWithBooking> getItems(@RequestHeader("X-Sharer-User-Id") Long owner, @RequestParam(required = false, defaultValue = "0") Integer from, @RequestParam(required = false, defaultValue = "20") Integer size) throws UserNotFoundException, PaginationNotCorrectException {
        return itemService.getItems(owner, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam String text, @RequestParam(required = false, defaultValue = "0") Integer from, @RequestParam(required = false, defaultValue = "20") Integer size) throws PaginationNotCorrectException {
        return itemService.searchItem(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestBody @Valid Comment comment, @PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long owner) throws IncorrectBookingException, IncorrectCommentException {
        return itemService.addComment(comment, itemId, owner);
    }
}
