package ru.job4j.grabber.service;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import ru.job4j.grabber.stores.Store;

/**
 * Класс описывающий парсер - SuperJobGrab
 */
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
