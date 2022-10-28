package ru.practicum.shareit.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.requests.controller.ItemRequestController;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;
import static ru.practicum.shareit.requests.ItemRequestMapper.toItemRequestDto;

@WebMvcTest(ItemRequestController.class)
@AutoConfigureMockMvc
public class ItemRequestControllerTest {
    @MockBean
    private ItemRequestService itemRequestService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    private ItemRequestDto itemRequestDto;
    private ItemRequest itemRequest;
    private Item item;
    private ItemDto itemDto;
    private User user;
    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");


    @BeforeEach
    void beforeEach() {
        item = new Item();
        item.setId(1L);
        itemDto = toItemDto(item);
        user = new User();
        user.setId(1L);
        LocalDateTime created = LocalDateTime.of(2022, 10, 15, 10, 15);
        itemRequest = new ItemRequest(1L, "description", user, created, List.of(item));
        itemRequestDto = toItemRequestDto(itemRequest, List.of(item));
    }

    @Test
    void addItemRequest() throws Exception {
        when(itemRequestService.addItemRequest(itemRequestDto, 1L)).thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequest))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription()), String.class))
                .andExpect(jsonPath("$.requestorId", is(itemRequestDto.getRequestorId()), Long.class))
                .andExpect(jsonPath("$.created", is(itemRequestDto.getCreated().format(format)), String.class))
                .andExpect(jsonPath("$.items.[0]", is(itemDto), ItemDto.class));
    }

    @Test
    void getItemRequestDtos() throws Exception {
        when(itemRequestService.getItemRequestDtos(1L)).thenReturn(List.of(itemRequestDto));
        mockMvc.perform(get("/requests")
                        .content(mapper.writeValueAsString(itemRequest))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Arrays.asList(itemRequestDto))));

        verify(itemRequestService, times(1))
                .getItemRequestDtos(1L);
    }

    @Test
    void getAllItemRequestDtos() throws Exception {
        when(itemRequestService.getAllItemRequestDtos(1L, 0, 1)).thenReturn(List.of(itemRequestDto));
        mockMvc.perform(get("/requests/all?from=0&size=1")
                        .content(mapper.writeValueAsString(itemRequest))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Arrays.asList(itemRequestDto))));

        verify(itemRequestService, times(1))
                .getAllItemRequestDtos(1L, 0, 1);
    }

    @Test
    void getItemRequestById() throws Exception {
        when(itemRequestService.getItemRequestById(1L, 1L)).thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/1")
                        .content(mapper.writeValueAsString(itemRequest))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription()), String.class))
                .andExpect(jsonPath("$.requestorId", is(itemRequestDto.getRequestorId()), Long.class))
                .andExpect(jsonPath("$.created", is(itemRequestDto.getCreated().format(format)), String.class))
                .andExpect(jsonPath("$.items.[0]", is(itemDto), ItemDto.class));

        verify(itemRequestService, times(1))
                .getItemRequestById(1L, 1L);
    }
}
