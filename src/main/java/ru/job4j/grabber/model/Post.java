package ru.job4j.grabber.model;

import java.util.Objects;

/**
 * Класс Post представляет модель данных, описывающую вакансию. Он включает следующие поля:
 * id: Long — уникальный идентификатор вакансии.
 * title: String — название вакансии.
 * link: String — ссылка на страницу с вакансией.
 * description: String — описание вакансии.
 * time: Long — дата и время публикации вакансии.
 * Этот класс содержит геттеры, сеттеры,
 * а также переопределенные методы equals() и hashCode() для корректного сравнения объектов.
 */
public class Post {

    private Long id;
    private String title;
    private String link;
    private String description;
    /**
     * Время публикации поста в epoch seconds (UTC).
     * Может быть null, если время неизвестно.
     */
    private Long time;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Post post = (Post) o;
        return Objects.equals(id, post.id) && Objects.equals(title, post.title) && Objects.equals(link, post.link) && Objects.equals(description, post.description) && Objects.equals(time, post.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, link, description, time);
    }

    @Override
    public String toString() {
        return "Post{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", link='" + link + '\''
                + ", description='" + description + '\''
                + ", time=" + time
                + '}';
    }
}
