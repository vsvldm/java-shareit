package ru.practicum.shareit.item.controller.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingClient;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.ReceivingBookingDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingValidationTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    BookingClient bookingClient;
    @Autowired
    private MockMvc mvc;
    private final long userId = 1L;
    private final long bookingId = 1L;
    private final ReceivingBookingDto bookingDto = ReceivingBookingDto.builder()
            .id(1L)
            .itemId(1L)
            .start(LocalDateTime.now().plusMinutes(5))
            .end(LocalDateTime.now().plusDays(4L))
            .build();


    @SneakyThrows
    @Test
    void create_whenStartIsNull_thenReturnBadRequest() {
        ReceivingBookingDto bookingDtoWithoutStart = ReceivingBookingDto.builder()
                .id(1L)
                .itemId(1L)
                .end(LocalDateTime.now().plusDays(4L))
                .build();

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoWithoutStart))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).create(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void create_whenStartIsPast_thenReturnBadRequest() {
        ReceivingBookingDto bookingDtoStartIsPast = ReceivingBookingDto.builder()
                .id(1L)
                .itemId(1L)
                .start(LocalDateTime.of(2023, 7, 4, 1,1))
                .end(LocalDateTime.now().plusDays(4L))
                .build();

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoStartIsPast))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).create(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void create_whenEndIsNull_thenReturnBadRequest() {
        ReceivingBookingDto bookingDtoWithoutEnd = ReceivingBookingDto.builder()
                .id(1L)
                .itemId(1L)
                .start(LocalDateTime.of(2024, 7, 4, 1,1))
                .build();

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoWithoutEnd))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).create(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void create_whenEndIsPresent_thenReturnBadRequest() {
        ReceivingBookingDto bookingDtoEndIsPresent = ReceivingBookingDto.builder()
                .id(1L)
                .itemId(1L)
                .start(LocalDateTime.of(2023, 7, 4, 1,1))
                .end(LocalDateTime.now())
                .build();;

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoEndIsPresent))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).create(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void create_whenEndIsPast_thenReturnBadRequest() {
        ReceivingBookingDto bookingDtoEndIsPast = ReceivingBookingDto.builder()
                .id(1L)
                .itemId(1L)
                .start(LocalDateTime.of(2023, 7, 4, 1,1))
                .end(LocalDateTime.now().minusDays(1))
                .build();

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoEndIsPast))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).create(anyLong(), any());
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

        verify(bookingClient, never()).create(anyLong(), any());
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

        verify(bookingClient, never()).statusUpdate(anyLong(), anyLong(), anyBoolean());
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

        verify(bookingClient, never()).statusUpdate(anyLong(), anyLong(), anyBoolean());
    }

    @SneakyThrows
    @Test
    void findById_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(get("/bookings/{bookingId}", bookingId))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(bookingClient, never()).findById(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void findAllByBookerId_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(get("/bookings"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).findAllByBookerId(anyLong(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void findAllByOwner_whenWithoutXSharerUserId_thenReturnBadRequest() {
        mvc.perform(get("/bookings/owner"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).findAllByOwnerId(anyLong(), any(), any(), any());
    }
}