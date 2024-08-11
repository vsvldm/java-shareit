package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ReturnItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemService itemService;
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
    private final ReturnItemDto returnItemDto = ReturnItemDto.builder()
            .id(itemDto.getId())
            .name(itemDto.getName())
            .description(itemDto.getDescription())
            .build();


    @SneakyThrows
    @Test
    void create_whenNormallyInvoked_thenReturnOk() {
        when(itemService.create(anyLong(), any()))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId())));

        verify(itemService, times(1)).create(anyLong(), any());
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

        verify(itemService, never()).create(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void update_whenNormallyUpdateOnlyName_thenReturnOk() {
        ItemDto itemNameUpdated = ItemDto.builder()
                .id(itemDto.getId())
                .name("UpdatedName")
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .requestId(itemDto.getRequestId())
                .build();
        when(itemService.update(anyLong(), anyLong(), any()))
                .thenReturn(itemNameUpdated);

        mvc.perform(patch("/items/{itemId}", itemId)
                        .content(mapper.writeValueAsString(itemNameUpdated))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemNameUpdated.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemNameUpdated.getName())))
                .andExpect(jsonPath("$.description", is(itemNameUpdated.getDescription())))
                .andExpect(jsonPath("$.available", is(itemNameUpdated.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemNameUpdated.getRequestId())));

        verify(itemService, times(1)).update(anyLong(), anyLong(), any());
    }

    @SneakyThrows
    @Test
    void update_whenNormallyUpdateOnlyDescription_thenReturnOk() {
        ItemDto itemUpdatedDescription = ItemDto.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description("UpdatedDescription")
                .available(itemDto.getAvailable())
                .requestId(itemDto.getRequestId())
                .build();
        when(itemService.update(anyLong(), anyLong(), any()))
                .thenReturn(itemUpdatedDescription);

        mvc.perform(patch("/items/{itemId}", itemId)
                        .content(mapper.writeValueAsString(itemUpdatedDescription))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemUpdatedDescription.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemUpdatedDescription.getName())))
                .andExpect(jsonPath("$.description", is(itemUpdatedDescription.getDescription())))
                .andExpect(jsonPath("$.available", is(itemUpdatedDescription.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemUpdatedDescription.getRequestId())));

        verify(itemService, times(1)).update(anyLong(), anyLong(), any());
    }

    @SneakyThrows
    @Test
    void update_whenNormallyUpdateOnlyAvailable_thenReturnOk() {
        ItemDto itemAvailableUpdated = ItemDto.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(false)
                .requestId(itemDto.getRequestId())
                .build();
        when(itemService.update(anyLong(), anyLong(), any()))
                .thenReturn(itemAvailableUpdated);

        mvc.perform(patch("/items/{itemId}", itemId)
                        .content(mapper.writeValueAsString(itemAvailableUpdated))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemAvailableUpdated.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemAvailableUpdated.getName())))
                .andExpect(jsonPath("$.description", is(itemAvailableUpdated.getDescription())))
                .andExpect(jsonPath("$.available", is(itemAvailableUpdated.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemAvailableUpdated.getRequestId())));

        verify(itemService, times(1)).update(anyLong(), anyLong(), any());
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

        verify(itemService, never()).update(anyLong(), anyLong(), any());
    }



    @SneakyThrows
    @Test
    void findById_whenNormallyInvoked_thenReturnOk() {
        when(itemService.findById(anyLong(), anyLong()))
                .thenReturn(returnItemDto);

        mvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(returnItemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(returnItemDto.getName())))
                .andExpect(jsonPath("$.description", is(returnItemDto.getDescription())));

        verify(itemService, times(1)).findById(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void findById_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).findById(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void findAllByOwner_whenNormallyInvokedWithAllParams_thenReturnOk() {
        Integer from = 0;
        Integer size = 10;
        List<ReturnItemDto> itemsByOwner = List.of(returnItemDto);

        when(itemService.findByOwner(anyLong(), anyInt(), anyInt()))
                .thenReturn(itemsByOwner);

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(itemsByOwner.size()));

        verify(itemService, times(1)).findByOwner(anyLong(),anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void findAllByOwner_whenNormallyInvokedWithoutAllParams_thenReturnOk() {
        List<ReturnItemDto> itemsByOwner = List.of(returnItemDto);

        when(itemService.findByOwner(anyLong(), any(), any()))
                .thenReturn(itemsByOwner);

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(itemsByOwner.size()));

        verify(itemService, times(1)).findByOwner(anyLong(),any(), any());
    }

    @SneakyThrows
    @Test
    void findAllByOwner_whenNormallyInvokedWithoutFromParam_thenReturnOk() {
        Integer size = 10;
        List<ReturnItemDto> itemsByOwner = List.of(returnItemDto);

        when(itemService.findByOwner(anyLong(), any(), any()))
                .thenReturn(itemsByOwner);

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(itemsByOwner.size()));

        verify(itemService, times(1)).findByOwner(anyLong(),any(), any());
    }

    @SneakyThrows
    @Test
    void findAllByOwner_whenNormallyInvokedWithoutSizeParam_thenReturnOk() {
        Integer from = 0;
        List<ReturnItemDto> itemsByOwner = List.of(returnItemDto);

        when(itemService.findByOwner(anyLong(), any(), any()))
                .thenReturn(itemsByOwner);

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(itemsByOwner.size()));

        verify(itemService, times(1)).findByOwner(anyLong(),any(), any());
    }

    @SneakyThrows
    @Test
    void findAllByOwner_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(get("/items", itemId))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).findByOwner(anyLong(), any(), any());
    }

    @SneakyThrows
    @Test
    void search_whenNormallyInvokedWithAllParams_thenReturnOk() {
        Integer from = 0;
        Integer size = 10;
        String text = "Text";
        List<ItemDto> searchedItemsDto = List.of(itemDto);

        when(itemService.search(anyLong(), any(), any(), any()))
                .thenReturn(searchedItemsDto);

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", text)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(searchedItemsDto.size()));

        verify(itemService, times(1)).search(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void search_whenNormallyInvokedWithoutFromAndSizeParams_thenReturnOk() {
        String text = "Text";
        List<ItemDto> searchedItemsDto = List.of(itemDto);

        when(itemService.search(anyLong(), any(), any(), any()))
                .thenReturn(searchedItemsDto);

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", text))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(searchedItemsDto.size()));

        verify(itemService, times(1)).search(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void search_whenNormallyInvokedWithoutFromParam_thenReturnOk() {
        Integer size = 10;
        String text = "Text";
        List<ItemDto> searchedItemsDto = List.of(itemDto);

        when(itemService.search(anyLong(), any(), any(), any()))
                .thenReturn(searchedItemsDto);

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", text)
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(searchedItemsDto.size()));

        verify(itemService, times(1)).search(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void search_whenNormallyInvokedWithoutSizeParam_thenReturnOk() {
        Integer from = 0;
        String text = "Text";
        List<ItemDto> searchedItemsDto = List.of(itemDto);

        when(itemService.search(anyLong(), any(), any(), any()))
                .thenReturn(searchedItemsDto);

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", text)
                        .param("from", String.valueOf(from)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(searchedItemsDto.size()));

        verify(itemService, times(1)).search(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void search_whenWithoutTextParam_thenReturnBadRequest() {
        Integer from = 0;
        Integer size = 10;
        List<ItemDto> searchedItemsDto = List.of(itemDto);

        when(itemService.search(anyLong(), any(), any(), any()))
                .thenReturn(searchedItemsDto);

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(itemService, never()).search(anyLong(), any(), any(), any());
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

        verify(itemService, never()).search(anyLong(), anyString(), any(), any());
    }

    @SneakyThrows
    @Test
    void createComment_whenNormallyInvoked_thenReturnOk() {
        CommentDto comment = CommentDto.builder()
                .text("CommentText")
                .build();

        when(itemService.createComment(anyLong(), anyLong(), any()))
                .thenReturn(comment);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(comment.getText())))
                .andDo(print());

        verify(itemService, times(1)).createComment(anyLong(), anyLong(), any());
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

        verify(itemService, never()).createComment(anyLong(), anyLong(), any());
    }
}