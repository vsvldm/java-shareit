package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;

import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ReturnItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Component
public class ItemMapper {
    public ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .build();
    }

    public ReturnItemDto toReturnItemDto(Item item, BookingDtoForItem last, BookingDtoForItem next, List<CommentDto> comments) {
        return ReturnItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .owner(item.getOwner())
                .lastBooking(last)
                .nextBooking(next)
                .comments(comments)
                .build();
    }

    public Item fromItemDto(User owner, ItemDto itemDto, ItemRequest itemRequest) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .request(itemRequest)
                .build();
    }

    public ResponseItemDto toResponseItemDto(Item item) {
        return ResponseItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .requestId(item.getRequest().getId())
                .available(item.isAvailable())
                .build();
    }
}