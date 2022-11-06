package ru.practicum.shareit.requests.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.RequestNotCorrectException;
import ru.practicum.shareit.requests.RequestNotFoundException;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.service.ItemRequestService;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.utils.PaginationNotCorrectException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private ItemRequestService itemRequestService;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequestDto addItemRequest(@RequestHeader("X-Sharer-User-Id") Long requestor, @RequestBody ItemRequestDto itemRequest) throws UserNotFoundException, RequestNotCorrectException {
        return itemRequestService.addItemRequest(itemRequest, requestor);
    }

    @GetMapping
    public List<ItemRequestDto> getItemRequestDtos(@RequestHeader("X-Sharer-User-Id") Long requestor) throws UserNotFoundException {
        return itemRequestService.getItemRequestDtos(requestor);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequestDtos(@RequestHeader("X-Sharer-User-Id") Long requestor, @RequestParam(required = false, defaultValue = "0") Integer from, @RequestParam(required = false, defaultValue = "20") Integer size) throws UserNotFoundException, PaginationNotCorrectException {
        return itemRequestService.getAllItemRequestDtos(requestor, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequestById(@RequestHeader("X-Sharer-User-Id") Long requestor, @PathVariable Long requestId) throws RequestNotFoundException, UserNotFoundException {
        return itemRequestService.getItemRequestById(requestor, requestId);
    }

}
