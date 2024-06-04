package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.exception.BadRequestException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final CommentMapper commentMapper;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public ItemDto create(long userId, ItemDto itemDto) {
        log.info("ItemService: Beginning of method execution create().");
        log.info("create(): Checking the existence of a user with id = {} creating the item.", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));

        log.info("create(): Checking the existence of a item request.");
        ItemRequest itemRequest = (itemDto.getRequestId() != null) ? itemRequestRepository.findById(itemDto.getRequestId())
                .orElseThrow(() -> new NotFoundException("Request not found.")) : null;

        log.info("crate(): Add the item to the database.");
        Item item = itemRepository.save(itemMapper.fromItemDto(user, itemDto, itemRequest));

        log.info("crate(): Item with id = {} successfully added to database.", item.getId());

        itemDto.setId(item.getId());
        if (itemRequest != null) {
            log.info("create(): Add requestId param.");
            itemDto.setRequestId(itemRequest.getId());
        }
        return itemDto;
    }

    @Override
    @Transactional
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        log.info("ItemService: Beginning of method execution update().");
        log.info("update(): Checking the existence of an item with id = {}.", itemId);
        Item existingItem = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException(String.format("Item with id = %d not found.", itemId))
        );

        log.info("update(): Checking the existence of an item with id = {} for a user with id = {}.", existingItem.getId(), userId);
        if (existingItem.getOwner().getId() == userId) {
            log.info("update(): Searching and updating information in the database.");
            if (itemDto.getName() != null) {
                existingItem.setName(itemDto.getName());
            }
            if (itemDto.getDescription() != null) {
                existingItem.setDescription(itemDto.getDescription());
            }
            if (itemDto.getAvailable() != null) {
                existingItem.setAvailable(itemDto.getAvailable());
            }
            Item updatedItem = itemRepository.save(existingItem);

            log.info("update(): Item with id = {} successfully updated in database.", updatedItem.getId());
            return itemMapper.toItemDto(updatedItem);
        } else {
            throw new NotFoundException(
                    String.format("User with id = %d did not create an item with id = %d not found.", userId, itemId)
            );
        }
    }

    @Override
    public ReturnItemDto findById(long userId, long itemId) {
        log.info("ItemService: Beginning of method execution findById().");

        log.info("findById(): Checking the existence of a user with id = {}.", userId);
        User owner = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found."));

        log.info("findById(): Searching item with id = {}.", itemId);
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException(String.format("Item with id = %d not found.", itemId)));

        log.info("findById(): Searching comments for item with id = {}.", itemId);
        List<CommentDto> comments = commentRepository.findByItem(item).stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());

        log.info("findById(): Checking whether the user with id = {} is the owner of the item with id = {}.", userId, itemId);
        if (!item.getOwner().equals(owner)) {
            log.info("findById(): The user is not the owner of the item with id = {}.", itemId);
            return itemMapper.toReturnItemDto(item, null, null, comments);
        }

        log.info("findById(): Searching last booking and next booking for item with id = {}.", itemId);
        LocalDateTime now = LocalDateTime.now();

        BookingDtoForItem lastBooking = getLastBooking(item, now);
        BookingDtoForItem nextBooking = getNextBooking(item, now);

        log.info("findById(): Search for item with id ={} successful completed.", itemId);
        return itemMapper.toReturnItemDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ReturnItemDto> findByOwner(long userId, Integer from, Integer size) {
        log.info("ItemService: Beginning of method execution findByOwner()");
        log.info("findByOwner(): Checking the existence of a user with id = {}.", userId);
        User owner = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found."));

        log.info("findByOwner(): Searching items by owner.");
        List<Item> itemsByOwner = new ArrayList<>();

        if(isPaginationEnabled(from, size)) {
            itemsByOwner.addAll(itemRepository.findAllByOwner(owner, PageRequest.of(from/size, size)).getContent());
        } else {
            itemsByOwner.addAll(itemRepository.findAllByOwner(owner));
        }

        log.info("findByOwner(): Searching last booking and next booking for items.");
        List<ReturnItemDto> itemsWithBookingDto = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Item item : itemsByOwner) {
            BookingDtoForItem lastBooking = getLastBooking(item, now);
            BookingDtoForItem nextBooking = getNextBooking(item, now);

            log.info("findByOwner(): Searching comments for item with id = {}.", item.getId());
            List<CommentDto> comments = commentRepository.findByItem(item).stream()
                    .map(commentMapper::toCommentDto)
                    .collect(Collectors.toList());

            itemsWithBookingDto.add(itemMapper.toReturnItemDto(item, lastBooking, nextBooking, comments));
            log.info("findByOwner(): Item with id ={} successful add in list.", item.getId());
        }

        log.info("findByOwner(): Search for items by owner successful completed.");
        return itemsWithBookingDto;
    }

    @Override
    public List<ItemDto> search(long userId, String text, Integer from, Integer size) {
        log.info("ItemService: Beginning of method execution search()");
        log.info("search(): Checking the text parameter for emptiness.");
        if (text.isEmpty()) {
            log.info("search(): Parameter is empty.");
            return List.of();
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<ItemDto> searchedItems = new ArrayList<>();

        log.info("search(): Searching items by text parameter.");
        if(isPaginationEnabled(from, size)) {
            searchedItems.addAll(itemRepository.findAllByNameOrDescription(text, PageRequest.of(from/size, size)).stream()
                    .map(itemMapper::toItemDto)
                    .collect(Collectors.toList()));
        } else {
            searchedItems.addAll(itemRepository.findAllByNameOrDescription(text).stream()
                    .map(itemMapper::toItemDto)
                    .collect(Collectors.toList()));
        }

        log.info("search(): Search for items by text parameter completed successful.");
        return searchedItems;
    }

    @Override
    @Transactional
    public CommentDto createComment(long userId, long itemId, CommentDto commentDto) {
        log.info("ItemService: Beginning of method execution createComment()");
        log.info("createComment(): Checking the existence of a user with id = {}.", userId);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));
        log.info("createComment(): Checking the existence of an item with id = {}.", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found."));

        log.info("createComment(): Checking the author with id = {} for the ability to create a comment.", userId);
        bookingRepository.findTop1ByItemAndBookerAndEndBefore(item, author, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("The author has not booking the item or the booking has not yet ended."));

        log.info("createComment(): Creating a new comment for the item.");
        commentDto.setCreated(LocalDateTime.now());
        Comment newComment = commentRepository.save(commentMapper.toComment(commentDto, author, item));
        log.info("createComment(): The comment was successfully created for the item.");
        return commentMapper.toCommentDto(newComment);
    }

    private BookingDtoForItem getLastBooking(Item item, LocalDateTime now) {
        Booking sortedEndBooking = bookingRepository.findTop1ByItemAndStartBeforeAndStatusOrderByEndDesc(item, now, BookingStatus.APPROVED).orElse(null);
        return (sortedEndBooking != null) ? bookingMapper.toBookingDtoForItem(sortedEndBooking) : null;
    }

    private BookingDtoForItem getNextBooking(Item item, LocalDateTime now) {
        Booking sortedStartBooking = bookingRepository.findTop1ByItemAndStartAfterAndStatusOrderByStartAsc(item, now, BookingStatus.APPROVED).orElse(null);
        return (sortedStartBooking != null) ? bookingMapper.toBookingDtoForItem(sortedStartBooking) : null;
    }

    private Boolean isPaginationEnabled(Integer from, Integer size) {
        if (from != null && size != null) {
            if (from < 0 || size < 1) {
                log.error("findAllByOwnerId(): Invalid request parameters.");
                throw new BadRequestException("Invalid request parameters.");
            }
            return true;
        }
        return false;
    }
}