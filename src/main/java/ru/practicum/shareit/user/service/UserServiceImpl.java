package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.EmailException;
import ru.practicum.shareit.user.UserAlreadyExistsException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository UserRepository) {
        this.userRepository = UserRepository;
    }

    public UserDto addUser(UserDto user) throws UserAlreadyExistsException, ValidationException, UserNotFoundException, EmailException {
        return userRepository.addUser(user);
    }

    public UserDto updateUser(UserDto user, Long id) throws UserNotFoundException, ValidationException, EmailException, UserAlreadyExistsException {
        return userRepository.updateUser(user, id);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public UserDto getUserById(Long id) throws UserNotFoundException {
        return userRepository.getUserById(id);
    }

    public UserDto deleteUser(Long id) throws UserNotFoundException {
        return userRepository.deleteUser(id);
    }
}
