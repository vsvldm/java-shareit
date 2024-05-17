package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        log.info("ItemService: Beginning of method execution create().");
        log.info("create(): Checking the existence of a user with id = {} creating the item.", userId);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found."));

        log.info("crate(): Add the item to the database.");
        Item item = itemRepository.add(itemMapper.fromItemDto(userId, itemDto));

        log.info("crate(): Item with id = {} successfully added to database.", item.getId());
        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        log.info("ItemService: Beginning of method execution update().");
        log.info("update(): Checking the existence of an item with id = {}.", itemId);
        Item existingItem = itemRepository.getById(itemId).orElseThrow(
                () -> new NotFoundException(String.format("Item with id = %d not found.", itemId))
        );

        log.info("update(): Checking the existence of an item with id = {} for a user with id = {}.", existingItem.getId(), userId);
        if (existingItem.getOwnerId() == userId) {
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
            Item updatedItem = itemRepository.update(existingItem);

            log.info("update(): Item with id = {} successfully updated in database.", updatedItem.getId());
            return itemMapper.toItemDto(updatedItem);
        } else {
            throw new NotFoundException(
                    String.format("User with id = %d did not create an item with id = %d not found.", userId, itemId)
            );
        }
    }

    @Override
    public ItemDto findById(long userId, long itemId) {
        log.info("ItemService: Beginning of method execution findById().");

        log.info("findById(): Checking the existence of a user with id = {}.", userId);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found."));

        log.info("findById(): Searching item with id = {}.", itemId);
        Item item = itemRepository.getById(itemId).orElseThrow(
                () -> new NotFoundException(String.format("Item with id = %d not found.", itemId)));

        log.info("findById(): Search for item with id ={} successful completed.", itemId);
        return itemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> findByOwner(long userId) {
        log.info("ItemService: Beginning of method execution findByOwner()");
        log.info("findByOwner(): Checking the existence of a user with id = {}.", userId);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found."));

        log.info("findByOwner(): Searching items by owner.");
        List<ItemDto> itemsByOwner = itemRepository.getItemsByOwner(userId).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());

        log.info("findByOwner(): Search for items by owner successful completed.");
        return itemsByOwner;
    }

    @Override
    public List<ItemDto> search(long userId, String text) {
        log.info("ItemService: Beginning of method execution search()");
        log.info("search(): Checking the text parameter for emptiness.");
        if (text.isEmpty()) {
            log.info("search(): Parameter is empty.");
            return List.of();
        }

        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        log.info("search(): Searching items by text parameter.");
        List<ItemDto> searchedItems = itemRepository.search(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());

        log.info("search(): Search for items by text parameter completed successful.");
        return searchedItems;
    }
}