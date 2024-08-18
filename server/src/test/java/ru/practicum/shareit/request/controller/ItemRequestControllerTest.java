package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemRequestService requestService;
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
    void create_whenNormallyInvoked_thenReturnOk() {
        when(requestService.create(anyLong(), any()))
                .thenReturn(requestDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(requestDto.getCreated().toString())))
                .andExpect(jsonPath("$.items.length()").value(requestDto.getItems().size()))
                .andExpect(jsonPath("$.items[0].id", is(requestDto.getItems().get(0).getId()), Long.class))
                .andExpect(jsonPath("$.items[0].name", is(requestDto.getItems().get(0).getName())))
                .andExpect(jsonPath("$.items[0].description", is(requestDto.getItems().get(0).getDescription())));

        verify(requestService, times(1)).create(anyLong(), any());
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

        verify(requestService, never()).create(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void findByUser_whenNormallyInvoked_thenReturnOk() {
        long userId = 1L;
        List<ItemRequestDto> requestDtos = new ArrayList<>();

        requestDtos.add(requestDto);
        requestDtos.add(request);

        when(requestService.findByUser(userId))
                .thenReturn(requestDtos);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(requestDtos.size()))
                .andExpect(jsonPath("$[0].id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(requestDto.getCreated().toString())))
                .andExpect(jsonPath("$[0].items.length()").value(requestDto.getItems().size()))
                .andExpect(jsonPath("$[0].items[0].id", is(requestDto.getItems().get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].name", is(requestDto.getItems().get(0).getName())))
                .andExpect(jsonPath("$[0].items[0].description", is(requestDto.getItems().get(0).getDescription())))
                .andExpect(jsonPath("$[1].id", is(request.getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(request.getDescription())))
                .andExpect(jsonPath("$[1].created", is(request.getCreated().toString())))
                .andExpect(jsonPath("$[1].items.length()").value(request.getItems().size()))
                .andExpect(jsonPath("$[1].items[0].id", is(request.getItems().get(0).getId()), Long.class))
                .andExpect(jsonPath("$[1].items[0].name", is(request.getItems().get(0).getName())))
                .andExpect(jsonPath("$[1].items[0].description", is(request.getItems().get(0).getDescription())));

        verify(requestService, times(1)).findByUser(anyLong());
    }

    @SneakyThrows
    @Test
    void findByUser_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).findByUser(anyLong());
    }

    @SneakyThrows
    @Test
    void findAll_whenNormallyInvoked_ThenReturnOk() {
        Integer from = 0;
        Integer size = 10;
        List<ItemRequestDto> requestDtos = new ArrayList<>();

        requestDtos.add(requestDto);
        requestDtos.add(request);

        when(requestService.findAll(anyLong(), any(), any()))
                .thenReturn(requestDtos);

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from",String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(requestDtos.size()))
                .andExpect(jsonPath("$[0].id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(requestDto.getCreated().toString())))
                .andExpect(jsonPath("$[0].items.length()").value(requestDto.getItems().size()))
                .andExpect(jsonPath("$[0].items[0].id", is(requestDto.getItems().get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].name", is(requestDto.getItems().get(0).getName())))
                .andExpect(jsonPath("$[0].items[0].description", is(requestDto.getItems().get(0).getDescription())))
                .andExpect(jsonPath("$[1].id", is(request.getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(request.getDescription())))
                .andExpect(jsonPath("$[1].created", is(request.getCreated().toString())))
                .andExpect(jsonPath("$[1].items.length()").value(request.getItems().size()))
                .andExpect(jsonPath("$[1].items[0].id", is(request.getItems().get(0).getId()), Long.class))
                .andExpect(jsonPath("$[1].items[0].name", is(request.getItems().get(0).getName())))
                .andExpect(jsonPath("$[1].items[0].description", is(request.getItems().get(0).getDescription())));

        verify(requestService, times(1)).findAll(anyLong(), any(), any());

    }

    @SneakyThrows
    @Test
    void findAll_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(get("/requests/all"))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).findAll(anyLong(), any(), any());
    }

    @SneakyThrows
    @Test
    void findAll_whenWithoutRequestParamFrom_thenReturnOk() {
        Integer size = 10;
        mvc.perform(get("/requests/all")
                        .param("size", String.valueOf(size))
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andDo(print());

        verify(requestService, times(1)).findAll(anyLong(), any(), any());
    }

    @SneakyThrows
    @Test
    void findAll_whenWithoutRequestParamSize_thenReturnOk() {
        Integer from = 0;
        mvc.perform(get("/requests/all")
                        .param("from",String.valueOf(from))
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(requestService, times(1)).findAll(anyLong(), any(), any());
    }

    @SneakyThrows
    @Test
    void findAll_whenWithoutRequestParamsFromAndSize_thenReturnOk() {
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(requestService, times(1)).findAll(anyLong(), any(), any());
    }

    @SneakyThrows
    @Test
    void findById_whenNormallyInvoked_thenReturnOk() {
        long requestId = 1L;
        when(requestService.findById(anyLong(), anyLong()))
                .thenReturn(requestDto);

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(requestDto.getCreated().toString())))
                .andExpect(jsonPath("$.items.length()").value(requestDto.getItems().size()))
                .andExpect(jsonPath("$.items[0].id", is(requestDto.getItems().get(0).getId()), Long.class))
                .andExpect(jsonPath("$.items[0].name", is(requestDto.getItems().get(0).getName())))
                .andExpect(jsonPath("$.items[0].description", is(requestDto.getItems().get(0).getDescription())));

        verify(requestService, times(1)).findById(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void findById_whenWithoutXSharerUserId_thenReturnBadRequest() {
        long requestId = 1L;

        mvc.perform(get("/requests/{requestId}", requestId))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).findById(anyLong(), anyLong());
    }
}