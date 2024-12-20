package com.github.groundbreakingmc.gigachat.database;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseHandler {

    private static HikariDataSource dataSource;

    private DatabaseHandler() {

    }

    public static void createConnection(final GigaChat plugin) {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getDriverUrl(plugin));
        hikariConfig.setMinimumIdle(4);
        hikariConfig.setMaximumPoolSize(16);
        hikariConfig.setConnectionTimeout(10000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(hikariConfig);
    }

    private static String getDriverUrl(final GigaChat plugin) {
        final File dbFile = new File(plugin.getDataFolder() + File.separator + "database.db");
        return "jdbc:sqlite:" + dbFile;
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeConnection() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
