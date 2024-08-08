package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.exception.ConflictException;
import ru.practicum.shareit.exception.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UpdatedUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userService;
    private final UserDto userDto = UserDto.builder()
            .name("UserNameTest")
            .email("userEmail@test.ru")
            .build();

    private final User user = User.builder()
            .id(1L)
            .name(userDto.getName())
            .email(userDto.getEmail())
            .build();

    private final UserDto expectedUser = UserDto.builder()
            .id(1L)
            .name(user.getName())
            .email(user.getEmail())
            .build();

    @Test
    void create_whenNormallyInvoked_thenReturnUserDto() {
        when(userMapper.fromUserDto(userDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDto(user)).thenReturn(expectedUser);

        UserDto actualUser = userService.create(userDto);

        assertEquals(expectedUser, actualUser);
    }

    @Test
    void create_whenCreatingWithExistingEmail_thenReturnConflictException() {
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.create(userDto));

        verify(userRepository, never()).save(user);
    }

    @Test
    void update_whenNormallyUpdateNameAndEmail_thenReturnUserDto() {
        long userId = 1L;
        UpdatedUserDto updatedUserDto = UpdatedUserDto.builder()
                .id(userId)
                .name("UpdatedName")
                .email("UpdatedEmail@mail.ru")
                .build();

        User updatedUser = User.builder()
                .id(user.getId())
                .name(updatedUserDto.getName())
                .email(updatedUserDto.getEmail())
                .build();

        UserDto expectedUser = UserDto.builder()
                .id(updatedUser.getId())
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(userMapper.toUserDto(updatedUser)).thenReturn(expectedUser);

        UserDto actualUser = userService.update(userId, updatedUserDto);

        assertEquals(expectedUser, actualUser);
    }

    @Test
    void update_whenNormallyUpdateNameOnly_thenReturnUserDto() {
        long userId = 1L;
        UpdatedUserDto updatedUserDto = UpdatedUserDto.builder()
                .id(userId)
                .name("UpdatedName")
                .build();

        User updatedUser = User.builder()
                .id(user.getId())
                .name(updatedUserDto.getName())
                .email(user.getEmail())
                .build();

        UserDto expectedUser = UserDto.builder()
                .id(updatedUser.getId())
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(userMapper.toUserDto(updatedUser)).thenReturn(expectedUser);

        UserDto actualUser = userService.update(userId, updatedUserDto);

        assertEquals(expectedUser, actualUser);
    }

    @Test
    void update_whenNormallyUpdateEmailOnly_thenReturnUserDto() {
        long userId = 1L;
        UpdatedUserDto updatedUserDto = UpdatedUserDto.builder()
                .id(userId)
                .email("UpdatedEmail@mail.ru")
                .build();

        User updatedUser = User.builder()
                .id(user.getId())
                .name(user.getName())
                .email(updatedUserDto.getEmail())
                .build();

        UserDto expectedUser = UserDto.builder()
                .id(updatedUser.getId())
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(userMapper.toUserDto(updatedUser)).thenReturn(expectedUser);

        UserDto actualUser = userService.update(userId, updatedUserDto);

        assertEquals(expectedUser, actualUser);
    }

    @Test
    void update_whenUpdatingUnknownUser_thenReturnNotFoundException() {
        long userId = 1L;
        UpdatedUserDto updatedUserDto = UpdatedUserDto.builder()
                .id(userId)
                .email("UpdatedEmail@mail.ru")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(userId, updatedUserDto));
    }

    @Test
    void update_whenUpdatingWithExistingEmail_thenReturnConflictException() {
        long userId = 1L;
        UpdatedUserDto updatedUserDto = UpdatedUserDto.builder()
                .id(userId)
                .name("UpdatedName")
                .email("UpdatedEmail@mail.ru")
                .build();

        User updatedUser = User.builder()
                .id(user.getId())
                .name(updatedUserDto.getName())
                .email(updatedUserDto.getEmail())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(updatedUserDto.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.update(userId, updatedUserDto));

        verify(userRepository, never()).save(updatedUser);
    }

    @Test
    void findById_whenNormallyInvoked_thenReturnUserDto() {
        long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserDto(user)).thenReturn(expectedUser);

        UserDto actualUser = userService.findById(userId);

        assertEquals(expectedUser, actualUser);
    }

    @Test
    void findById_whenFindingUnknownUser_thenReturnNotFoundException() {
        long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.findById(userId));
    }

    @Test
    void findAll_whenNormallyInvoked_thenReturnListUsersDto() {
        User userForList = User.builder()
                .id(2L)
                .name("UserName2")
                .email("UserEmail2@mail.ru")
                .build();
        UserDto userDtoForList = UserDto.builder()
                .id(2L)
                .name("UserDtoName2")
                .email("UserDtoEmail2@mail.ru")
                .build();
        List<User> userList = List.of(user, userForList);
        List<UserDto> expectedList = List.of(userDto, userDtoForList);

        when(userRepository.findAll()).thenReturn(userList);
        when(userMapper.toUserDto(user)).thenReturn(userDto);
        when(userMapper.toUserDto(userForList)).thenReturn(userDtoForList);

        List<UserDto> actualList = userService.findAll();

        assertEquals(expectedList, actualList);
    }

    @Test
    void findAll_whenNoUsersExist_thenReturnEmptyListUsersDto() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        assertTrue(userService.findAll().isEmpty());
    }

    @Test
    void deleteById() {
        long userId = 1L;

        userService.deleteById(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }
}