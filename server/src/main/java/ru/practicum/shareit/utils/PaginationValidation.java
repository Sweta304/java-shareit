package ru.practicum.shareit.utils;

public class PaginationValidation {
    public static boolean validatePagination(Integer from, Integer size) {
        boolean isValid = true;
        if (from < 0 || size <= 0) {
            isValid = false;
        }
        return isValid;
    }
}
