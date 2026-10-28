package ru.job4j.grabber.service;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import ru.job4j.grabber.stores.Store;

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
    public void execute(JobExecutionContext context) throws JobExecutionException {
        var store = (Store) context.getJobDetail().getJobDataMap().get("store");
        for (var post : store.getAll()) {
            LOG.info(post.getId() + " — " + post.getTitle());
        }
    }
}
