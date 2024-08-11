package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemRequestMapperTest {

    private ItemRequestMapper itemRequestMapper;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;
    private User user;

    @BeforeEach
    public void setUp() {
        itemRequestMapper = new ItemRequestMapper();
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();
        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Request Description")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();
        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Request Description")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    public void testToItemRequestDto() {
        ItemRequestDto mappedItemRequestDto = itemRequestMapper.toItemRequestDto(itemRequest);

        assertEquals(itemRequest.getId(), mappedItemRequestDto.getId());
        assertEquals(itemRequest.getDescription(), mappedItemRequestDto.getDescription());
        assertEquals(itemRequest.getCreated(), mappedItemRequestDto.getCreated());
    }

    @Test
    public void testToItemRequest() {
        ItemRequest mappedItemRequest = itemRequestMapper.toItemRequest(itemRequestDto, user);

        assertEquals(itemRequestDto.getId(), mappedItemRequest.getId());
        assertEquals(itemRequestDto.getDescription(), mappedItemRequest.getDescription());
        assertEquals(user, mappedItemRequest.getRequestor());
        assertEquals(itemRequestDto.getCreated(), mappedItemRequest.getCreated());
    }
}
