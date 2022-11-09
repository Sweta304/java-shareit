package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.dto.BookerDto;

import java.util.List;
import java.util.Objects;


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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemWithBooking that = (ItemWithBooking) o;
        return Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(available, that.available) && Objects.equals(id, that.id) && Objects.equals(lastBooking, that.lastBooking) && Objects.equals(nextBooking, that.nextBooking) && Objects.equals(comments, that.comments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, available, id, lastBooking, nextBooking, comments);
    }
}
