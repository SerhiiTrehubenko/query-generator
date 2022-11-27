package com.tsa.orm.services;

import com.tsa.orm.annotation.Column;
import com.tsa.orm.annotation.Entity;
import com.tsa.orm.annotation.Table;
import com.tsa.orm.interfaces.QueryGenerator;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DefaultQueryGenerator implements QueryGenerator {
    private static final String SELECT = "SELECT ";
    private static final String DELETE = "DELETE ";
    private static final String FROM = " FROM ";
    private static final String WHERE = " WHERE ";
    private static final String SEMICOLON = ";";
    private static final String[] ID = {"id_", "id", "_id"};

    public DefaultQueryGenerator() {
    }

    @Override
    public String findAll(Class<?> type) {
        String tableName = getTableName(type);
        return getQuery(SELECT, getColumns(type), FROM, tableName, SEMICOLON);
    }

    @Override
    public String findById(Class<?> type, Serializable id) {
        return generateQueryForFindOrDeleteById(type, id, SELECT);
    }

    @Override
    public String deleteById(Class<?> type, Serializable id) {
        return generateQueryForFindOrDeleteById(type, id, DELETE);
    }

    @Override
    public String insert(Object object) {
        String tableName = getTableName(object);

        Map<String, Object> map = getMapOfColumnsAndValues(object);

        return getQuery("INSERT INTO ", tableName,
                " (", getColumnsFromMap(map), ") VALUES (", getValuesFromMap(map), ");");
    }

    @Override
    public String update(Object object) {
        String tableName = getTableName(object);

        Map<String, Object> map = getMapOfColumnsAndValues(object);

        return getQuery("UPDATE ", tableName,
                " SET ", createSetForQueryFromMap(map),
                WHERE, createConditionForQueryFromMap(map), SEMICOLON);
    }

    private void requireNotNull(Object object) {
        Objects.requireNonNull(object, "The arguments cannot be \"null\"");

    }

    private void checkIncomeObjectOrClassOnEntityAnnotationAndNotNull(Object object) {
        requireNotNull(object);

        Class<?> retrievedClazz = getClass(object);
        Entity entityAnnotation = retrievedClazz.getAnnotation(Entity.class);

        if (Objects.isNull(entityAnnotation)) {
            throw new IllegalArgumentException("Provided class: " + retrievedClazz.getName() + " is not an Entity");
        }
    }

    String getTableName(Object object) {
        checkIncomeObjectOrClassOnEntityAnnotationAndNotNull(object);
        Class<?> clazz = getClass(object);
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation != null) {
            return tableAnnotation.name().toLowerCase();
        } else {
            return clazz.getSimpleName().toLowerCase();
        }
    }
    private Class<?> getClass(Object object) {
        return object instanceof Class<?> ? (Class<?>) object : object.getClass();
    }
    String getColumnNameConsideringAnnotation(Field field) {
        Column columnNameAnnotation = field.getAnnotation(Column.class);
        if (columnNameAnnotation != null) {
            return columnNameAnnotation.name();
        } else {
            return field.getName();
        }
    }

    Map<String, Object> getMapOfColumnsAndValues(Object object) {

        Map<String, Object> map = new HashMap<>();
        Deque<Class<?>> listOfSuperClasses = new ArrayDeque<>();
        getSuperClassesFromHierarchy(object.getClass(), listOfSuperClasses);

        listOfSuperClasses.forEach(clazz -> {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    map.put(getColumnNameConsideringAnnotation(field), field.get(object));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return map;
    }

    List<String> getListOfColumnsFromOneClass(Class<?> type) {
        List<String> listOfColumnNames;
        Field[] fields = type.getDeclaredFields();
        try {
            listOfColumnNames = Arrays.stream(fields)
                    .map(this::getColumnNameConsideringAnnotation)
                    .collect(Collectors.toList());
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        }
        return listOfColumnNames;
    }

    String getColumns(Class<?> type) {
        StringJoiner collectFieldNames = new StringJoiner(", ");
        List<String> Columns = getListOfColumnsFromHierarchy(type);

        Columns.forEach(collectFieldNames::add);

        return collectFieldNames.toString();
    }

    List<String> getListOfColumnsFromHierarchy(Class<?> type) {
        List<String> stringList = new ArrayList<>();
        Deque<Class<?>> listOfSuperClasses = new ArrayDeque<>();
        getSuperClassesFromHierarchy(type, listOfSuperClasses);
        listOfSuperClasses.forEach(clazz ->
                stringList.addAll(getListOfColumnsFromOneClass(clazz))
        );
        return stringList;
    }

    void getSuperClassesFromHierarchy(Class<?> type, Deque<Class<?>> dequeue) {
        if (type != null && type != Object.class) {
            dequeue.addFirst(type);
            getSuperClassesFromHierarchy(type.getSuperclass(), dequeue);
        }
    }

    private String getQuery(String... arg) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : arg) {
            stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }

    Long parseId(Serializable id) {
        requireNotNull(id);
        Long incomeId = null;
        Class<?> idClassName = id.getClass();
        if (CharSequence.class.isAssignableFrom(idClassName)) {
            Matcher matcher = Pattern.compile("\\d+").matcher(id.toString());
            while (matcher.find()) {
                String result = matcher.group();
                incomeId = Long.parseLong(result);
                if (!result.isEmpty()) {
                    break;
                }
            }
        } else if (Integer.class.isAssignableFrom(idClassName)) {
            Integer integer = (Integer) id;
            incomeId = integer.longValue();
        } else if (Long.class.isAssignableFrom(idClassName)) {
            incomeId = (Long) id;
        }
        if (incomeId == null) {
            throw new IllegalArgumentException("Entity id should be String, Integer or Long");
        }
        return incomeId;
    }

    private String generateQueryForFindOrDeleteById(Class<?> type, Serializable id, String action) {
        String tableName = getTableName(type);
        Long incomeId = parseId(id);
        String columns = getColumns(type);
        String foundNameColumnId = "";
        for (String string : columns.split(", ")) {
            if (isColumnId(string.toLowerCase())) {
                foundNameColumnId = string;
            }
        }
        if (action.equals(DELETE)) {
            return getQuery(action,
                    FROM, tableName, WHERE, foundNameColumnId, " = ", String.valueOf(incomeId), SEMICOLON);
        } else {
            return getQuery(action, columns,
                    FROM, tableName, WHERE, foundNameColumnId, " = ", String.valueOf(incomeId), SEMICOLON);
        }
    }

    String getColumnsFromMap(Map<String, Object> map) {
        StringJoiner stringJoiner = new StringJoiner(", ");
        for (String key : map.keySet()) {
            if (!isColumnId(key)) {
                stringJoiner.add(key);
            }
        }
        return stringJoiner.toString();
    }

    String getValuesFromMap(Map<String, Object> map) {
        StringJoiner stringJoiner = new StringJoiner(", ");
        for (String key : map.keySet()) {
            if (!isColumnId(key)) {
                Object value = map.get(key);
                if (CharSequence.class.isAssignableFrom(value.getClass())) {
                    stringJoiner.add("'" + value + "'");
                } else {
                    stringJoiner.add(String.valueOf(value));
                }
            }
        }
        return stringJoiner.toString();
    }

    String createSetForQueryFromMap(Map<String, Object> map) {
        StringJoiner stringJoiner = new StringJoiner(", ");
        for (String key : map.keySet()) {
            if (!isColumnId(key)) {
                Object value = map.get(key);
                if (CharSequence.class.isAssignableFrom(value.getClass())) {
                    stringJoiner.add(key + "=" + "'" + value + "'");
                } else {
                    stringJoiner.add(key + "=" + value);
                }
            }
        }
        return stringJoiner.toString();
    }

    private boolean isColumnId(String key) {
        return key.startsWith(ID[0]) | key.equals(ID[1]) | key.endsWith(ID[2]);
    }

    String createConditionForQueryFromMap(Map<String, Object> map) {

        for (String key : map.keySet()) {
            if (isColumnId(key)) {
                return key + "=" + map.get(key);
            }
        }
        return "";
    }
}
