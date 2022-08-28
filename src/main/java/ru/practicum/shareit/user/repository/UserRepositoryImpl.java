package ru.practicum.shareit.user.repository;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.EmailException;
import ru.practicum.shareit.user.UserAlreadyExistsException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.ValidationException;
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
    public User addUser(User user) throws UserAlreadyExistsException, ValidationException, EmailException {
        if (!User.validate(user)) {
            log.error("валидация пользователя не пройдена");
            throw new ValidationException("данные о пользователе указаны некорректно");
        } else if (user.getId() != null && users.get(user.getId()) != null || (user.getEmail() != null && users.values().stream().filter(x -> x.getEmail().equals(user.getEmail())).collect(Collectors.toList()).size() > 0)) {
            throw new UserAlreadyExistsException("пользователь уже существует");
        } else if (User.validateMail(user)) {
            throw new EmailException("некорректный Email");
        } else {
            user.setId(makeId());
            users.put(user.getId(), user);
            log.info("добавлен новый пользователь с id {}", user.getId());
        }
        return user;
    }

    @Override
    public User updateUser(User user, Long id) throws UserNotFoundException, EmailException, UserAlreadyExistsException {
        User oldUser = users.get(id);
        if (oldUser == null) {
            throw new UserNotFoundException("такого пользователя не существует");
        } else {
            if (user.getEmail() != null &&
                    (user.getEmail().isEmpty()
                    || user.getEmail().isBlank())) {
                throw new EmailException("некорректный Email");
            } else if (user.getEmail() != null && users.values().stream().filter(x -> x.getEmail().equals(user.getEmail())).collect(Collectors.toList()).size() > 0) {
                throw new UserAlreadyExistsException("пользователь с таким Email уже существует");
            } else if (user.getEmail() != null) {
                oldUser.setEmail(user.getEmail());
            }
            if (user.getName() != null) {
                oldUser.setName(user.getName());
            }
            users.remove(id);
            users.put(id, oldUser);
        }
        log.info("информация для пользователя с id {} обновлена", id);
        return oldUser;
    }

    @Override
    public List<User> getAllUsers() {
        log.info("текущее количество пользователей: {}", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(Long id) throws UserNotFoundException {
        User user = users.get(id);
        if (user == null) {
            throw new UserNotFoundException("пользователь с id " + id + " не существует");
        }
        return user;
    }

    @Override
    public User deleteUser(Long id) throws UserNotFoundException {
        User user = users.get(id);
        if (user == null) {
            throw new UserNotFoundException("пользователь с id " + id + " не существует");
        } else {
            users.remove(id);
        }
        return user;
    }

    public Long makeId() {
        return ++lastId;
    }
}
