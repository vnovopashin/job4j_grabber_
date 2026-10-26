package ru.job4j.grabber.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import ru.job4j.grabber.model.Post;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HabrCareerParseTest {

    @Test
    void whenParseFixtureThenExtractsAllVacancies() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/habr_page.html")) {
            assertThat(in).as("фикстура habr_page.html не найдена").isNotNull();
            Document doc = Jsoup.parse(in, StandardCharsets.UTF_8.name(), "https://career.habr.com");

            List<Post> posts = new HabrCareerParse().parse(doc);

            assertThat(posts)
                    .as("на фикстуре должно быть ровно 25 вакансий")
                    .hasSize(25);

            assertThat(posts)
                    .extracting(Post::getLink)
                    .as("все ссылки должны вести на career.habr.com/vacancies/")
                    .allMatch(link -> link.startsWith("https://career.habr.com/vacancies/"));

            assertThat(posts)
                    .extracting(Post::getTime)
                    .as("у всех вакансий на фикстуре дата должна распарситься")
                    .doesNotContainNull();

            assertThat(posts)
                    .extracting(Post::getTitle)
                    .as("title не должен быть null/пустым")
                    .doesNotContainNull()
                    .allMatch(title -> !title.isBlank());

            assertThat(posts)
                    .extracting(Post::getLink)
                    .as("якорные вакансии должны присутствовать")
                    .contains(
                            "https://career.habr.com/vacancies/1000168359",
                            "https://career.habr.com/vacancies/1000168906"
                    );

            assertThat(posts)
                    .extracting(Post::getDescription)
                    .as("parse(Document) не заполняет description — это делает fetch()")
                    .containsOnlyNulls();
        }
    }

    @Test
    void whenExtractDescriptionFromFixtureThenReturnsText() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/habr_vacancy_page.html")) {
            assertThat(in).as("фикстура habr_vacancy_page.html не найдена").isNotNull();
            Document doc = Jsoup.parse(in, StandardCharsets.UTF_8.name(), "https://career.habr.com");

            String description = new HabrCareerParse().extractDescription(doc);

            assertThat(description)
                    .as("описание должно быть извлечено")
                    .isNotNull()
                    .isNotBlank()
                    .contains("О компании и команде")
                    .contains("Java")
                    .contains("Условия работы")
                    .doesNotContain("Описание вакансии");
        }
    }

    @Test
    void whenNoDescriptionBlockThenReturnsNull() {
        Document doc = Jsoup.parse("<html><body><div>нет описания</div></body></html>");

        assertThat(new HabrCareerParse().extractDescription(doc)).isNull();
    }

    @Test
    void whenDescriptionBlockIsEmptyThenReturnsNull() {
        Document doc = Jsoup.parse(
                "<html><body><div class=\"vacancy-description__text\">   </div></body></html>");

        assertThat(new HabrCareerParse().extractDescription(doc)).isNull();
    }
}
