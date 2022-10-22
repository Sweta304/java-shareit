package ru.practicum.shareit.requests.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.RequestNotCorrectException;
import ru.practicum.shareit.item.repository.ItemJpaRepository;
import ru.practicum.shareit.requests.RequestNotFoundException;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.repository.ItemRequestJpaRepository;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;
import ru.practicum.shareit.utils.MyPageable;
import ru.practicum.shareit.utils.PaginationNotCorrectException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.requests.ItemRequestMapper.toItemRequestDto;
import static ru.practicum.shareit.utils.PaginationValidation.validatePagination;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestJpaRepository itemRequestJpaRepository;
    private final ItemJpaRepository itemJpaRepository;
    private final UserJpaRepository userJpaRepository;

    public ItemRequestServiceImpl(ItemRequestJpaRepository itemRequestJpaRepository,
                                  ItemJpaRepository itemJpaRepository,
                                  UserJpaRepository userJpaRepository) {
        this.itemRequestJpaRepository = itemRequestJpaRepository;
        this.itemJpaRepository = itemJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public ItemRequestDto addItemRequest(ItemRequest itemRequest, Long requestor) throws UserNotFoundException, RequestNotCorrectException {
        User user = userJpaRepository.findById(requestor).orElseThrow(() -> new UserNotFoundException("Пользователя не существует"));

        if (itemRequest.getDescription() == null
                || itemRequest.getDescription().isBlank()
                || itemRequest.getDescription().isEmpty()) {
            throw new RequestNotCorrectException("Проверьте корректность описания запроса");
        }
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());
        return toItemRequestDto(itemRequestJpaRepository.save(itemRequest), new ArrayList<>());
    }

    @Override
    public List<ItemRequestDto> getItemRequestDtos(Long requestor) throws UserNotFoundException {
        User user = userJpaRepository.findById(requestor).orElseThrow(() -> new UserNotFoundException("Пользователя не существует"));
        List<ItemRequestDto> requests = itemRequestJpaRepository.findAll()
                .stream()
                .filter(x -> x.getRequestor().equals(user))
                .map(x -> toItemRequestDto(x, itemJpaRepository.findItemsByRequestId(x.getId())))
                .collect(Collectors.toList());
        return requests;
    }

    @Override
    public List<ItemRequestDto> getAllItemRequestDtos(Long requestor, Integer from, Integer size) throws UserNotFoundException, PaginationNotCorrectException {
        User user = userJpaRepository.findById(requestor).orElseThrow(() -> new UserNotFoundException("Пользователя не существует"));
        List<ItemRequestDto> requests;

        if (from != null && size != null && !validatePagination(from, size)) {
            throw new PaginationNotCorrectException("Неверно заданы параметры вывода страниц");
        }
        Sort sortByCreated = Sort.by(Sort.Direction.DESC, "created");
        Pageable page = new MyPageable(from, size, sortByCreated);
        Page<ItemRequest> requestPage = itemRequestJpaRepository.findAll(page);
        requests = requestPage.getContent()
                .stream()
                .filter(x -> !x.getRequestor().equals(user))
                .map(x -> toItemRequestDto(x, itemJpaRepository.findItemsByRequestId(x.getId())))
                .collect(Collectors.toList());

        return requests;
    }

    @Override
    public ItemRequestDto getItemRequestById(Long requestor, Long requestId) throws RequestNotFoundException, UserNotFoundException {
        if (userJpaRepository.findById(requestor).isEmpty()) {
            throw new UserNotFoundException("Пользователя не существует");
        }
        ItemRequest itemRequest = itemRequestJpaRepository.findById(requestId).orElseThrow(() -> new RequestNotFoundException("Запроса не существует"));
        ItemRequestDto requestDto = toItemRequestDto(itemRequest, itemJpaRepository.findItemsByRequestId(requestId));
        return requestDto;

    }
}
