package ru.job4j.grabber.service;

import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.stores.Store;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Класс SchedulerManager управляет задачами парсинга через библиотеку Quartz.
 * Поля:
 * scheduler: Scheduler — объект для управления расписанием задач.
 * Методы:
 * init(): void — инициализирует планировщик.
 * load(int period, Class<SuperJobGrab> task, Store store): void — создает и запускает задачу с указанным интервалом.
 * close(): void — завершает работу планировщика.
 */
public class SchedulerManager implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(SchedulerManager.class);
    private Scheduler scheduler;

    public void init() {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
        } catch (SchedulerException se) {
            LOG.error("When init scheduler", se);
        }
    }

    public void load(int period, Class<? extends Job> task, Store store) {
        try {
            var data = new JobDataMap();
            data.put("store", store);
            var job = newJob(task)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(period)
                    .repeatForever();

            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();

            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            LOG.error("When init job", se);
        }
    }

    public void close() {
        if (scheduler != null) {
            try {
                scheduler.shutdown(true);
            } catch (SchedulerException e) {
                LOG.error("When shutdown scheduler", e);
            }
        }
    }
}
