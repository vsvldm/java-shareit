package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ReturnItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(long userId, ItemDto itemDto);

    ItemDto update(long userId, long itemId, ItemDto itemDto);

    ReturnItemDto findById(long userId, long itemId);

    List<ReturnItemDto> findByOwner(long userId);

    List<ItemDto> search(long userId, String text);

    CommentDto createComment(long userId, long itemId, CommentDto commentDto);
}