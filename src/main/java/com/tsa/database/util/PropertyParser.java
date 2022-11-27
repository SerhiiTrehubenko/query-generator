package com.tsa.database.util;

import java.io.FileInputStream;
import java.util.Properties;

public class PropertyParser {
    public PropertyParser() {
    }

    public static Properties getProperties() {
        var PropertyParser = new PropertyParser();
        try (var input = PropertyParser.getClass()
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            return properties;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static Properties getCustomProperties(String path) {
        try (var input = new FileInputStream(path)) {
            Properties properties = new Properties();
            properties.load(input);
            return properties;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}