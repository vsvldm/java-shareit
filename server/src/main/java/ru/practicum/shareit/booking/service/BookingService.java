package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.ReceivingBookingDto;
import ru.practicum.shareit.booking.dto.ReturnBookingDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    ReturnBookingDto create(Long userId, ReceivingBookingDto bookingDto);

    ReturnBookingDto statusUpdate(Long userId, Long bookingId, boolean approved);

    ReturnBookingDto findById(Long userId, Long bookingId);

    List<ReturnBookingDto> findAllByBookerId(Long bookerId, BookingState state, Integer from, Integer size);

    List<ReturnBookingDto> findAllByOwnerId(Long ownerId, BookingState state, Integer from, Integer size);
}
