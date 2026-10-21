package ru.job4j.grabber.stores;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.job4j.grabber.model.Post;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StoreContractTest {

    private static Connection connection;
    private static Store jdbcStore;

    @BeforeAll
    static void initDb() throws SQLException {
        connection = DriverManager.getConnection(
                "jdbc:h2:mem:storecontract;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
        try (Statement st = connection.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS post (
                        id SERIAL PRIMARY KEY,
                        title TEXT,
                        link TEXT UNIQUE,
                        description TEXT,
                        time TIMESTAMP WITH TIME ZONE
                    )
                    """);
        }
        jdbcStore = new JdbcStore(connection);
    }

    @AfterAll
    static void closeDb() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @BeforeEach
    void cleanTable() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("DELETE FROM post");
            st.execute("ALTER TABLE post ALTER COLUMN id RESTART WITH 1");
        }
    }

    static Stream<Store> stores() {
        return Stream.of(new MemStore(), jdbcStore);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("stores")
    @DisplayName("save() проставляет id и findById() возвращает сохранённый пост")
    void whenSaveThenFindByIdReturnsSamePost(Store store) {
        Post post = new Post();
        post.setTitle("Java Developer");
        post.setLink("https://example.com/job/1");

        store.save(post);

        assertThat(post.getId())
                .as("id должен быть сгенерирован хранилищем")
                .isNotNull();

        Optional<Post> found = store.findById(post.getId());

        assertThat(found)
                .as("пост с id=%s должен находиться", post.getId())
                .isPresent()
                .get()
                .extracting(Post::getTitle, Post::getLink)
                .containsExactly("Java Developer", "https://example.com/job/1");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("stores")
    @DisplayName("getAll() возвращает все сохранённые посты")
    void whenSaveMultiplePostsThenGetAllReturnsAll(Store store) {
        Post first = new Post();
        first.setTitle("First");
        first.setLink("https://example.com/job/1");

        Post second = new Post();
        second.setTitle("Second");
        second.setLink("https://example.com/job/2");

        store.save(first);
        store.save(second);

        List<Post> all = store.getAll();

        assertThat(all)
                .as("getAll() должен вернуть оба поста")
                .hasSize(2)
                .extracting(Post::getTitle)
                .containsExactlyInAnyOrder("First", "Second");

        assertThat(all)
                .extracting(Post::getId)
                .doesNotContainNull()
                .doesNotHaveDuplicates();
    }
}
