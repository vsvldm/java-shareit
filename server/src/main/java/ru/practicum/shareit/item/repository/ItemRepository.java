package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Collection<Item> findAllByOwner(User owner);

    Page<Item> findAllByOwner(User owner, Pageable pageable);

    @Query("select i " +
            "from Item i " +
            "where i.available = true " +
            "and (lower(i.name) like (lower (concat ('%',?1, '%'))) " +
            "or lower(i.description) like (lower (concat ('%',?1, '%'))))")
    Collection<Item> findAllByNameOrDescription(String text);

    @Query("select i " +
            "from Item i " +
            "where i.available = true " +
            "and (lower(i.name) like (lower (concat ('%',?1, '%'))) " +
            "or lower(i.description) like (lower (concat ('%',?1, '%'))))")
    Page<Item> findAllByNameOrDescription(String text, Pageable pageable);

    Collection<Item> findAllByRequest(ItemRequest request);
}