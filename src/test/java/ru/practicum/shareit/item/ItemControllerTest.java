package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookStatus;
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBooking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.booking.BookingMapper.toBookerDto;
import static ru.practicum.shareit.item.CommentMapper.toCommentDto;
import static ru.practicum.shareit.item.ItemMapper.toItemWithBooking;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
public class ItemControllerTest {
    @MockBean
    private ItemService itemService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    private Item item;
    private User user;
    private User owner;
    private ItemDto itemDto;
    private ItemWithBooking itemWithBooking;
    private Comment comment;
    private CommentDto commentDto;
    private BookerDto last;
    private BookerDto next;
    private Booking booking;
    private Booking secondBooking;


    @BeforeEach
    void beforeEach() {
        item = new Item();
        item.setId(1L);
        item.setAvailable(true);
        user = new User();
        user.setId(1L);
        user.setName("name");
        owner = new User();
        owner.setId(2L);
        owner.setName("owner");
        item.setOwner(owner);
        itemDto = new ItemDto("item1", "description", true, 1L, 1L);
        comment = new Comment(1L, "text", item, user);
        commentDto = toCommentDto(comment, user.getName());
        booking = new Booking(1L,
                LocalDateTime.of(2022, 8, 15, 10, 15),
                LocalDateTime.of(2022, 8, 15, 10, 15),
                item,
                user,
                null);
        secondBooking = new Booking(2L,
                LocalDateTime.of(2022, 11, 16, 10, 15),
                LocalDateTime.of(2022, 12, 16, 10, 15),
                item,
                user,
                BookStatus.WAITING);
        last = toBookerDto(booking, item, user.getId());
        next = toBookerDto(secondBooking, item, owner.getId());
        itemWithBooking = toItemWithBooking(item, last, next, List.of(commentDto));
        ;
    }

    @Test
    void addItem() throws Exception {
        when(itemService.addItem(any(), any()))
                .thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId()), Long.class));
    }

    @Test
    void updateItem() throws Exception {
        when(itemService.updateItem(any(), any(), any()))
                .thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId()), Long.class));
    }

    @Test
    void getItem() throws Exception {
        when(itemService.getItem(1L, 1L))
                .thenReturn(itemWithBooking);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemWithBooking.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemWithBooking.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(itemWithBooking.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.id", is(itemWithBooking.getId()), Long.class));
    }

    @Test
    void getItems() throws Exception {
        when(itemService.getItems(any(), any(), any()))
                .thenReturn(List.of(itemWithBooking));

        mockMvc.perform(get("/items?from=0&size=1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(itemWithBooking))));
    }

    @Test
    void searchItem() throws Exception {
        when(itemService.searchItem(any(), any(), any()))
                .thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search?text=text&from=0&size=1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(itemDto))));
    }

    @Test
    void addComment() throws Exception {
        when(itemService.addComment(any(), any(), any()))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(commentDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText()), String.class))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName()), String.class));
    }

}
