package ru.practicum.shareit.user.repository;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Setter
@Component
public class UserRepositoryImpl implements UserRepository {
    private Map<Long, User> users = new HashMap<>();
    private Long lastId = 0L;

    @Override
    public UserDto addUser(UserDto userDto) throws UserAlreadyExistsException, ValidationException, EmailException {
        if (!UserDto.validate(userDto)) {
            log.error("валидация пользователя не пройдена");
            throw new ValidationException("данные о пользователе указаны некорректно");
        } else if (userDto.getId() != null && users.get(userDto.getId()) != null
                || (userDto.getEmail() != null
                && users.values().stream()
                .filter(x -> x.getEmail().equals(userDto.getEmail()))
                .findAny()
                .isPresent())) {
            throw new UserAlreadyExistsException("пользователь уже существует");
        } else if (UserDto.validateMail(userDto)) {
            throw new EmailException("некорректный Email");
        } else {
            userDto.setId(makeId());
            User user = new User(userDto.getId(),
                    userDto.getName(),
                    userDto.getEmail());
            users.put(user.getId(), user);
            log.info("добавлен новый пользователь с id {}", user.getId());
        }
        return userDto;
    }

    @Override
    public UserDto updateUser(UserDto user, Long id) throws UserNotFoundException, EmailException, UserAlreadyExistsException {
        User oldUser = users.get(id);
        if (oldUser == null) {
            throw new UserNotFoundException("такого пользователя не существует");
        } else {
            if (user.getEmail() != null &&
                    (user.getEmail().isEmpty()
                            || user.getEmail().isBlank())) {
                throw new EmailException("некорректный Email");
            } else if (user.getEmail() != null
                    && users.values().stream()
                    .filter(x -> x.getEmail().equals(user.getEmail()))
                    .findAny()
                    .isPresent()) {
                throw new UserAlreadyExistsException("пользователь с таким Email уже существует");
            } else if (user.getEmail() != null) {
                oldUser.setEmail(user.getEmail());
            }
            if (user.getName() != null) {
                oldUser.setName(user.getName());
            }
            users.put(id, oldUser);
        }
        log.info("информация для пользователя с id {} обновлена", id);
        return UserMapper.toUserDto(oldUser);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("текущее количество пользователей: {}", users.size());
        return new ArrayList<>(users.values()).stream()
                .map(x -> UserMapper.toUserDto(x))
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) throws UserNotFoundException {
        User user = users.get(id);
        if (user == null) {
            throw new UserNotFoundException("пользователь с id " + id + " не существует");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto deleteUser(Long id) throws UserNotFoundException {
        User user = users.get(id);
        if (user == null) {
            throw new UserNotFoundException("пользователь с id " + id + " не существует");
        } else {
            users.remove(id);
        }
        return UserMapper.toUserDto(user);
    }

    public Long makeId() {
        return ++lastId;
    }
}
