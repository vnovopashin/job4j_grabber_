package ru.job4j.grabber.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import ru.job4j.grabber.model.Post;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HabrCareerParseTest {

    @Test
    void whenParseFixtureThenExtractsAllVacancies() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/habr_page.html")) {
            assertThat(in).as("фикстура habr_page.html не найдена").isNotNull();
            Document doc = Jsoup.parse(in, StandardCharsets.UTF_8.name(), "https://career.habr.com");

            List<Post> posts = new HabrCareerParse().parse(doc);

            assertThat(posts)
                    .as("на странице должно быть найдено несколько вакансий")
                    .isNotEmpty();

            assertThat(posts)
                    .extracting(Post::getLink)
                    .allMatch(link -> link.startsWith("https://career.habr.com/vacancies/"));

            assertThat(posts)
                    .extracting(Post::getTitle)
                    .doesNotContainNull()
                    .allMatch(title -> !title.isBlank());
            assertThat(posts)
                    .as("на фикстуре должно быть ровно N вакансий")
                    .hasSize(25);

            assertThat(posts)
                    .extracting(Post::getTime)
                    .as("у всех вакансий на фикстуре дата должна распарситься")
                    .doesNotContainNull();

            assertThat(posts)
                    .extracting(Post::getTitle)
                    .contains("Java Developer", "Senior Java/Kotlin Developer");
        }
    }
}
