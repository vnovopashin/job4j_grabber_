package ru.job4j.grabber.service;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Класс необходим для загрузки свойств: настройка периодичности,
 * настройка соединения с базой данных
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
