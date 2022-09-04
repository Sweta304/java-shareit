package ru.practicum.shareit.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

/**
 * // TODO .
 */
@Data
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id) || email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}
