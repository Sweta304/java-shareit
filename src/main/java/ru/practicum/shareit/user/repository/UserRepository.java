package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.EmailException;
import ru.practicum.shareit.user.UserAlreadyExistsException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserRepository {
    UserDto addUser(UserDto user) throws UserAlreadyExistsException, ValidationException, EmailException;

    UserDto updateUser(UserDto user, Long id) throws UserNotFoundException, ValidationException, EmailException, UserAlreadyExistsException;

    List<UserDto> getAllUsers();

    UserDto getUserById(Long id) throws UserNotFoundException;

    UserDto deleteUser(Long id) throws UserNotFoundException;

}
