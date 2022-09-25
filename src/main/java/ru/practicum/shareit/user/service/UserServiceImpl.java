package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.EmailException;
import ru.practicum.shareit.user.UserAlreadyExistsException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.UserMapper.fromUserDto;
import static ru.practicum.shareit.user.UserMapper.toUserDto;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserJpaRepository userRepository;

    @Autowired
    public UserServiceImpl(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto addUser(UserDto userDto) throws UserAlreadyExistsException, ValidationException, EmailException {
        User user = fromUserDto(userDto);
        if (!UserDto.validate(userDto)) {
            log.error("валидация пользователя не пройдена");
            throw new ValidationException("данные о пользователе указаны некорректно");
        } else if (UserDto.validateMail(userDto)) {
            throw new EmailException("некорректный Email");
        }
        return toUserDto(userRepository.save(user));
    }

    public UserDto updateUser(UserDto userDto, Long id) throws UserNotFoundException, EmailException, UserAlreadyExistsException {
        User user = userRepository.findById(id).get();
        if (user == null) {
            throw new UserNotFoundException("такого пользователя не существует");
        } else if (userDto.getEmail() != null &&
                (userDto.getEmail().isEmpty()
                        || userDto.getEmail().isBlank())) {
            throw new EmailException("некорректный Email");
        } else if (userDto.getEmail() != null
                && getAllUsers().stream()
                .filter(x -> x.getEmail().equals(user.getEmail()))
                .filter(x -> x.getId() != user.getId())
                .findAny()
                .isPresent()) {
            throw new UserAlreadyExistsException("пользователь с таким Email уже существует");
        } else if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        return toUserDto(userRepository.save(user));
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(x -> toUserDto(x))
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) throws UserNotFoundException {
        if (!userRepository.findById(id).isPresent()) {
            throw new UserNotFoundException("пользователь с id " + id + " не существует");
        }
        User user = userRepository.findById(id).get();
        return toUserDto(user);
    }

    public UserDto deleteUser(Long id) throws UserNotFoundException {
        User user = userRepository.findById(id).get();
        if (user == null) {
            throw new UserNotFoundException("пользователь с id " + id + " не существует");
        } else {
            userRepository.delete(user);
        }
        return toUserDto(user);
    }
}
