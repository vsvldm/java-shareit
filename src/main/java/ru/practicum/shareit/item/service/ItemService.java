package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Map;

public interface ItemService {
    ItemDto create(long userId, ItemDto itemDto);

    ItemDto update(long userId, long itemId, Map<String, Object> fields);

    ItemDto findById(long userId, long itemId);

    List<ItemDto> findByOwner(long userId);

    List<ItemDto> search(long userId, String text);
}