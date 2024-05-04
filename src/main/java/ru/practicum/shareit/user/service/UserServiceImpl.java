package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.exception.ConflictException;
import ru.practicum.shareit.exception.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto create(UserDto userDto) {
        log.info("UserService: Beginning of method execution create().");
        User user = userMapper.fromUserDto(userDto);

        log.info("create(): Uniqueness check email = {}.", user.getEmail());
        for (User existingUser : userRepository.getAll()) {
            if (existingUser.getEmail().equals(user.getEmail())) {
                throw new ConflictException(String.format("User with email = %s already exists", user.getEmail()));
            }
        }

        log.info("create(): Add the user to the database.");
        User addedUser = userRepository.add(user);

        log.info("crate(): User with id = {} successfully added to database.", addedUser.getId());
        return userMapper.toUserDto(addedUser);
    }

    @Override
    public UserDto update(long userId, Map<String, Object> fields) {
        log.info("UserService: Beginning of method execution update().");
        log.info("update(): Checking the existence of a user with id = {}.", userId);
        User existingUser = userRepository.getById(userId).orElseThrow(
                () -> new NotFoundException(String.format("User with id = %s not found", userId))
        );

        log.info("update(): Searching and updating information in the database.");
        fields.forEach((key, value) -> {
            try {
                Field field = User.class.getDeclaredField(key);
                field.setAccessible(true);
                field.set(existingUser, value);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ConflictException(String.format("Field %s not found or access to it is limited.", key));
            }
        });
        User user = userRepository.update(existingUser);

        log.info("update(): User with id = {} successfully updated in database.", user.getId());
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto findById(long userId) {
        log.info("UserService: Beginning of method execution findById().");
        log.info("findById(): Searching user with id = {}.", userId);
        User user = userRepository.getById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id = %d not foud", userId)));

        log.info("findById(): Search for user with id ={} successful completed.", userId);
        return userMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        log.info("UserService: Beginning of method execution findAll().");

        log.info("findAll(): Searching all users.");
        List<UserDto> userDtos = userRepository.getAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());

        log.info("findAll(): Search for all users successful completed.");
        return userDtos;
    }

    @Override
    public void deleteById(long userId) {
        log.info("UserService: Beginning of method execution deleteById().");
        userRepository.remove(userId);
        log.info("deleteById(): User with id = {} successfully deleted.", userId);
    }
}
