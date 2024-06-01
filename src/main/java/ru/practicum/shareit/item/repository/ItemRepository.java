package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Collection<Item> findAllByOwner(User owner);

    @Query("select i " +
            "from Item i " +
            "where i.available = true " +
            "and (lower(i.name) like (lower (concat ('%',?1, '%'))) " +
            "or lower(i.description) like (lower (concat ('%',?1, '%'))))")
    Collection<Item> findAllByNameOrDescription(String text);
}