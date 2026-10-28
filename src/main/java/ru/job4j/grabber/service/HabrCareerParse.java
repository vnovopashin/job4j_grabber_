package ru.job4j.grabber.service;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс HabrCareerParse реализует интерфейс Parse.
 * Используется для парсинга вакансий с платформы career.habr.com.
 * Поля:
 * SOURCE_LINK: String — базовая ссылка на сайт.
 * PREFIX: String — префикс для ссылок на страницы с вакансиями.
 * SUFFIX: String — параметры для фильтрации вакансий.
 * Метод fetch() загружает и обрабатывает HTML-страницы, извлекая данные о вакансиях.
 */
public class HabrCareerParse implements Parse {
    private static final Logger LOG = Logger.getLogger(HabrCareerParse.class);
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PREFIX = "/vacancies?page=";
    private static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final int PAGE_COUNT = 5;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> fetch() {
        var result = new ArrayList<Post>();
        for (int pageNumber = 1; pageNumber <= PAGE_COUNT; pageNumber++) {
            if (pageNumber > 1) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOG.warn("Прервано во время паузы между страницами");
                    break;
                }
            }
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
            try {
                var document = Jsoup.connect(fullLink)
                        .userAgent("Mozilla/5.0")
                        .timeout(10_000)
                        .get();
                List<Post> pagePosts = parse(document);
                if (pagePosts.isEmpty()) {
                    LOG.info("Страница " + pageNumber + " пустая — останавливаемся");
                    break;
                }
                for (Post post : pagePosts) {
                    post.setDescription(retrieveDescription(post.getLink()));
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOG.warn("Прервано во время паузы между описаниями");
                        return result;
                    }
                }
                result.addAll(pagePosts);
            } catch (IOException e) {
                LOG.error("Не удалось загрузить страницу " + pageNumber, e);
            }
        }
        return result;
    }

    List<Post> parse(Document document) {
        var result = new ArrayList<Post>();
        var rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            var dateElement = row.select(".basic-date").first();
            var titleLink = row.select(".vacancy-card__title-link").first();
            if (dateElement == null || titleLink == null) {
                String href = row.select(".vacancy-card__backdrop-link").attr("href");
                LOG.warn("Пропускаю карточку без даты или ссылки: href=" + href);
                return;
            }
            String dateTime = dateElement.attr("datetime");
            String vacancyName = titleLink.text();
            String link = String.format("%s%s", SOURCE_LINK, titleLink.attr("href"));
            Long time = null;
            try {
                time = dateTimeParser.parse(dateTime).toEpochSecond(ZoneOffset.UTC);
            } catch (DateTimeParseException e) {
                LOG.warn(String.format("Не смог распарсить дату '%s' для вакансии '%s'",
                        dateTime, vacancyName));
            }
            var post = new Post();
            post.setTitle(vacancyName);
            post.setLink(link);
            post.setTime(time);
            result.add(post);
        });
        return result;
    }

    private String retrieveDescription(String link) {
        if (link == null || link.isBlank()) {
            return null;
        }
        try {
            Document doc = Jsoup.connect(link)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .get();
            return extractDescription(doc);
        } catch (IOException e) {
            LOG.error("Не удалось загрузить страницу вакансии: " + link, e);
            return null;
        }
    }

    /**
     * Извлекает текст описания вакансии из уже загруженного Document.
     * Не делает сетевых запросов — тестируется на фикстуре.
     *
     * @return текст описания или null, если блока нет
     */
    String extractDescription(Document document) {
        var descriptionBlock = document.selectFirst(".vacancy-description__text");
        if (descriptionBlock == null) {
            return null;
        }
        String text = descriptionBlock.text();
        return text.isBlank() ? null : text;
    }

    public static void main(String[] args) {
        HabrCareerDateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        HabrCareerParse parse = new HabrCareerParse(dateTimeParser);
        List<Post> posts = parse.fetch();
        for (Post post : posts) {
            System.out.println(post);
        }
    }
}
