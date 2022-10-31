package ru.practicum.shareit.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.user.UserMapper.toUserDto;

public class UserServiceTest {
    private UserService userService;
    private UserJpaRepository userRepository;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void beforeEach() {
        userRepository = mock(UserJpaRepository.class);
        userService = new UserServiceImpl(userRepository);
        user = new User(1L, "user 1", "user1@email");
        userDto = toUserDto(user);
    }

    @AfterEach
    void afterEach() {
        user.setEmail("user1@email");
        user.setId(1L);
    }

    @Test
    void getAllUsers() {
        final List<User> users = List.of(user);
        when(userRepository.findAll())
                .thenReturn(users);

        final List<UserDto> userDtos = userService.getAllUsers();

        assertNotNull(userDtos);
        assertEquals(1, userDtos.size());
        assertEquals(userDto, userDtos.get(0));
    }

    @Test
    void addUser() throws Exception {
        when(userRepository.save(user)).thenReturn(user);
        UserDto userDtoSample = userService.addUser(toUserDto(user));

        assertNotNull(userDto);
        assertEquals(userDto, userDtoSample);
    }

    @Test
    void addUserEmptyEmail() {
        user.setEmail("");
        assertThrows(ValidationException.class, () -> userService.addUser(toUserDto(user)));

        user.setEmail(" ");
        assertThrows(ValidationException.class, () -> userService.addUser(toUserDto(user)));
    }

    @Test
    void addUserInvalidEmail() {
        user.setEmail("b");
        assertThrows(EmailException.class, () -> userService.addUser(toUserDto(user)));
    }

    @Test
    void addUserIncorrectId() {
        user.setId(-1L);
        assertThrows(ValidationException.class, () -> userService.addUser(toUserDto(user)));
    }

    @Test
    void updateUser() throws Exception {
        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserDto userDtoSample = userService.updateUser(toUserDto(user), user.getId());

        assertNotNull(userDto);
        assertEquals(userDto, userDtoSample);
    }

    @Test
    void updateUserDuplicate() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(toUserDto(user), 1L));
    }

    @Test
    void updateUserIncorrectId() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        user.setId(999L);
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(toUserDto(user), 999L));
    }

    @Test
    void updateUserInvalidEmail() {
        user.setEmail("b");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(EmailException.class, () -> userService.updateUser(toUserDto(user), 1L));
    }

    @Test
    void getUserById() throws Exception {
        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserDto userDtoSample = userService.getUserById(user.getId());

        assertNotNull(userDto);
        assertEquals(userDto, userDtoSample);
    }

    @Test
    void getUserByIncorrectId() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        user.setId(999L);
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void deleteUser() throws Exception {
        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserDto userDtoSample = userService.deleteUser(user.getId());

        assertNotNull(userDto);
        assertEquals(userDto, userDtoSample);
    }

    @Test
    void deleteUserByIncorrectId() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        user.setId(999L);
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(999L));
    }

}
