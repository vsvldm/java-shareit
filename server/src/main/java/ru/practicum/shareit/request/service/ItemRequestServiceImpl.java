package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.exception.BadRequestException;
import ru.practicum.shareit.exception.exception.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
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
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(long userId, ItemRequestDto itemRequestDto) {
        log.info("ItemRequestService: Beginning of method execution create().");
        log.info("create(): Checking the existence of a user with id = {}.", userId);
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("create(): User with id = {} not found", userId);
                    return new NotFoundException(String.format("User with id = %d not found", userId));
                });

        itemRequestDto.setCreated(LocalDateTime.now());
        log.info("crate(): Add the request to the database.");
        ItemRequest itemRequest = itemRequestRepository.save(itemRequestMapper.toItemRequest(itemRequestDto, requestor));

        log.info("crate(): Request with id = {} successfully added to database.", itemRequest.getId());
        return itemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestDto> findByUser(long userId) {
        log.info("ItemRequestService: Beginning of method execution findByUser().");
        log.info("findByUser(): Checking the existence of a user with id = {}.", userId);
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("findByUser(): User with id = {} not found", userId);
                    return new NotFoundException(String.format("User with id = %d not found", userId));
                });

        List<ItemRequestDto> requestDtos = new ArrayList<>();

        log.info("findByUser(): Searching all requests by user.");
        log.info("findByUser(): Searching responses for item request.");
        for (ItemRequest request : itemRequestRepository.findAllByRequestorOrderByCreatedDesc(requestor)) {
            requestDtos.add(getFullItemRequestDto(request));
        }

        log.info("findByUser(): Searching successfully completed.");
        return requestDtos;
    }

    @Override
    public List<ItemRequestDto> findAll(long userId, Integer from, Integer size) {
        log.info("ItemRequestService: Beginning of method execution findAll().");
        log.info("findAll(): Validation of request parameters.");
        if (from == null || size == null) {
            log.info("findAll(): Size or From parameters are null.");
            return List.of();
        }
        if (from < 0 || size < 1) {
            log.error("findAll(): Invalid request parameters.");
            throw new BadRequestException("Invalid request parameters.");
        }

        log.info("findAll(): Checking the existence of a user with id = {}.", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("findAll(): User with id = {} not found", userId);
                    return new NotFoundException(String.format("User with id = %d not found", userId));
                });

        List<ItemRequestDto> requestDtos = new ArrayList<>();

        log.info("findAll(): Searching all requests.");
        List<ItemRequest> requests = itemRequestRepository.findAll(PageRequest.of(from/size, size, Sort.by("created"))). stream()
                .filter(request -> user.getId() != request.getRequestor().getId())
                .collect(Collectors.toList());

        log.info("findAll(): Searching responses for item request.");
        for (ItemRequest request : requests) {
            requestDtos.add(getFullItemRequestDto(request));
        }
        log.info("findAll(): Searching successfully completed.");
        return requestDtos;
    }

    @Override
    public ItemRequestDto findById(long userId, long requestId) {
        log.info("ItemRequestService: Beginning of method execution findById().");
        log.info("findById(): Checking the existence of a user with id = {}.", userId);
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("findById(): User with id = {} not found", userId);
                    return new NotFoundException(String.format("User with id = %d not found", userId));
                });

        log.info("findById(): Searching request with id = {}.", requestId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("findById(): Request with id = {} not found", requestId);
                    return new NotFoundException(String.format("Request with id = %d not found", requestId));
                });

        log.info("findAll(): Searching response for item request.");
        ItemRequestDto itemRequestDto = getFullItemRequestDto(itemRequest);

        log.info("findById(): Searching successfully completed.");
        return itemRequestDto;
    }

    private ItemRequestDto getFullItemRequestDto(ItemRequest request) {
        ItemRequestDto itemRequestDto = itemRequestMapper.toItemRequestDto(request);

        itemRequestDto.setItems(itemRepository.findAllByRequest(request).stream()
                .map(itemMapper::toResponseItemDto)
                .collect(Collectors.toList()));

        return itemRequestDto;
    }
}
