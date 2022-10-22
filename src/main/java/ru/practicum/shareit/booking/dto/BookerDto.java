package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookStatus;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookerDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Item item;
    private Long bookerId;
    private BookStatus status;
}
