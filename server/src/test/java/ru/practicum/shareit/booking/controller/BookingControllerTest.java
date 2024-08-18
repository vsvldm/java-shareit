package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.ReceivingBookingDto;
import ru.practicum.shareit.booking.dto.ReturnBookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    BookingService bookingService;
    @Autowired
    private MockMvc mvc;
    private final long userId = 1L;
    private final long bookingId = 1L;
    private final Integer from = 0;
    private final Integer size = 10;
    private final ReceivingBookingDto bookingDto = ReceivingBookingDto.builder()
            .id(1L)
            .itemId(1L)
            .start(LocalDateTime.now().plusMinutes(5))
            .end(LocalDateTime.now().plusDays(4L))
            .build();
    private final ReturnBookingDto returnBookingDto = ReturnBookingDto.builder().build();
    private final BookingState state = BookingState.CURRENT;
    private final List<ReturnBookingDto> listReturnBookingDtos = List.of(returnBookingDto);

    @SneakyThrows
    @Test
    void create_whenNormallyInvoked_thenReturnOk() {
        when(bookingService.create(anyLong(), any()))
                .thenReturn(returnBookingDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andDo(print())
                .andExpect(status().isOk());

        verify(bookingService, times(1)).create(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void create_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).create(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void statusUpdate_whenNormallyInvoked_thenReturnOk() {
        boolean approved = false;

        when(bookingService.statusUpdate(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(returnBookingDto);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", String.valueOf(approved)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(bookingService, times(1)).statusUpdate(anyLong(), anyLong(), anyBoolean());
    }

    @SneakyThrows
    @Test
    void statusUpdate_whenWithoutApprovedParam_thenReturnBadRequest() {
        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).statusUpdate(anyLong(), anyLong(), anyBoolean());
    }

    @SneakyThrows
    @Test
    void statusUpdate_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).statusUpdate(anyLong(), anyLong(), anyBoolean());
    }

    @SneakyThrows
    @Test
    void findById_whenNormallyInvoked_thenReturnOk() {
        when(bookingService.findById(anyLong(), anyLong()))
                .thenReturn(returnBookingDto);

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andDo(print());

        verify(bookingService, times(1)).findById(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void findById_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(get("/bookings/{bookingId}", bookingId))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(bookingService, never()).findById(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void findAllByBookerId_whenNormallyInvoked_thenReturnOk() {
        when(bookingService.findAllByBookerId(anyLong(), any(), any(), any()))
                .thenReturn(listReturnBookingDtos);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state.toString())
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(listReturnBookingDtos.size()));

        verify(bookingService, times(1)).findAllByBookerId(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void findAllByBookerId_whenNormallyInvokedWithoutStateParam_thenReturnOk() {
        when(bookingService.findAllByBookerId(anyLong(), any(), any(), any()))
                .thenReturn(listReturnBookingDtos);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(listReturnBookingDtos.size()));

        verify(bookingService, times(1)).findAllByBookerId(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void findAllByBookerId_whenNormallyInvokedWithoutFromParam_thenReturnOk() {
        when(bookingService.findAllByBookerId(anyLong(), any(), any(), any()))
                .thenReturn(listReturnBookingDtos);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state.toString())
                        .param("size", size.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(listReturnBookingDtos.size()));

        verify(bookingService, times(1)).findAllByBookerId(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void findAllByBookerId_whenNormallyInvokedWithoutSizeParam_thenReturnOk() {
        when(bookingService.findAllByBookerId(anyLong(), any(), any(), any()))
                .thenReturn(listReturnBookingDtos);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state.toString())
                        .param("from", from.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(listReturnBookingDtos.size()));

        verify(bookingService, times(1)).findAllByBookerId(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void findAllByBookerId_whenNormallyInvokedWithoutAllParams_thenReturnOk() {
        when(bookingService.findAllByBookerId(anyLong(), any(), any(), any()))
                .thenReturn(listReturnBookingDtos);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(listReturnBookingDtos.size()));

        verify(bookingService, times(1)).findAllByBookerId(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void findAllByBookerId_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(get("/bookings"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).findAllByBookerId(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void findAllByOwner_whenNormallyInvoked_thenReturnOk() {
        when(bookingService.findAllByOwnerId(anyLong(), any(), any(), any()))
                .thenReturn(listReturnBookingDtos);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state.toString())
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(listReturnBookingDtos.size()));

        verify(bookingService, times(1)).findAllByOwnerId(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void findAllByOwner_whenNormallyInvokedWithoutStateParam_thenReturnOk() {
        when(bookingService.findAllByOwnerId(anyLong(), any(), any(), any()))
                .thenReturn(listReturnBookingDtos);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(listReturnBookingDtos.size()));

        verify(bookingService, times(1)).findAllByOwnerId(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void findAllByOwner_whenNormallyInvokedWithoutFromParam_thenReturnOk() {
        when(bookingService.findAllByOwnerId(anyLong(), any(), any(), any()))
                .thenReturn(listReturnBookingDtos);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state.toString())
                        .param("size", size.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(listReturnBookingDtos.size()));

        verify(bookingService, times(1)).findAllByOwnerId(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void findAllByOwner_whenNormallyInvokedWithoutSizeParam_thenReturnOk() {
        when(bookingService.findAllByOwnerId(anyLong(), any(), any(), any()))
                .thenReturn(listReturnBookingDtos);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state.toString())
                        .param("from", from.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(listReturnBookingDtos.size()));

        verify(bookingService, times(1)).findAllByOwnerId(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void findAllByOwner_whenNormallyInvokedWithoutAllParams_thenReturnOk() {
        when(bookingService.findAllByOwnerId(anyLong(), any(), any(), any()))
                .thenReturn(listReturnBookingDtos);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(listReturnBookingDtos.size()));

        verify(bookingService, times(1)).findAllByOwnerId(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void findAllByOwner_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(get("/bookings/owner"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).findAllByOwnerId(anyLong(), any(), any(), any());
    }
}