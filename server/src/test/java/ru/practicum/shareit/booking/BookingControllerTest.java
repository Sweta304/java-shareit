package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;
import static ru.practicum.shareit.user.UserMapper.toUserDto;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
public class BookingControllerTest {
    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    private BookingIncomingDto bookingIncomingDto;
    private BookingDto bookingDto;
    private BookingDto bookingSecondDto;
    private Item item;
    private User user;
    private BookStatus bookStatus;


    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeEach
    void beforeEach() {
        bookingIncomingDto = new BookingIncomingDto(LocalDateTime.of(2022, 11, 15, 10, 15),
                LocalDateTime.of(2022, 12, 15, 10, 15),
                1L);
        item = new Item();
        item.setId(1L);
        user = new User();
        user.setId(1L);
        bookStatus = BookStatus.APPROVED;
        bookingDto = new BookingDto(1L,
                LocalDateTime.of(2022, 11, 15, 10, 15),
                LocalDateTime.of(2022, 12, 15, 10, 15),
                toItemDto(item),
                toUserDto(user),
                bookStatus);
        bookingSecondDto = new BookingDto(2L,
                LocalDateTime.of(2022, 11, 15, 10, 15),
                LocalDateTime.of(2022, 12, 15, 10, 15),
                toItemDto(item),
                toUserDto(user),
                bookStatus);
    }

    @Test
    void addBooking() throws Exception {
        when(bookingService.addBooking(any(), any()))
                .thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingIncomingDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().format(format)), String.class))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().format(format)), String.class))
                .andExpect(jsonPath("$.item", is(bookingDto.getItem()), ItemDto.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString()), String.class));
    }

    @Test
    void setBookingStatus() throws Exception {

        when(bookingService.setBookingStatus(any(), any(), any()))
                .thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().format(format)), String.class))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().format(format)), String.class))
                .andExpect(jsonPath("$.item", is(bookingDto.getItem()), ItemDto.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString()), String.class));


        verify(bookingService, times(1)).setBookingStatus(1L, true, 1L);
    }

    @Test
    void getBookingById() throws Exception {
        when(bookingService.getBookingById(1L, 1L))
                .thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().format(format)), String.class))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().format(format)), String.class))
                .andExpect(jsonPath("$.item", is(bookingDto.getItem()), ItemDto.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString()), String.class));

        verify(bookingService, times(1)).getBookingById(1L, 1L);
    }

    @Test
    void getAllBookings() throws Exception {
        when(bookingService.getAllBookings(1L, "ALL", 0, 1))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings?from=0&size=1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(bookingDto))));

        verify(bookingService, times(1)).getAllBookings(1L, "ALL", 0, 1);
    }

    @Test
    void getAllBookingsByOwnerItems() throws Exception {
        when(bookingService.getAllBookingsByOwnerItems(any(), any(), any(), any()))
                .thenReturn(List.of(bookingDto, bookingSecondDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Arrays.asList(bookingDto, bookingSecondDto))))
        ;
    }
}
