package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HabrCareerParseDateTimeTest {

    private final HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();

    @Test
    void whenValidIsoWithPositiveOffsetThenReturnsLocalDateTimeWithoutOffset() {
        LocalDateTime result = parser.parse("2026-10-14T14:27:16+03:00");

        assertThat(result)
                .isEqualTo(LocalDateTime.of(2026, 10, 14, 14, 27, 16));
    }

    @Test
    void whenValidIsoWithUtcThenReturnsLocalDateTime() {
        LocalDateTime result = parser.parse("2026-10-14T14:27:16Z");

        assertThat(result)
                .isEqualTo(LocalDateTime.of(2026, 10, 14, 14, 27, 16));
    }

    @Test
    void whenValidIsoWithNegativeOffsetThenOffsetIsDroppedNotConverted() {
        LocalDateTime result = parser.parse("2026-10-14T14:27:16-05:00");

        assertThat(result)
                .as("offset должен отбрасываться, а не конвертироваться в UTC")
                .isEqualTo(LocalDateTime.of(2026, 10, 14, 14, 27, 16));
    }

    @Test
    void whenValidIsoWithNanosThenNanosPreserved() {
        LocalDateTime result = parser.parse("2026-10-14T14:27:16.123+03:00");

        assertThat(result)
                .isEqualTo(LocalDateTime.of(2026, 10, 14, 14, 27, 16, 123_000_000));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void whenNullOrBlankThenReturnsNull(String value) {
        assertThat(parser.parse(value)).isNull();
    }

    @Test
    void whenIsoWithoutOffsetThenThrows() {
        assertThatThrownBy(() -> parser.parse("2026-10-14T14:27:16"))
                .as("метод требует offset — строка без него должна падать")
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    void whenGarbageThenThrows() {
        assertThatThrownBy(() -> parser.parse("not a date"))
                .isInstanceOf(DateTimeParseException.class);
    }
}
