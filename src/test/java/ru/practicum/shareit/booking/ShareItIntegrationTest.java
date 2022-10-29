package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class ShareItIntegrationTest {
    @Autowired
    private BookingController bookingController;

    @Autowired
    private ItemController itemController;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private BookingIncomingDto bookingIncomingDto;
    private BookingDto bookingDto;
    private BookingDto bookingSecondDto;
    private Item item;
    private ItemDto itemDto;
    private User user;
    private UserDto userDto;
    private BookStatus bookStatus;


    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeEach
    void beforeEach() {
        bookingIncomingDto = new BookingIncomingDto(LocalDateTime.of(2022, 11, 15, 10, 15),
                LocalDateTime.of(2022, 12, 15, 10, 15),
                1L);
        item = new Item();
        item.setId(1L);
        itemDto = new ItemDto("name", "description", true, 1L, 1L);
        item.setName("name");
        item.setDescription("description");
        item.setAvailable(true);
        item.setRequest(new ItemRequest());
        user = new User();
        user.setId(1L);
        userDto = new UserDto(1L, "Sveta", "mail@mail.ru");
        bookStatus = BookStatus.APPROVED;
        bookingDto = new BookingDto(1L,
                LocalDateTime.of(2022, 11, 3, 12, 54, 13),
                LocalDateTime.of(2022, 11, 27, 12, 54, 13),
                itemDto,
                userDto,
                bookStatus);
        bookingSecondDto = new BookingDto(2L,
                LocalDateTime.of(2022, 12, 3, 12, 54, 13),
                LocalDateTime.of(2022, 12, 27, 12, 54, 13),
                itemDto,
                userDto,
                bookStatus);
    }


    @Test
    void addBooking() throws Exception {
        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingIncomingDto))
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3L), Long.class))
                .andExpect(jsonPath("$.start", is(bookingIncomingDto.getStart().format(format)), String.class))
                .andExpect(jsonPath("$.end", is(bookingIncomingDto.getEnd().format(format)), String.class))
                .andExpect(jsonPath("$.item", is(itemDto), ItemDto.class))
                .andExpect(jsonPath("$.status", is("WAITING"), String.class));
    }

    @Test
    void getItem() throws Exception {
        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class));
    }

    @Test
    void getAllBookings() throws Exception {
        mockMvc.perform(get("/bookings?from=0&size=2")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(bookingSecondDto, bookingDto))));
    }

    @Test
    void getBookingNotFound() throws Exception {
        mockMvc.perform(get("/bookings/300")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}
