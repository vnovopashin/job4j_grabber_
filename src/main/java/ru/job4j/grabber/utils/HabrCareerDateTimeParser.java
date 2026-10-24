package ru.job4j.grabber.utils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class HabrCareerDateTimeParser implements DateTimeParser {

    /**
     * Парсит ISO-8601 строку с offset'ом (например, "2026-10-14T14:27:16+03:00")
     * в LocalDateTime без offset'а ("2026-10-14T14:27:16").
     *
     * @param parse дата в формате строки
     * @return LocalDateTime или null, если вход null/blank
     */
    @Override
    public LocalDateTime parse(String parse) {
        if (parse == null || parse.isBlank()) {
            return null;
        }
        return OffsetDateTime.parse(parse, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
    }
}
