package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
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
}
