package ru.job4j.grabber.stores;

import ru.job4j.grabber.model.Post;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс Store задает контракт для хранилища вакансий. Методы:
 * save(Post post): void — сохраняет вакансию в хранилище.
 * getAll(): List<Post> — возвращает все сохраненные вакансии.
 * findById(Long id): Optional<Post> — находит вакансию по идентификатору.
 */
public interface Store {
    void save(Post post);

    List<Post> getAll();

    Optional<Post> findById(Long id);
}
