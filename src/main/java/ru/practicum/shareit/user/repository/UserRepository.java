package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.EmailException;
import ru.practicum.shareit.user.UserAlreadyExistsException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    public User addUser(User user) throws UserAlreadyExistsException, ValidationException, EmailException;

    public User updateUser(User user, Long id) throws UserNotFoundException, ValidationException, EmailException, UserAlreadyExistsException;

    public List<User> getAllUsers();

    public User getUserById(Long id) throws UserNotFoundException;

    public User deleteUser(Long id) throws UserNotFoundException;

}
