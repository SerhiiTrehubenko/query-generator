package com.tsa.database.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbConnector {
    private static final String PREFIX_MYSQL = "jdbc:postgresql://";
    private static final String HOSTNAME = "hostName";
    private static final String PORT = "port";
    private static final String DB_NAME = "dbName";
    private static final String USER_NAME = "userName";
    private static final String PASSWORD = "password";

    public static Connection getConnection (Properties properties) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(PREFIX_MYSQL);
        stringBuilder.append(properties.getProperty(HOSTNAME));
        stringBuilder.append(":");
        stringBuilder.append(properties.getProperty(PORT));
        stringBuilder.append("/");
        stringBuilder.append(properties.getProperty(DB_NAME));
        try {
            return DriverManager.getConnection(stringBuilder.toString(),
                    properties.getProperty(USER_NAME),
                    properties.getProperty(PASSWORD));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
