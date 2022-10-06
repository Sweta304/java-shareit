package ru.practicum.shareit.requests;

import lombok.Data;
import ru.practicum.shareit.user.model.User;

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
    @ManyToOne
    @JoinColumn(name = "requestor_id")
    private User requestor;
    @Column(name = "created")
    private LocalDateTime created;
}
