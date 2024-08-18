package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> findByUser(long userId);

    List<ItemRequestDto> findAll(long userId, Integer from, Integer size);

    ItemRequestDto findById(long userId, long requestId);
}
