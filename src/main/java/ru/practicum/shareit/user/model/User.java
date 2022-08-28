package ru.practicum.shareit.user.model;

import lombok.Data;

import java.util.Objects;

/**
 * // TODO .
 */
@Data
public class User {
    private Long id;
    private String name;
    private String email;

    public static boolean validate(User user) {
        boolean isValid = false;
        if (!(user.getEmail() == null
                || user.getEmail().isEmpty()
                || user.getEmail().isBlank()
                || (user.getId() != null && (user.getId() < 0)))
        ) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean validateMail(User user) {
        boolean isValid = false;
        if (!user.getEmail().contains("@") && !(user.getEmail() == null)) {
            isValid = true;
        }
        return isValid;
    }

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
