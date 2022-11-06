package ru.practicum.shareit.requests.service;

import ru.practicum.shareit.item.RequestNotCorrectException;
import ru.practicum.shareit.requests.RequestNotFoundException;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.utils.PaginationNotCorrectException;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addItemRequest(ItemRequestDto itemRequest, Long requestor) throws UserNotFoundException, RequestNotCorrectException;

    ItemRequestDto getItemRequestById(Long requestor, Long requestId) throws RequestNotFoundException, UserNotFoundException;

    List<ItemRequestDto> getItemRequestDtos(Long requestor) throws UserNotFoundException;

    List<ItemRequestDto> getAllItemRequestDtos(Long requestor, Integer from, Integer size) throws UserNotFoundException, PaginationNotCorrectException;

}
