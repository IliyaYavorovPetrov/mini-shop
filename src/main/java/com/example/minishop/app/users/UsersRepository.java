package com.example.minishop.app.users;

import com.example.minishop.app.users.entities.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Repository
public class UsersRepository {
    private final Logger logger = LoggerFactory.getLogger(UsersRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public UsersRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String save(UserEntity userEntity) {
        String query = """
                INSERT INTO users (id, name, email, imageURL, role, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Instant currentTime = Instant.now();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(query, new String[]{"id"});
                statement.setString(1, userEntity.getId());
                statement.setString(2, userEntity.getName());
                statement.setString(3, userEntity.getEmail());
                statement.setString(4, userEntity.getImageURL());
                statement.setString(5, userEntity.getRole().name());
                statement.setTimestamp(6, Timestamp.from(currentTime));
                statement.setTimestamp(7, Timestamp.from(currentTime));
                return statement;
            }, keyHolder);
        }
        catch (Exception e) {
            logger.error(String.format("Failed to save user with id %s", userEntity.getId()), e);
            return null;
        }

        return Objects.requireNonNull(keyHolder.getKey()).toString();
    }

    public UserEntity findById(String id) {
        String query = """
                SELECT id, name, email, imageURL, role FROM users
                WHERE id = ?
                """;

        try {
            return jdbcTemplate.queryForObject(query, UserMapper::fromResultSetToUserEntity, id);
        } catch (Exception e) {
            logger.error(String.format("Failed to find user with id %s", id), e);
            return null;
        }
    }

    public List<UserEntity> findAll() {
        String query = """
                SELECT id, name, email, imageURL, role FROM users
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
                UPDATE users SET name = ?, email = ?, imageURL = ?, role = ?
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
        int numberAffectedRecords;

        try {
            numberAffectedRecords = jdbcTemplate.update(query, id);
        } catch (Exception e) {
            logger.error(String.format("Failed to check if user with id %s exists", id), e);
            return null;
        }

        return numberAffectedRecords > 0;
    }
}
