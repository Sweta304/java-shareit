package ru.practicum.shareit.requests;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * // TODO .
 */
@Entity
@Table(name = "requests")
@Data
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "description")
    private String description;
    @Column(name = "requestor_id")
    private Long requestor_id;
    @Column(name = "created")
    private LocalDateTime created;
}
