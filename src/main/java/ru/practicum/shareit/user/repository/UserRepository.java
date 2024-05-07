package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
    User add(User user);

    User update(User user);

    void remove(long userId);

    Collection<User> getAll();

    Optional<User> getById(long userId);
}