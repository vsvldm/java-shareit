package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ReturnItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") long userId,
                          @PathVariable long itemId,
                          @RequestBody ItemDto itemDto) {
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ReturnItemDto findById(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @PathVariable("itemId") long itemId) {
        return itemService.findById(userId, itemId);
    }

    @GetMapping
    public List<ReturnItemDto> findAllByOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @RequestParam(required = false) Integer from,
                                              @RequestParam(required = false) Integer size) {
        return itemService.findByOwner(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestHeader("X-Sharer-User-Id") long userId,
                                @RequestParam String text,
                                @RequestParam(required = false) Integer from,
                                @RequestParam(required = false) Integer size) {
        return itemService.search(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @PathVariable long itemId,
                                    @Valid @RequestBody CommentDto commentDto) {
        return itemService.createComment(userId, itemId, commentDto);
    }

}