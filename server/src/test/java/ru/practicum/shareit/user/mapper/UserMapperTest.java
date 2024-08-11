package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserMapperTest {

    private UserMapper userMapper;
    private User user;
    private UserDto userDto;

    @BeforeEach
    public void setUp() {
        userMapper = new UserMapper();
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();
        userDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();
    }

    @Test
    public void testToUserDto() {
        UserDto mappedUserDto = userMapper.toUserDto(user);

        assertEquals(user.getId(), mappedUserDto.getId());
        assertEquals(user.getName(), mappedUserDto.getName());
        assertEquals(user.getEmail(), mappedUserDto.getEmail());
    }

    @Test
    public void testFromUserDto() {
        User mappedUser = userMapper.fromUserDto(userDto);

        assertEquals(userDto.getId(), mappedUser.getId());
        assertEquals(userDto.getName(), mappedUser.getName());
        assertEquals(userDto.getEmail(), mappedUser.getEmail());
    }
}
