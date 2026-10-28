package ru.job4j.grabber.service;

import ru.job4j.grabber.model.Post;

import java.util.List;

/**
 * Интерфейс Parse описывает операции для парсинга данных с веб-сайтов.
 * Метод: fetch(): List<Post> — возвращает список вакансий.
 */
public interface Parse {
    List<Post> fetch();
}
