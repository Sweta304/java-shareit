package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.dto.BookerDto;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
public class ItemWithBooking {
    private String name;
    private String description;
    private Boolean available;
    private Long id;
    private BookerDto lastBooking;
    private BookerDto nextBooking;
    private List<CommentDto> comments;
}
