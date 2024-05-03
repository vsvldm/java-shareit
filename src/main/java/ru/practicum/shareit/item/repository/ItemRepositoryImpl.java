package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private final NamedParameterJdbcOperations jdbcOperations;

    @Override
    public Item add(Item item) {
        Map<String, Object> params = Map.of(
                "name", item.getName(),
                "description", item.getDescription(),
                "available", item.isAvailable(),
                "owner", item.getOwnerId());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource paramSource = new MapSqlParameterSource(params);
        String sql = "INSERT INTO ITEMS (ITEM_NAME, " +
                "ITEM_DESCRIPTION, " +
                "ITEM_AVAILABLE, " +
                "ITEM_OWNER_ID) " +
                "VALUES (:name, :description, :available, :owner)";

        jdbcOperations.update(sql, paramSource, keyHolder);
        item.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return item;
    }

    @Override
    public Item update(Item item) {
        Map<String, Object> params = Map.of(
                "id", item.getId(),
                "name", item.getName(),
                "description", item.getDescription(),
                "available", item.isAvailable(),
                "owner", item.getOwnerId()
        );
        String sql = "UPDATE ITEMS SET ITEM_NAME = :name, " +
                "ITEM_DESCRIPTION = :description, " +
                "ITEM_AVAILABLE = :available, " +
                "ITEM_OWNER_ID = :owner " +
                "WHERE ITEM_ID = :id";

        jdbcOperations.update(sql, params);
        return item;
    }

    @Override
    public Optional<Item> getById(long itemId) {
        Map<String, Object> params = Map.of("id", itemId);
        String sql = "SELECT * " +
                "FROM ITEMS " +
                "WHERE ITEM_ID = :id";

        try {
            return Optional.ofNullable(jdbcOperations.queryForObject(sql, params, this::makeItem));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Collection<Item> getItemsByOwner(long userId) {
        Map<String, Object> params = Map.of("owner", userId);
        String sql = "SELECT * " +
                "FROM ITEMS " +
                "WHERE ITEM_OWNER_ID = :owner";

        return jdbcOperations.query(sql, params, this::makeItem);
    }

    @Override
    public Collection<Item> search(String searchText) {
        Map<String, Object> params = Map.of("searchText", "%" + searchText.toLowerCase() + "%");
        String sql = "SELECT * " +
                "FROM ITEMS " +
                "WHERE ITEM_AVAILABLE = TRUE " +
                "AND (LOWER(ITEM_NAME) LIKE :searchText OR LOWER(ITEM_DESCRIPTION) LIKE :searchText)";

        return jdbcOperations.query(sql, params, this::makeItem);
    }

    private Item makeItem(ResultSet rs, int rowNum) throws SQLException {
        return Item.builder()
                .id(rs.getLong("ITEM_ID"))
                .name(rs.getString("ITEM_NAME"))
                .description(rs.getString("ITEM_DESCRIPTION"))
                .available(rs.getBoolean("ITEM_AVAILABLE"))
                .ownerId(rs.getLong("ITEM_OWNER_ID"))
                .build();
    }
}