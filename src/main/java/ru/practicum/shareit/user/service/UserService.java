package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserDto create(UserDto user);

    UserDto update(long userId, Map<String, Object> fields);

    UserDto findById(long userId);

    List<UserDto> findAll();

    void deleteById(long userId);
}