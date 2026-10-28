package ru.job4j.grabber.stores;

import ru.job4j.grabber.model.Post;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Класс MemStore реализует интерфейс Store и предоставляет хранилище в памяти.
 * Использует коллекцию mem: Map<Long, Post> для хранения объектов.
 * Реализованные методы:
 * save(Post post): void — добавляет вакансию в коллекцию.
 * getAll(): List<Post> — возвращает список всех вакансий.
 * findById(Long id): Optional<Post> — находит вакансию по идентификатору в коллекции.
 */
public class MemStore implements Store {
    private final Map<Long, Post> mem = new HashMap<>();
    private final AtomicLong seq = new AtomicLong();

    @Override
    public void save(Post post) {
        if (post.getId() == null) {
            post.setId(seq.incrementAndGet());
        }
        mem.put(post.getId(), post);
    }

    @Override
    public List<Post> getAll() {
        return new ArrayList<>(mem.values());
    }

    @Override
    public Optional<Post> findById(Long id) {
        return Optional.ofNullable(mem.get(id));
    }
}
