package com.example.minishop.app.users;

import com.example.minishop.app.users.entities.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class UserRepository {
    private final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String save(UserEntity userEntity) {
        String query = """
                INSERT INTO users (id, name, email, image_url, role, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        Instant currentTime = Instant.now();
        try {
            jdbcTemplate.update(
                    query,
                    userEntity.getId(),
                    userEntity.getName(),
                    userEntity.getEmail(),
                    userEntity.getImageURL(),
                    userEntity.getRole().name(),
                    Timestamp.from(currentTime),
                    Timestamp.from(currentTime)
            );
        } catch (Exception e) {
            logger.error(String.format("Failed to save user with id %s", userEntity.getId()), e);
            return null;
        }

        return userEntity.getId();
    }

    public Optional<UserEntity> findById(String id) {
        String query = """
                SELECT id, name, email, image_url, role FROM users
                WHERE id = ?
                """;

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(query, UserMapper::fromResultSetToUserEntity, id));
        } catch (Exception e) {
            logger.error(String.format("Failed to find user with id %s", id), e);
            return Optional.empty();
        }
    }

    public List<UserEntity> findAll() {
        String query = """
                SELECT id, name, email, image_url, role FROM users
                """;

        try {
            return jdbcTemplate.query(query, UserMapper::fromResultSetToUserEntity);
        } catch (Exception e) {
            logger.error("Failed to load all users", e);
            return null;
        }
    }

    public String updateUser(UserEntity userEntity) {
        String query = """
                UPDATE users SET name = ?, email = ?, image_url = ?, role = ?
                WHERE id = ?
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(query, new String[]{"id"});
                statement.setString(1, userEntity.getId());
                statement.setString(2, userEntity.getName());
                statement.setString(3, userEntity.getEmail());
                statement.setString(4, userEntity.getImageURL());
                statement.setString(5, userEntity.getRole().name());
                return statement;
            }, keyHolder);
        } catch (Exception e) {
            logger.error(String.format("Failed to update user with id %s", userEntity.getId()), e);
            return null;
        }

        return Objects.requireNonNull(keyHolder.getKey()).toString();
    }

    public Boolean deleteById(String id) {
        String query = """
                DELETE FROM users
                WHERE id = ?
                """;
        int numberAffectedRecords;

        try {
            numberAffectedRecords = jdbcTemplate.update(query, id);
        } catch (Exception e) {
            logger.error(String.format("Failed to delete user with id %s", id), e);
            return null;
        }

        return numberAffectedRecords > 0;
    }

    public Boolean existsById(String id) {
        String query = """
                SELECT COUNT(*) FROM users
                WHERE id = ?
                """;
        Integer numberAffectedRecords;

        try {
            numberAffectedRecords = jdbcTemplate.queryForObject(query, Integer.class, id);
            if (numberAffectedRecords == null) {
                return false;
            }
        } catch (Exception e) {
            logger.error(String.format("Failed to check if user with id %s exists", id), e);
            return null;
        }

        return numberAffectedRecords > 0;
    }
}
