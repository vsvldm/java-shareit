package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ReturnItemDto;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemMapperTest {

    private ItemMapper itemMapper;
    private Item item;
    private ItemDto itemDto;
    private User user;
    private ItemRequest itemRequest;
    private BookingDtoForItem lastBooking;
    private BookingDtoForItem nextBooking;
    private List<CommentDto> comments;

    @BeforeEach
    public void setUp() {
        itemMapper = new ItemMapper();
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
        item = Item.builder()
                .id(1L)
                .name("Item Name")
                .description("Item Description")
                .available(true)
                .owner(user)
                .request(itemRequest)
                .build();
        itemDto = ItemDto.builder()
                .id(1L)
                .name("Item Name")
                .description("Item Description")
                .available(true)
                .build();
        lastBooking = BookingDtoForItem.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now())
                .itemId(1L)
                .bookerId(1L)
                .status(BookingStatus.APPROVED)
                .build();
        nextBooking = BookingDtoForItem.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .bookerId(1L)
                .status(BookingStatus.APPROVED)
                .build();
        comments = Collections.singletonList(CommentDto.builder()
                .id(1L)
                .text("This is a comment")
                .authorName("John Doe")
                .created(LocalDateTime.now())
                .build());
    }

    @Test
    public void testToItemDto() {
        ItemDto mappedItemDto = itemMapper.toItemDto(item);

        assertEquals(item.getId(), mappedItemDto.getId());
        assertEquals(item.getName(), mappedItemDto.getName());
        assertEquals(item.getDescription(), mappedItemDto.getDescription());
        assertEquals(item.isAvailable(), mappedItemDto.getAvailable());
    }

    @Test
    public void testToReturnItemDto() {
        ReturnItemDto returnItemDto = itemMapper.toReturnItemDto(item, lastBooking, nextBooking, comments);

        assertEquals(item.getId(), returnItemDto.getId());
        assertEquals(item.getName(), returnItemDto.getName());
        assertEquals(item.getDescription(), returnItemDto.getDescription());
        assertEquals(item.isAvailable(), returnItemDto.isAvailable());
        assertEquals(item.getOwner(), returnItemDto.getOwner());
        assertEquals(lastBooking, returnItemDto.getLastBooking());
        assertEquals(nextBooking, returnItemDto.getNextBooking());
        assertEquals(comments, returnItemDto.getComments());
    }

    @Test
    public void testFromItemDto() {
        Item mappedItem = itemMapper.fromItemDto(user, itemDto, itemRequest);

        assertEquals(itemDto.getId(), mappedItem.getId());
        assertEquals(itemDto.getName(), mappedItem.getName());
        assertEquals(itemDto.getDescription(), mappedItem.getDescription());
        assertEquals(itemDto.getAvailable(), mappedItem.isAvailable());
        assertEquals(user, mappedItem.getOwner());
        assertEquals(itemRequest, mappedItem.getRequest());
    }

    @Test
    public void testToResponseItemDto() {
        ResponseItemDto responseItemDto = itemMapper.toResponseItemDto(item);

        assertEquals(item.getId(), responseItemDto.getId());
        assertEquals(item.getName(), responseItemDto.getName());
        assertEquals(item.getDescription(), responseItemDto.getDescription());
        assertEquals(item.getRequest().getId(), responseItemDto.getRequestId());
        assertEquals(item.isAvailable(), responseItemDto.getAvailable());
    }
}
