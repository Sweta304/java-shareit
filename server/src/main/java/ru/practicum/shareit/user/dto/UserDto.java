package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;

    public static boolean validate(UserDto user) {
        boolean isValid = false;
        if (
                !(user.getEmail() == null
                        || user.getEmail().isEmpty()
                        || user.getEmail().isBlank()
                        || (user.getId() != null && (user.getId() < 0)))
        ) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean validateMail(UserDto user) {
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
        UserDto userDto = (UserDto) o;
        return Objects.equals(id, userDto.id) && Objects.equals(name, userDto.name) && Objects.equals(email, userDto.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email);
    }
}
