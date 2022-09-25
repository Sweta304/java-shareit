package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * // TODO .
 */
@Entity
@Table(name = "booking")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "booking_start")
    private LocalDateTime start;
    @Column(name = "booking_end")
    private LocalDateTime end;
    @Column(name = "item_id")
    private Long itemId;
    @Column(name = "booker_id")
    private Long bookerId;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookStatus status;
}
