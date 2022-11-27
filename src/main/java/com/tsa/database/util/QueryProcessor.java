package com.tsa.database.util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryProcessor {

    private final static String GET = "get";
    private final static String STRING = "String";
    private final static char POINTER = '.';

    public static List<Map<String, Object>> select(Connection connection, String query) {
        Map<String, String> mapMeta = new HashMap<>();
        Map<String, Object> mapRow;
        List<Map<String, Object>> listResult = new ArrayList<>();
        try (connection) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int quantityColumn = metaData.getColumnCount();
            for (int i = 1; i <= quantityColumn; i++) {
                mapMeta.put(metaData.getColumnName(i), metaData.getColumnClassName(i));
            }
            Method[] methods = resultSet.getClass().getDeclaredMethods();
            while (resultSet.next()) {
                mapRow = new HashMap<>();
                for (String key : mapMeta.keySet()) {
                    String value = mapMeta.get(key);
                    String type = value.substring(value.lastIndexOf(POINTER)+1);
                    for (Method method : methods) {
                        String methodName = method.getName();
                        Parameter[] methodParameters = method.getParameters();
                        if (methodName.contains(GET) &&
                                !methodName.contains("getNString") &&
                                methodName.contains(type) &&
                                methodParameters.length == 1 &&
                                methodParameters[0].toString().contains(STRING)) {
                            mapRow.put(key, method.invoke(resultSet, key));
                        }
                    }
                }
                listResult.add(mapRow);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return listResult;
    }
    public static void insert(Connection connection, String query) {
        try (connection) {
            Statement statement = connection.createStatement();
            statement.execute(query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
