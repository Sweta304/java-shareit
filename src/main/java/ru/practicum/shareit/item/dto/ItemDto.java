package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * // TODO .
 */
@Data
@AllArgsConstructor
public class ItemDto {
    private String name;
    private String description;
    private Boolean available;
    private Long id;

    public static boolean validateItem(ItemDto itemDto) {
        boolean isValid = true;
        if (itemDto.getDescription() == null
                || itemDto.getName() == null
                || itemDto.getName().isEmpty()
                || itemDto.getName().isBlank()
                || itemDto.getAvailable() == null) {
            isValid = false;
        }
        return isValid;
    }
}
