package ru.job4j.grabber.stores;

import ru.job4j.grabber.model.Post;

import java.sql.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Класс JdbcStore реализует интерфейс Store для хранения данных в базе PostgreSQL.
 * Поле connection: Connection используется для взаимодействия с базой.
 * Методы:
 * save(Post post): void — сохраняет вакансию в базе данных.
 * getAll(): List<Post> — извлекает все вакансии из базы.
 * findById(Long id): Optional<Post> — находит вакансию по идентификатору.
 */
public class JdbcStore implements Store {
    private final Connection connection;

    public JdbcStore(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Post post) {
        String sql = """
                INSERT INTO post(title, link, description, time)
                                VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING""";
        try (PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getLink());
            statement.setString(3, post.getDescription());
            if (post.getTime() != null) {
                OffsetDateTime odt = Instant.ofEpochSecond(post.getTime())
                        .atOffset(ZoneOffset.UTC);
                statement.setObject(4, odt);
            } else {
                statement.setNull(4, Types.TIMESTAMP_WITH_TIMEZONE);
            }
            statement.executeUpdate();
            try (ResultSet generatedKey = statement.getGeneratedKeys()) {
                if (generatedKey.next()) {
                    post.setId(generatedKey.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось сохранить Post: " + post, e);
        }
    }

    @Override
    public List<Post> getAll() {
        String sql = "SELECT id, title, link, description, time FROM post ORDER BY time DESC NULLS LAST";
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                posts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось получить список Post", e);
        }
        return posts;
    }

    @Override
    public Optional<Post> findById(Long id) {
        String sql = "SELECT id, title, link, description, time FROM post WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти Post по id=" + id, e);
        }
    }

    private Post mapRow(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getLong("id"));
        post.setTitle(rs.getString("title"));
        post.setLink(rs.getString("link"));
        post.setDescription(rs.getString("description"));
        OffsetDateTime odt = rs.getObject("time", OffsetDateTime.class);
        Instant time = odt != null ? odt.toInstant() : null;
        post.setTime(time == null ? null : time.getEpochSecond());
        return post;
    }
}
