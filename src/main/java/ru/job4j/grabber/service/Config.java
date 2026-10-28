package ru.job4j.grabber.service;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Класс Config предназначен для работы с конфигурацией приложения.
 * Содержит поле properties: Properties, которое хранит параметры из конфигурационного файла.
 * Основные методы:
 * load(String file): void — загружает свойства из указанного файла.
 * get(String key): String — возвращает значение свойства по ключу.
 * Этот класс используется для настройки приложения, например, указания интервала работы парсера.
 */
public class Config {
    private static final Logger LOG = Logger.getLogger(Config.class);
    private final Properties properties = new Properties();

    public void load(String file) {
        InputStream input = Config.class.getClassLoader().getResourceAsStream(file);
        if (input == null) {
            throw new IllegalStateException(file + " not found");
        }
        try (input) {
            properties.load(input);
        } catch (IOException io) {
            throw new IllegalStateException("Не удалось прочитать " + file, io);
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}
