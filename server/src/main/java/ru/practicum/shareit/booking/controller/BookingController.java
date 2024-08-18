package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.ReceivingBookingDto;
import ru.practicum.shareit.booking.dto.ReturnBookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ReturnBookingDto create(@RequestHeader("X-Sharer-User-Id") long userId,
                                   @RequestBody ReceivingBookingDto bookingDto) {
        return bookingService.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ReturnBookingDto statusUpdate(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable long bookingId,
                                         @RequestParam boolean approved) {
        return bookingService.statusUpdate(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ReturnBookingDto findById(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @PathVariable long bookingId) {
        return bookingService.findById(userId, bookingId);
    }

    @GetMapping
    public List<ReturnBookingDto> findAllByBookerId(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                                    @RequestParam(required = false, defaultValue = "ALL") BookingState state,
                                                    @RequestParam(required = false) Integer from,
                                                    @RequestParam(required = false) Integer size) {
        return bookingService.findAllByBookerId(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public List<ReturnBookingDto> findAllByOwner(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                 @RequestParam(required = false, defaultValue = "ALL") BookingState state,
                                                 @RequestParam(required = false) Integer from,
                                                 @RequestParam(required = false) Integer size) {
        return bookingService.findAllByOwnerId(ownerId, state, from, size);
    }
}