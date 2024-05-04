package ru.practicum.shareit.user.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final NamedParameterJdbcOperations jdbcOperations;

    @Override
    public User add(User user) {
        Map<String, Object> params = Map.of(
                "name", user.getName(),
                "email", user.getEmail()
        );
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource paramSource = new MapSqlParameterSource(params);
        String sql = "INSERT INTO USERS (USER_NAME, " +
                "USER_EMAIL) " +
                "VALUES (:name, :email)";

        jdbcOperations.update(sql, paramSource, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return user;
    }

    @Override
    public User update(User user) {
        Map<String, Object> params = Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail());
        String sql = "UPDATE USERS SET USER_NAME = :name," +
                "USER_EMAIL = :email " +
                "WHERE USER_ID = :id";

            jdbcOperations.update(sql, params);
            return user;
    }

    @Override
    public void remove(long userId) {
        Map<String, Object> params = Map.of("id", userId);
        String sql = "DELETE FROM USERS " +
                "WHERE USER_ID = :id";

        jdbcOperations.update(sql, params);
    }

    @Override
    public Collection<User> getAll() {
        String sql = "SELECT * " +
                "FROM USERS";

        return jdbcOperations.query(sql, this::makeUser);
    }

    @Override
    public Optional<User> getById(long userId) {
        Map<String, Object> params = Map.of("id", userId);
        String sql = "SELECT * " +
                "FROM USERS " +
                "WHERE USER_ID = :id";

        try {
            return Optional.ofNullable(jdbcOperations.queryForObject(sql, params, this::makeUser));
        } catch (DataAccessException e) {
            log.info("UserRepository.getById(): User with id = {} not found", userId);
            return Optional.empty();
        }
    }

    private User makeUser(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("USER_ID"))
                .name(rs.getString("USER_NAME"))
                .email(rs.getString("USER_EMAIL"))
                .build();
    }
}
