package ru.practicum.shareit.item.controller.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserClient;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UpdatedUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserValidationTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    UserClient userClient;
    @Autowired
    private MockMvc mvc;
    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("UserTest")
            .email("usertest@mail.ru")
            .build();

    @SneakyThrows
    @Test
    void create_whenUserWithoutEmail_thenReturnBadRequest() {
        UserDto userWithoutEmailDto = UserDto.builder()
                .id(1L)
                .name("UserTest")
                .build();

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userWithoutEmailDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).create(userWithoutEmailDto);
    }

    @SneakyThrows
    @Test
    void create_whenUserEmailNotValid_thenReturnBadRequest() {
        UserDto userEmailNotValidDto = UserDto.builder()
                .id(1L)
                .name("UserTest")
                .email("usertestmail.ru")
                .build();

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userEmailNotValidDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).create(userEmailNotValidDto);
    }

    @SneakyThrows
    @Test
    void update_whenFailUpdateEmail_thenReturnBadRequest() {
        long userId = 1L;
        UpdatedUserDto userToUpdate = UpdatedUserDto.builder()
                .email("updateemailmail.ru")
                .build();

        mvc.perform(patch("/users/{userId}", userId)
                        .content(mapper.writeValueAsString(userToUpdate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).update(userId, userToUpdate);
    }
}