package ru.job4j.grabber.service;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.stores.Store;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.util.List;

/**
 * Класс SuperJobGrab реализует интерфейс Job (из Quartz)
 * и отвечает за выполнение задачи парсинга.
 * Поле:
 * parse: Parse — объект, реализующий логику парсинга.
 * Метод:
 * execute(JobExecutionContext context): void — выполняет задачу парсинга
 * и сохраняет данные через хранилище (Store).
 */

@DisallowConcurrentExecution
public class SuperJobGrab implements Job {
    private static final Logger LOG = Logger.getLogger(SuperJobGrab.class);

    @Override
    public void execute(JobExecutionContext context) {
        Store store = (Store) context.getJobDetail().getJobDataMap().get("store");
        Parse parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> posts = parse.fetch();
        posts.forEach(store::save);
        long inserted = posts.stream().filter(p -> p.getId() != null).count();
        LOG.info("Получено: " + posts.size() + ", вставлено: " + inserted);
    }
}
