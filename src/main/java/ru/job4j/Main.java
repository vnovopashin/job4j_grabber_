package ru.job4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.grabber.service.Config;
import ru.job4j.grabber.service.SchedulerManager;
import ru.job4j.grabber.service.SuperJobGrab;
import ru.job4j.grabber.service.Web;
import ru.job4j.grabber.stores.JdbcStore;
import ru.job4j.grabber.stores.Store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        var config = new Config();
        config.load("db/liquibase.properties");

        SchedulerManager scheduler = new SchedulerManager();
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(
                    config.get("url"),
                    config.get("username"),
                    config.get("password"));
            Store store = new JdbcStore(connection);

            scheduler.init();
            scheduler.load(
                    Integer.parseInt(config.get("rabbit.interval")),
                    SuperJobGrab.class,
                    store);

            new Thread(() -> new Web(store)
                    .start(Integer.parseInt(config.get("server.port"))))
                    .start();

            // Не даём main завершиться, пока живы Web и Quartz
            Thread.currentThread().join();
        } catch (SQLException | InterruptedException e) {
            LOG.error("When starting application", e);
        } finally {
            scheduler.close();
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOG.error("When closing connection", e);
                }
            }
        }
    }
}
