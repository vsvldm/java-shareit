package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {
    Item add(Item item);

    Item update(Item item);

    Optional<Item> getById(long itemId);

    Collection<Item> getItemsByOwner(long userId);

    Collection<Item> search(String searchText);
}