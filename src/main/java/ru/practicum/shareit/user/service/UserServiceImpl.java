package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.exception.ConflictException;
import ru.practicum.shareit.exception.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UpdatedUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        log.info("UserService: Beginning of method execution create().");
        log.info("create(): Add the user to the database.");
        try {
            User addedUser = userRepository.save(userMapper.fromUserDto(userDto));

            log.info("crate(): User with id = {} successfully added to database.", addedUser.getId());
            return userMapper.toUserDto(addedUser);
        } catch (RuntimeException e) {
            log.error("create(): Conflict when saving data. A user with this email already exists.");
            throw new ConflictException("Conflict when saving data. A user with this email already exists.");
        }
    }

    @Override
    @Transactional
    public UserDto update(long userId, UpdatedUserDto updatedUserDto) {
        log.info("UserService: Beginning of method execution update().");
        log.info("update(): Checking the existence of a user with id = {}.", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("update(): User with id = {} not found", userId);
                    return new NotFoundException(String.format("User with id = %d not found", userId));
                });

        log.info("update(): Searching and updating information in the database.");
        if (updatedUserDto.getName() != null) {
            existingUser.setName(updatedUserDto.getName());
            log.info("update(): Update UserName with id = {}", userId );
        }
        if (updatedUserDto.getEmail() != null) {
            existingUser.setEmail(updatedUserDto.getEmail());
            log.info("update(): Update UserEmail with id = {}", userId );
        }

        try {
            User updatedUser = userRepository.save(existingUser);

            log.info("update(): User with id = {} successfully updated in database.", updatedUser.getId());
            return userMapper.toUserDto(updatedUser);
        } catch (RuntimeException e){
            log.error("update(): Conflict when saving data. A user with this email already exists.");
            throw new ConflictException("Conflict when saving data. A user with this email already exists.");
        }

    }

    @Override
    public UserDto findById(long userId) {
        log.info("UserService: Beginning of method execution findById().");
        log.info("findById(): Searching user with id = {}.", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("findById(): User with id = {} not found", userId);
                    return new NotFoundException(String.format("User with id = %d not found", userId));
                });

        log.info("findById(): Search for user with id = {} successful completed.", userId);
        return userMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        log.info("UserService: Beginning of method execution findAll().");

        log.info("findAll(): Searching all users.");
        List<UserDto> userDtoList = userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());

        log.info("findAll(): Search for all users successful completed.");
        return userDtoList;
    }

    @Override
    @Transactional
    public void deleteById(long userId) {
        log.info("UserService: Beginning of method execution deleteById().");
        userRepository.deleteById(userId);
        log.info("deleteById(): User with id = {} successfully deleted.", userId);
    }
}
