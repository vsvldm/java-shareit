package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UpdatedUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    UserService userService;
    @Autowired
    private MockMvc mvc;
    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("UserTest")
            .email("usertest@mail.ru")
            .build();

    @SneakyThrows
    @Test
    void create_whenNormallyInvoked_thenReturnOk() {
        when(userService.create(any()))
                .thenReturn(userDto);

        mvc.perform(post("/users")
                                .content(mapper.writeValueAsString(userDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1)).create(userDto);
    }

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

        verify(userService, never()).create(userWithoutEmailDto);
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

        verify(userService, never()).create(userEmailNotValidDto);
    }

    @SneakyThrows
    @Test
    void update_whenNormallyUpdateOnlyName_thenReturnOk() {
        long userId = 1L;
        UpdatedUserDto userToUpdate = UpdatedUserDto.builder()
                .name("UpdateUser")
                .build();
        UserDto updatedUser = UserDto.builder()
                .id(userDto.getId())
                .name("UpdateUser")
                .email(userDto.getEmail())
                .build();

        when(userService.update(userId, userToUpdate))
                .thenReturn(updatedUser);

        mvc.perform(patch("/users/{userId}", userId)
                        .content(mapper.writeValueAsString(userToUpdate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedUser.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updatedUser.getName())))
                .andExpect(jsonPath("$.email", is(updatedUser.getEmail())));

        verify(userService, times(1)).update(userId, userToUpdate);
    }

    @SneakyThrows
    @Test
    void update_whenNormallyUpdateOnlyEmail_thenReturnOk() {
        long userId = 1L;
        UpdatedUserDto userToUpdate = UpdatedUserDto.builder()
                .email("updateemail@mail.ru")
                .build();
        UserDto updatedUser = UserDto.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email("updateemail@mail.ru")
                .build();

        when(userService.update(userId, userToUpdate))
                .thenReturn(updatedUser);

        mvc.perform(patch("/users/{userId}", userId)
                        .content(mapper.writeValueAsString(userToUpdate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedUser.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updatedUser.getName())))
                .andExpect(jsonPath("$.email", is(updatedUser.getEmail())));

        verify(userService, times(1)).update(userId, userToUpdate);
    }

    @SneakyThrows
    @Test
    void update_whenNormallyUpdateNameAndEmail_thenReturnOk() {
        long userId = 1L;
        UpdatedUserDto userToUpdate = UpdatedUserDto.builder()
                .name("UpdateUser")
                .email("updateemail@mail.ru")
                .build();
        UserDto updatedUser = UserDto.builder()
                .id(userDto.getId())
                .name("UpdateUser")
                .email("updateemail@mail.ru")
                .build();

        when(userService.update(userId, userToUpdate))
                .thenReturn(updatedUser);

        mvc.perform(patch("/users/{userId}", userId)
                        .content(mapper.writeValueAsString(userToUpdate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedUser.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updatedUser.getName())))
                .andExpect(jsonPath("$.email", is(updatedUser.getEmail())));

        verify(userService, times(1)).update(userId, userToUpdate);
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

        verify(userService, never()).update(userId, userToUpdate);
    }

    @SneakyThrows
    @Test
    void findById_whenNormallyInvoked_thenReturnOk() {
        long userId = 1L;

        when(userService.findById(userId))
                .thenReturn(userDto);

        mvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService).findById(userId);
    }

    @SneakyThrows
    @Test
    void findAll_whenNormallyInvoked_thenReturnOk() {
        UserDto user = UserDto.builder()
                .id(2L)
                .name("user2")
                .email("user2@email.ru")
                .build();
        List<UserDto> userDtoList = new ArrayList<>();

        userDtoList.add(userDto);
        userDtoList.add(user);

        when(userService.findAll())
                .thenReturn(userDtoList);

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(userDtoList.size()))
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$[0].email", is(userDto.getEmail())))
                .andExpect(jsonPath("$[1].id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(user.getName())))
                .andExpect(jsonPath("$[1].email", is(user.getEmail())));

        verify(userService,times(1)).findAll();
    }

    @SneakyThrows
    @Test
    void deleteById() {
        long userId = 1L;

        mvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteById(userId);
    }
}