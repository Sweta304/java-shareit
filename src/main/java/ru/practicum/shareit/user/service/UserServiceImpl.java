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
import static ru.practicum.shareit.user.dto.UserDto.validateMail;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserJpaRepository userRepository;

    @Autowired
    public UserServiceImpl(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto addUser(UserDto userDto) throws ValidationException, EmailException {
        User user = fromUserDto(userDto);
        if (!UserDto.validate(userDto)) {
            log.error("валидация пользователя не пройдена");
            throw new ValidationException("данные о пользователе указаны некорректно");
        } else if (validateMail(userDto)) {
            throw new EmailException("некорректный Email");
        }
        return toUserDto(userRepository.save(user));
    }

    public UserDto updateUser(UserDto userDto, Long id) throws UserNotFoundException, EmailException, UserAlreadyExistsException {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("такого пользователя не существует"));
        if (userDto.getEmail() != null &&
                (userDto.getEmail().isEmpty()
                        || userDto.getEmail().isBlank()
                        || validateMail(userDto))) {
            throw new EmailException("некорректный Email");
        } else if (userDto.getEmail() != null
                && userRepository.findByEmail(userDto.getEmail()) != null) {
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
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("пользователь с id " + id + " не существует"));
        return toUserDto(user);
    }

    public UserDto deleteUser(Long id) throws UserNotFoundException {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("пользователь с id " + id + " не существует"));
        userRepository.delete(user);
        return toUserDto(user);
    }
}
