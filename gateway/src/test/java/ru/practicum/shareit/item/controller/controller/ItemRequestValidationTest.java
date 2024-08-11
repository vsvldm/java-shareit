package ru.practicum.shareit.item.controller.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestValidationTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    ru.practicum.shareit.request.client.ItemRequestClient itemRequestClient;
    @Autowired
    private MockMvc mvc;
    private final ItemRequestDto requestDto = ItemRequestDto.builder()
            .id(1L)
            .description("TestRequestDescription")
            .created(LocalDateTime.of(2024,6,7,7,5, 1))
            .items(List.of(ResponseItemDto.builder()
                            .id(1L)
                            .name("TestName")
                            .description("TestResponseDescription")
                    .build()))
            .build();
    private final ItemRequestDto request = ItemRequestDto.builder()
            .id(2L)
            .description("TestRequestDescription2")
            .created(LocalDateTime.of(2024,6,7,7,5, 1))
            .items(List.of(ResponseItemDto.builder()
                    .id(2L)
                    .name("TestName2")
                    .description("TestResponseDescription2")
                    .build()))
            .build();

    @SneakyThrows
    @Test
    void create_whenItemRequestFailDescription_thenReturnBadRequest() {
        ItemRequestDto requestFail = ItemRequestDto.builder()
                .id(1L)
                .description(null)
                .created(LocalDateTime.of(2024,6,7,7,5, 1))
                .items(null)
                .build();

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestFail))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).create(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void create_whenItemRequestFailCreated_thenReturnBadRequest() {
        ItemRequestDto requestFail = ItemRequestDto.builder()
                .id(1L)
                .description("Description")
                .created(LocalDateTime.of(2030,6,7,7,5, 1))
                .items(null)
                .build();

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestFail))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).create(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void create_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).create(anyLong(), any());
    }


    @SneakyThrows
    @Test
    void findByUser_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).findByUser(anyLong());
    }

    @SneakyThrows
    @Test
    void findAll_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(get("/requests/all"))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).findAll(anyLong(), any(), any());
    }

    @SneakyThrows
    @Test
    void findById_whenWithoutXSharerUserId_thenReturnBadRequest() {
        long requestId = 1L;

        mvc.perform(get("/requests/{requestId}", requestId))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).findById(anyLong(), anyLong());
    }
}