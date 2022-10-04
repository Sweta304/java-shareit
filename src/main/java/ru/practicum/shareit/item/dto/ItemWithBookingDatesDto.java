package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;


@Data
@AllArgsConstructor
public class ItemWithBookingDatesDto {
    private String name;
    private String description;
    private Boolean available;
    private Long id;
    private Booking lastBooking;
    private Booking nextBooking;
    private List<CommentDto> comments;

}
