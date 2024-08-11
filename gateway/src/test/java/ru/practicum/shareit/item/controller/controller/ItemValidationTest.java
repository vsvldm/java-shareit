package ru.practicum.shareit.item.controller.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemClient;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemValidationTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemClient itemClient;
    @Autowired
    private MockMvc mvc;
    private final long userId = 1L;
    private final long itemId = 1L;
    private final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("TestName")
            .description("TestDescription")
            .available(true)
            .requestId(null)
            .build();

    @SneakyThrows
    @Test
    void create_whenNameIsNull_thenReturnBadRequest() {
        ItemDto failItemDto = ItemDto.builder()
                .id(1L)
                .description("TestDescription")
                .available(true)
                .requestId(null)
                .build();

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(failItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).create(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void create_whenNameIsBlank_thenReturnBadRequest() {
        ItemDto failItemDto = ItemDto.builder()
                .id(1L)
                .name("")
                .description("TestDescription")
                .available(true)
                .requestId(null)
                .build();

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(failItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).create(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void create_whenDescriptionIsNull_thenReturnBadRequest() {
        ItemDto failItemDto = ItemDto.builder()
                .id(1L)
                .name("TestName")
                .available(true)
                .requestId(null)
                .build();

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(failItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).create(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void create_whenDescriptionIsBlank_thenReturnBadRequest() {
        ItemDto failItemDto = ItemDto.builder()
                .id(1L)
                .name("TestName")
                .description("")
                .available(true)
                .requestId(null)
                .build();

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(failItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).create(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void create_whenAvailableIsNull_thenReturnBadRequest() {
        ItemDto failItemDto = ItemDto.builder()
                .id(1L)
                .name("TestName")
                .description("TestDescription")
                .requestId(null)
                .build();

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(failItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).create(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void create_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).create(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void update_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(patch("/items/{itemId}", itemId)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).update(anyLong(), anyLong(), any());
    }

    @SneakyThrows
    @Test
    void findById_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).findById(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void findAllByOwner_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(get("/items", itemId))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).findByOwner(anyLong(), any(), any());
    }

    @SneakyThrows
    @Test
    void search_whenWithoutTextParam_thenReturnBadRequest() {
        Integer from = 0;
        Integer size = 10;

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).search(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void search_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(get("/items/search")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).search(anyLong(), anyString(), any(), any());
    }

    @SneakyThrows
    @Test
    void createComment_whenTextInCommentIsNull_thenReturnBadRequest() {
        CommentDto comment = CommentDto.builder()
                .build();

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(itemClient, never()).createComment(anyLong(), anyLong(), any());
    }

    @SneakyThrows
    @Test
    void createComment_whenTextInCommentIsEmpty_thenReturnBadRequest() {
        CommentDto comment = CommentDto.builder()
                .text("")
                .build();

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(itemClient, never()).createComment(anyLong(), anyLong(), any());
    }

    @SneakyThrows
    @Test
    void createComment_whenWithoutXSharerUserId_thenReturnBadRequest() {
        CommentDto comment = CommentDto.builder()
                .text("CommentText")
                .build();
        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createComment(anyLong(), anyLong(), any());
    }
}