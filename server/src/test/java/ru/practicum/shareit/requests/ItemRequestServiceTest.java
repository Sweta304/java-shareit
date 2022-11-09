package ru.practicum.shareit.requests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.RequestNotCorrectException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemJpaRepository;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.repository.ItemRequestJpaRepository;
import ru.practicum.shareit.requests.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;
import ru.practicum.shareit.utils.MyPageable;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.item.ItemMapper.fromItemDto;
import static ru.practicum.shareit.requests.ItemRequestMapper.toItemRequestDto;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ItemRequestServiceTest {
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    @Mock
    private ItemJpaRepository itemRepository;
    @Mock
    private UserJpaRepository userRepository;
    @Mock
    private ItemRequestJpaRepository itemRequestJpaRepository;
    private Item item;
    private ItemDto itemDto;
    private User user;
    private User user2;
    private User owner;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void beforeEach() {
        item = new Item();
        item.setId(1L);
        item.setAvailable(true);
        user = new User();
        user.setId(1L);
        user.setName("name");
        user.setEmail("mail@ya.ru");
        user2 = new User();
        user2.setId(3L);
        user2.setName("name2");
        user2.setEmail("mail2@ya.ru");
        owner = new User();
        owner.setId(2L);
        owner.setName("owner");
        item.setOwner(owner);
        itemDto = new ItemDto("item1", "description", true, 1L, 1L);
        item = fromItemDto(itemDto, user, itemRequest);
        itemRequest = new ItemRequest(1L, "description", user, LocalDateTime.now(), List.of(item));
        itemRequestDto = toItemRequestDto(itemRequest, List.of(item));
    }

    @AfterEach
    void afterEach() {
        item.setId(1L);
        item.setAvailable(true);
        user.setId(1L);
        owner.setId(2L);
        item.setOwner(owner);
        itemRequestDto.setDescription("description");
    }

    @Test
    void addItemRequestUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> itemRequestService.addItemRequest(itemRequestDto, 1L));
    }

    @Test
    void addItemRequestNotCorrect() {
        itemRequestDto.setDescription("");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(RequestNotCorrectException.class, () -> itemRequestService.addItemRequest(itemRequestDto, 1L));
    }

    @Test
    void addItemRequest() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestJpaRepository.save(any())).thenReturn(itemRequest);
        ItemRequestDto itemReqDto = itemRequestService.addItemRequest(itemRequestDto, 1L);
        assertEquals(itemRequestDto.getId(), itemReqDto.getId());
        assertEquals(itemRequestDto.getRequestorId(), itemReqDto.getRequestorId());
        assertEquals(itemRequestDto.getDescription(), itemReqDto.getDescription());
        assertEquals(new ArrayList<>(), itemReqDto.getItems());
    }

    @Test
    void getItemRequestDtosUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> itemRequestService.getItemRequestDtos(1L));
    }

    @Test
    void getItemRequestDtos() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestJpaRepository.findAll()).thenReturn(List.of(itemRequest));
        when(itemRepository.findItemsByRequestId(1L)).thenReturn(List.of(item));
        List<ItemRequestDto> itemRequestDtos = itemRequestService.getItemRequestDtos(1L);
        assertEquals(1, itemRequestDtos.size());
        assertEquals(itemRequestDto, itemRequestDtos.get(0));
    }

    @Test
    void getAllItemRequestDtosUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> itemRequestService.getAllItemRequestDtos(1L, 0, 1));
    }

    @Test
    void getAllItemRequestDtosPagination() throws Exception {
        itemRequest.setRequestor(user2);
        Sort sortByCreated = Sort.by(Sort.Direction.DESC, "created");
        Pageable pageable = new MyPageable(0, 1, sortByCreated);
        Page<ItemRequest> requestPage = new PageImpl<>(List.of(itemRequest));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestJpaRepository.findAll(pageable)).thenReturn(requestPage);
        when(itemRepository.findItemsByRequestId(1L)).thenReturn(List.of(item));

        List<ItemRequestDto> itemRequestDtos = itemRequestService.getAllItemRequestDtos(1L, 0, 1);
        assertEquals(1, itemRequestDtos.size());
        assertEquals(toItemRequestDto(itemRequest, List.of(item)), itemRequestDtos.get(0));
        itemRequest.setRequestor(user);
    }

    @Test
    void getItemRequestByIdUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> itemRequestService.getItemRequestById(1L, 1L));
    }

    @Test
    void getItemRequestById() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestJpaRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findItemsByRequestId(1L)).thenReturn(List.of(item));
        ItemRequestDto itemReqDto = itemRequestService.getItemRequestById(1L, 1L);
        assertEquals(itemRequestDto, itemReqDto);
    }

}
