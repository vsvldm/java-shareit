package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.exception.BadRequestException;
import ru.practicum.shareit.exception.exception.ForbiddenException;
import ru.practicum.shareit.exception.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ReturnItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private BookingMapper bookingMapper;
    @InjectMocks
    private ItemServiceImpl itemService;
    private final long userId = 1L;
    private final long itemId = 1L;
    private final User user = User.builder()
            .id(userId)
            .name("UserName")
            .email("user@email.com")
            .build();
    private final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("ItemName")
            .description("ItemDescription")
            .available(true)
            .requestId(1L)
            .build();
    private final ItemRequest request = ItemRequest.builder()
            .id(1L)
            .description("RequestDescription")
            .requestor(user)
            .created(LocalDateTime.now())
            .build();
    private final Item item = Item.builder()
            .id(1L)
            .name("ItemName")
            .description("ItemDescription")
            .available(true)
            .owner(user)
            .request(request)
            .build();
    private final Comment comment = Comment.builder()
            .id(1L)
            .text("CommentText")
            .item(item)
            .author(user)
            .created(LocalDateTime.now())
            .build();
    private final CommentDto commentDto = CommentDto.builder()
            .id(1L)
            .text("CommentText")
            .authorName(comment.getAuthor().getName())
            .created(LocalDateTime.now())
            .build();
    private final List<CommentDto> commentsDto = Collections.singletonList(commentDto);
    private final ReturnItemDto returnItemDto = ReturnItemDto.builder()
            .id(item.getId())
            .name(item.getName())
            .description(item.getDescription())
            .available(item.isAvailable())
            .owner(item.getOwner())
            .lastBooking(null)
            .nextBooking(null)
            .comments(commentsDto)
            .build();
    private final BookingDtoForItem lastBooking = BookingDtoForItem.builder()
            .id(1L)
            .start(LocalDateTime.now().minusDays(7))
            .end(LocalDateTime.now().minusDays(1))
            .itemId(item.getId())
            .bookerId(user.getId())
            .status(BookingStatus.APPROVED)
            .build();
    private final BookingDtoForItem nextBooking = BookingDtoForItem.builder()
            .id(2L)
            .start(LocalDateTime.now().plusDays(1))
            .end(LocalDateTime.now().plusDays(7))
            .itemId(item.getId())
            .bookerId(user.getId())
            .status(BookingStatus.APPROVED)
            .build();
    private final ReturnItemDto returnItemDtoWithLastAndNext = ReturnItemDto.builder()
            .id(item.getId())
            .name(item.getName())
            .description(item.getDescription())
            .available(item.isAvailable())
            .owner(item.getOwner())
            .lastBooking(lastBooking)
            .nextBooking(nextBooking)
            .comments(commentsDto)
            .build();
    private final List<Comment> comments = Collections.singletonList(comment);
    private final List<Item> items = Collections.singletonList(item);


    @Test
    void create_whenNormallyInvoked_thenReturnItemDto() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(requestRepository.findById(anyLong())).thenReturn(Optional.of(request));
        when(itemMapper.fromItemDto(user, itemDto, request)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);

        ItemDto actual = itemService.create(userId, itemDto);

        assertEquals(itemDto, actual);

        verify(userRepository, times(1)).findById(anyLong());
        verify(requestRepository, times(1)).findById(anyLong());
        verify(itemMapper, times(1)).fromItemDto(any(), any(), any());
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void create_whenInvokedWithUnknownUser_thenReturnNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.create(userId, itemDto));
        verify(userRepository, times(1)).findById(userId);
        verify(requestRepository, never()).findById(anyLong());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void create_whenInvokedWithUnknownItemRequest_thenReturnNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(requestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.create(userId, itemDto));
        verify(userRepository, times(1)).findById(userId);
        verify(requestRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).save(item);
    }

    @Test
    void create_whenInvokedWithoutItemRequest_thenReturnItemDto() {
        ItemDto itemDtoForTest = ItemDto.builder()
                .id(1L)
                .name("ItemDtoName")
                .description("ItemDtoDescription")
                .available(true)
                .build();
        Item itemForTest = Item.builder()
                .id(1L)
                .name("ItemName")
                .description("ItemDescription")
                .available(true)
                .owner(user)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemMapper.fromItemDto(user, itemDtoForTest, null)).thenReturn(itemForTest);
        when(itemRepository.save(itemForTest)).thenReturn(itemForTest);

        ItemDto actual = itemService.create(userId, itemDtoForTest);

        assertEquals(itemDtoForTest, actual);

        verify(userRepository, times(1)).findById(anyLong());
        verify(requestRepository, never()).findById(anyLong());
        verify(itemMapper, times(1)).fromItemDto(any(), any(), any());
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void update_whenNormallyUpdateAllFields_thenReturnItemDto() {
        ItemDto itemDtoForUpdate = ItemDto.builder()
                .name("UpdateName")
                .description("UpdateDescription")
                .available(false)
                .build();
        ItemDto expected = ItemDto.builder()
                .name(itemDtoForUpdate.getName())
                .description(itemDtoForUpdate.getDescription())
                .available(itemDtoForUpdate.getAvailable())
                .requestId(itemDto.getRequestId())
                .build();
        Item itemForTest = Item.builder()
                .id(itemDto.getId())
                .name(expected.getName())
                .description(expected.getDescription())
                .available(expected.getAvailable())
                .owner(user)
                .request(request)
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(itemForTest);
        when(itemMapper.toItemDto(itemForTest)).thenReturn(expected);

        ItemDto actual = itemService.update(userId, itemId, itemDtoForUpdate);

        assertEquals(expected, actual);

        verify(itemRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).save(any());
        verify(itemMapper, times(1)).toItemDto(any());
    }

    @Test
    void update_whenNormallyUpdateOnlyName_thenReturnItemDto() {
        ItemDto itemDtoForUpdate = ItemDto.builder()
                .name("UpdateName")
                .build();
        ItemDto expected = ItemDto.builder()
                .name(itemDtoForUpdate.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .requestId(itemDto.getRequestId())
                .build();
        Item itemForTest = Item.builder()
                .id(itemDto.getId())
                .name(expected.getName())
                .description(expected.getDescription())
                .available(expected.getAvailable())
                .owner(user)
                .request(request)
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(itemForTest);
        when(itemMapper.toItemDto(itemForTest)).thenReturn(expected);

        ItemDto actual = itemService.update(userId, itemId, itemDtoForUpdate);

        assertEquals(expected, actual);

        verify(itemRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).save(any());
        verify(itemMapper, times(1)).toItemDto(any());
    }

    @Test
    void update_whenNormallyUpdateOnlyDescription_thenReturnItemDto() {
        ItemDto itemDtoForUpdate = ItemDto.builder()
                .description("UpdateDescription")
                .build();
        ItemDto expected = ItemDto.builder()
                .name(itemDto.getName())
                .description(itemDtoForUpdate.getDescription())
                .available(itemDto.getAvailable())
                .requestId(itemDto.getRequestId())
                .build();
        Item itemForTest = Item.builder()
                .id(itemDto.getId())
                .name(expected.getName())
                .description(expected.getDescription())
                .available(expected.getAvailable())
                .owner(user)
                .request(request)
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(itemForTest);
        when(itemMapper.toItemDto(itemForTest)).thenReturn(expected);

        ItemDto actual = itemService.update(userId, itemId, itemDtoForUpdate);

        assertEquals(expected, actual);

        verify(itemRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).save(any());
        verify(itemMapper, times(1)).toItemDto(any());
    }

    @Test
    void update_whenNormallyUpdateOnlyAvailable_thenReturnItemDto() {
        ItemDto itemDtoForUpdate = ItemDto.builder()
                .available(false)
                .build();
        ItemDto expected = ItemDto.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDtoForUpdate.getAvailable())
                .requestId(itemDto.getRequestId())
                .build();
        Item itemForTest = Item.builder()
                .id(itemDto.getId())
                .name(expected.getName())
                .description(expected.getDescription())
                .available(expected.getAvailable())
                .owner(user)
                .request(request)
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(itemForTest);
        when(itemMapper.toItemDto(itemForTest)).thenReturn(expected);

        ItemDto actual = itemService.update(userId, itemId, itemDtoForUpdate);

        assertEquals(expected, actual);

        verify(itemRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).save(any());
        verify(itemMapper, times(1)).toItemDto(any());
    }

    @Test
    void update_whenUpdateUnknownItem_thenReturnNotFoundException() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.update(userId, itemId, itemDto));

        verify(itemRepository, never()).save(any());
    }

    @Test
    void update_whenUpdateIsNotAvailableForUser_thenReturnForbiddenException() {
        long userIdForTest = 2L;

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class, () -> itemService.update(userIdForTest, itemId, itemDto));

        verify(itemRepository, never()).save(any());
    }

    @Test
    void findById_whenInvokedWithUserIsOwner_thenReturnItemDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findByItem(item)).thenReturn(comments);
        when(commentMapper.toCommentDto(comment)).thenReturn(commentDto);
        when(bookingRepository.findTop1ByItemAndStartBeforeAndStatusOrderByEndDesc(any(), any(), any()))
                .thenReturn(Optional.of(Booking.builder()
                        .id(1L)
                        .start(lastBooking.getStart())
                        .end(lastBooking.getEnd())
                        .item(item)
                        .booker(user)
                        .status(BookingStatus.APPROVED)
                        .build()));
        when(bookingRepository.findTop1ByItemAndStartAfterAndStatusOrderByStartAsc(any(), any(), any()))
                .thenReturn(Optional.of(Booking.builder()
                        .id(2L)
                        .start(nextBooking.getStart())
                        .end(nextBooking.getEnd())
                        .item(item)
                        .booker(user)
                        .status(BookingStatus.APPROVED)
                        .build()));
        when(bookingMapper.toBookingDtoForItem(any())).thenReturn(lastBooking, nextBooking);
        when(itemMapper.toReturnItemDto(item, lastBooking, nextBooking, commentsDto)).thenReturn(returnItemDtoWithLastAndNext);

        ReturnItemDto actual = itemService.findById(userId, itemId);

        assertEquals(returnItemDtoWithLastAndNext, actual);

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(commentRepository, times(1)).findByItem(any());
        verify(commentMapper, times(1)).toCommentDto(any());
        verify(bookingRepository, times(1)).findTop1ByItemAndStartAfterAndStatusOrderByStartAsc(any(), any(), any());
        verify(bookingRepository, times(1)).findTop1ByItemAndStartBeforeAndStatusOrderByEndDesc(any(), any(), any());
        verify(bookingMapper, times(2)).toBookingDtoForItem(any());
        verify(itemMapper, times(1)).toReturnItemDto(any(), any(), any(), anyList());
    }

    @Test
    void findById_whenInvokedWithUserNotIsOwner_thenReturnItemDto() {
        List<Comment> comments = Collections.singletonList(comment);
        User notOwner = User.builder()
                .id(2L)
                .name("NotOwner")
                .email("NotOwner@Email.com")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(notOwner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findByItem(item)).thenReturn(comments);
        when(commentMapper.toCommentDto(comment)).thenReturn(commentDto);
        when(itemMapper.toReturnItemDto(any(), any(), any(), anyList())).thenReturn(returnItemDto);

        ReturnItemDto actual = itemService.findById(userId, itemId);

        assertEquals(returnItemDto, actual);

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(commentRepository, times(1)).findByItem(any());
        verify(commentMapper, times(1)).toCommentDto(any());
        verify(bookingRepository, never()).findTop1ByItemAndStartAfterAndStatusOrderByStartAsc(any(), any(), any());
        verify(bookingRepository, never()).findTop1ByItemAndStartBeforeAndStatusOrderByEndDesc(any(), any(), any());
        verify(bookingMapper, never()).toBookingDtoForItem(any());
        verify(itemMapper, times(1)).toReturnItemDto(any(), any(), any(), anyList());
    }

    @Test
    void findById_whenInvokedWithUnknownUser_thenReturnNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.findById(userId, itemId));

        verify(itemRepository, never()).findById(anyLong());
    }

    @Test
    void findById_whenInvokedWithUnknownItem_thenReturnNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.findById(userId, itemId));

        verify(commentRepository, never()).findByItem(any());
    }

    @Test
    void findByOwner_whenNormallyInvokedWithPaginationAndWithLastAndNextBooking_thenReturnListReturnItemDto() {
        List<ReturnItemDto> expected = Collections.singletonList(returnItemDtoWithLastAndNext);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(any(), any())).thenReturn(new PageImpl<>(items));
        when(bookingRepository.findTop1ByItemAndStartBeforeAndStatusOrderByEndDesc(any(), any(), any()))
                .thenReturn(Optional.of(Booking.builder()
                        .id(1L)
                        .start(lastBooking.getStart())
                        .end(lastBooking.getEnd())
                        .item(item)
                        .booker(user)
                        .status(BookingStatus.APPROVED)
                        .build()));
        when(bookingRepository.findTop1ByItemAndStartAfterAndStatusOrderByStartAsc(any(), any(), any()))
                .thenReturn(Optional.of(Booking.builder()
                        .id(2L)
                        .start(nextBooking.getStart())
                        .end(nextBooking.getEnd())
                        .item(item)
                        .booker(user)
                        .status(BookingStatus.APPROVED)
                        .build()));
        when(bookingMapper.toBookingDtoForItem(any())).thenReturn(lastBooking, nextBooking);
        when(commentRepository.findByItem(item)).thenReturn(comments);
        when(commentMapper.toCommentDto(comment)).thenReturn(commentDto);
        when(itemMapper.toReturnItemDto(item, lastBooking, nextBooking, commentsDto)).thenReturn(returnItemDtoWithLastAndNext);

        List<ReturnItemDto> actual = itemService.findByOwner(userId, 0, 10);

        assertEquals(expected, actual);

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findAllByOwner(any(),any());
        verify(itemRepository, never()).findAllByOwner(any());
        verify(bookingRepository, times(1)).findTop1ByItemAndStartAfterAndStatusOrderByStartAsc(any(), any(), any());
        verify(bookingRepository, times(1)).findTop1ByItemAndStartBeforeAndStatusOrderByEndDesc(any(), any(), any());
        verify(bookingMapper, times(2)).toBookingDtoForItem(any());
        verify(commentRepository, times(1)).findByItem(any());
        verify(commentMapper, times(1)).toCommentDto(any());
        verify(itemMapper, times(1)).toReturnItemDto(any(), any(), any(), anyList());
    }

    @Test
    void findByOwner_whenNormallyInvokedWithoutPaginationAndWithoutLastAndNextBooking_thenReturnListReturnItemDto() {
        List<ReturnItemDto> expected = Collections.singletonList(returnItemDto);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(any())).thenReturn(items);
        when(bookingRepository.findTop1ByItemAndStartBeforeAndStatusOrderByEndDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(bookingRepository.findTop1ByItemAndStartAfterAndStatusOrderByStartAsc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(commentRepository.findByItem(item)).thenReturn(comments);
        when(commentMapper.toCommentDto(comment)).thenReturn(commentDto);
        when(itemMapper.toReturnItemDto(item, null, null, commentsDto)).thenReturn(returnItemDto);

        List<ReturnItemDto> actual = itemService.findByOwner(userId, null, null);

        assertEquals(expected, actual);

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findAllByOwner(any());
        verify(itemRepository, never()).findAllByOwner(any(),any());
        verify(bookingRepository, times(1)).findTop1ByItemAndStartAfterAndStatusOrderByStartAsc(any(), any(), any());
        verify(bookingRepository, times(1)).findTop1ByItemAndStartBeforeAndStatusOrderByEndDesc(any(), any(), any());
        verify(bookingMapper, never()).toBookingDtoForItem(any());
        verify(commentRepository, times(1)).findByItem(any());
        verify(commentMapper, times(1)).toCommentDto(any());
        verify(itemMapper, times(1)).toReturnItemDto(any(), any(), any(), anyList());
    }

    @Test
    void findByOwner_whenInvokedWithUnknownUser_thenReturnNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.findById(userId, itemId));

        verify(itemRepository, never()).findAllByOwner(any());
    }

    @Test
    void search_whenInvokedWithPagination_thenReturnListItemDto() {
        List<Item> items = Collections.singletonList(item);
        List<ItemDto> expected = Collections.singletonList(itemDto);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByNameOrDescription(anyString(), any())).thenReturn(new PageImpl<>(items));
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        List<ItemDto> actual = itemService.search(userId, "text", 0, 10);

        assertEquals(expected, actual);

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findAllByNameOrDescription(anyString(), any());
        verify(itemRepository, never()).findAllByNameOrDescription(anyString());
        verify(itemMapper, times(1)).toItemDto(any());
    }

    @Test
    void search_whenInvokedWithoutPagination_thenReturnListItemDto() {
        List<Item> items = Collections.singletonList(item);
        List<ItemDto> expected = Collections.singletonList(itemDto);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByNameOrDescription(anyString())).thenReturn(items);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        List<ItemDto> actual = itemService.search(userId, "text", null, null);

        assertEquals(expected, actual);

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).findAllByNameOrDescription(anyString(), any());
        verify(itemRepository, times(1)).findAllByNameOrDescription(anyString());
        verify(itemMapper, times(1)).toItemDto(any());
    }

    @Test
    void search_whenInvokedWithEmptyText_thenReturnEmptyList() {
        List<ItemDto> expected = Collections.emptyList();

        List<ItemDto> actual = itemService.search(userId, "", null, null);

        assertEquals(expected, actual);

        verify(userRepository, never()).findById(anyLong());
        verify(itemRepository, never()).findAllByNameOrDescription(anyString());
    }

    @Test
    void search_whenInvokedWithUnknownUser_thenReturnNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.findById(userId, itemId));

        verify(itemRepository, never()).findAllByNameOrDescription(any());
        verify(itemRepository, never()).findAllByNameOrDescription(any(), any());
    }

    @Test
    void createComment_whenNormallyInvoked_thenReturnCommentDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findTop1ByItemAndBookerAndEndBefore(any(), any(), any()))
                .thenReturn(Optional.of(Booking.builder()
                        .id(1L)
                        .start(LocalDateTime.now().minusDays(7))
                        .end(LocalDateTime.now().minusDays(1))
                        .item(item)
                        .booker(user)
                        .status(BookingStatus.APPROVED)
                        .build()));
        when(commentMapper.toComment(commentDto, user, item)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toCommentDto(comment)).thenReturn(commentDto);

        CommentDto actual = itemService.createComment(userId, itemId, commentDto);

        assertEquals(commentDto, actual);

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findTop1ByItemAndBookerAndEndBefore(any(), any(), any());
        verify(commentRepository, times(1)).save(any());
    }

    @Test
    void createComment_whenInvokedWithUnknownUser_thenReturnNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createComment(userId, itemId, commentDto));

        verify(itemRepository, never()).findById(anyLong());
    }

    @Test
    void createComment_whenInvokedWithUnknownItem_thenReturnNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createComment(userId, itemId, commentDto));

        verify(bookingRepository, never()).findTop1ByItemAndBookerAndEndBefore(any(), any(), any());
    }

    @Test
    void createComment_whenInvokedFailFoundBooking_thenReturnBadRequestException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findTop1ByItemAndBookerAndEndBefore(any(), any(), any())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> itemService.createComment(userId, itemId, commentDto));

        verify(commentRepository, never()).save(any());
    }
}