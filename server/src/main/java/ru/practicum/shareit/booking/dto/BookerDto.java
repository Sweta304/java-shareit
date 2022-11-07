package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.booking.BookStatus;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.Objects;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookerDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private ItemDto item;
    private Long bookerId;
    private BookStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookerDto bookerDto = (BookerDto) o;
        return Objects.equals(id, bookerDto.id) && Objects.equals(start, bookerDto.start) && Objects.equals(end, bookerDto.end) && Objects.equals(item, bookerDto.item) && Objects.equals(bookerId, bookerDto.bookerId) && status == bookerDto.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, start, end, item, bookerId, status);
    }
}
