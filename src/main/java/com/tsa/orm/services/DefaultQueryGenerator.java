package com.tsa.orm.services;

import com.tsa.orm.annotation.Column;
import com.tsa.orm.annotation.Entity;
import com.tsa.orm.annotation.Id;
import com.tsa.orm.annotation.Table;
import com.tsa.orm.interfaces.QueryGenerator;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultQueryGenerator implements QueryGenerator {
    private static final String SELECT = "SELECT ";
    private static final String DELETE = "DELETE";
    private static final String FROM = " FROM ";
    private static final String WHERE = " WHERE ";
    private static final String SEMICOLON = ";";

    /**
     * Entity is allowed to have only one @Id
     */
    private final List<String> listOfIds = new ArrayList<>();

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
        Id idColumn = field.getAnnotation(Id.class);
        Column columnNameAnnotation = field.getAnnotation(Column.class);
        String columnName;
        if (columnNameAnnotation != null) {
            columnName = columnNameAnnotation.name();
        } else if (idColumn != null) {
            String name = idColumn.name();
            columnName = name.equals("null") ? field.getName() : name;
            listOfIds.add(columnName);
        } else {
            columnName = field.getName();
        }
        return columnName;
    }

    Map<String, Object> getMapOfColumnsAndValues(Object object) {
        listOfIds.clear();
        Deque<Class<?>> listOfSuperClasses = new ArrayDeque<>();
        enrichWithSuperClassesFromHierarchy(object.getClass(), listOfSuperClasses);

        Map<String, Object> map = listOfSuperClasses.stream()
                .map(Class::getDeclaredFields)
                .flatMap(Stream::of)
                .peek(field -> field.setAccessible(true))
                .collect(Collectors.toMap(this::getColumnNameConsideringAnnotation, field -> {
                    try {
                        return field.get(object);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }));
        ensureIdPresent();
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
        listOfIds.clear();
        List<String> columns = getListOfColumnsFromHierarchy(type);
        ensureIdPresent();
        return String.join(", ", columns);
    }

    private void ensureIdPresent() {
        int numberOfIds = listOfIds.size();
        if (numberOfIds != 1) {
            throw new IllegalArgumentException("The Entity should have only one id, the current Entity has: " + numberOfIds);
        }
    }

    List<String> getListOfColumnsFromHierarchy(Class<?> type) {
        List<String> listOfColumns;
        Deque<Class<?>> listOfSuperClasses = new ArrayDeque<>();
        enrichWithSuperClassesFromHierarchy(type, listOfSuperClasses);

        listOfColumns = listOfSuperClasses.stream()
                .map(this::getListOfColumnsFromOneClass)
                .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);

        return listOfColumns;
    }

    void enrichWithSuperClassesFromHierarchy(Class<?> type, Deque<Class<?>> dequeue) {
        if (type != null && type != Object.class) {
            dequeue.addFirst(type);
            enrichWithSuperClassesFromHierarchy(type.getSuperclass(), dequeue);
        }
    }

    private String getQuery(String... args) {
        return String.join("", args);
    }

    String parseId(Serializable id) {
        requireNotNull(id);
        String incomeId = null;
        Class<?> classOfId = id.getClass();
        if (CharSequence.class.isAssignableFrom(classOfId)) {
            try {
                Long castedId = Long.parseLong(String.valueOf(id));
                incomeId = String.valueOf(castedId);
            } catch (NumberFormatException e) {
                incomeId = "'" + id + "'";
            }
        } else if (Integer.class.isAssignableFrom(classOfId)) {
            incomeId = String.valueOf(id);
        } else if (Long.class.isAssignableFrom(classOfId)) {
            incomeId = String.valueOf(id);
        }
        if (incomeId == null) {
            throw new IllegalArgumentException("Entity id should be String, Integer or Long");
        }
        return incomeId;
    }

    private String generateQueryForFindOrDeleteById(Class<?> type, Serializable id, String action) {
        String tableName = getTableName(type);
        String columns = getColumns(type);
        String incomeId = parseId(id);
        String condition = listOfIds.get(0);

        if (action.equals(DELETE)) {
            return getQuery(action,
                    FROM, tableName, WHERE, condition, " = ", String.valueOf(incomeId), SEMICOLON);
        } else {
            return getQuery(action, columns,
                    FROM, tableName, WHERE, condition, " = ", String.valueOf(incomeId), SEMICOLON);
        }
    }

    String getColumnsFromMap(Map<String, Object> map) {
        return map.keySet().stream()
                .filter(column -> !isColumnId(column))
                .collect(Collectors.joining(", "));
    }

    String getValuesFromMap(Map<String, Object> map) {
        return map.keySet().stream()
                .filter(column -> !isColumnId(column))
                .map(filteredColumn -> {
                    Object value = map.get(filteredColumn);
                    String valueToReturn;
                    if (CharSequence.class.isAssignableFrom(value.getClass())) {
                        valueToReturn = "'" + value + "'";
                    } else {
                        valueToReturn = String.valueOf(value);
                    }
                    return valueToReturn;
                }).collect(Collectors.joining(", "));
    }

    String createSetForQueryFromMap(Map<String, Object> map) {
        return map.keySet().stream()
                .filter(column -> !isColumnId(column))
                .map(filteredColumn -> {
                    Object value = map.get(filteredColumn);
                    String valueToReturn;
                    if (CharSequence.class.isAssignableFrom(value.getClass())) {
                        valueToReturn = filteredColumn + "=" + "'" + value + "'";
                    } else {
                        valueToReturn = filteredColumn + "=" + value;
                    }
                    return valueToReturn;
                }).collect(Collectors.joining(", "));
    }

    private boolean isColumnId(String key) {
        return key.equals(listOfIds.get(0));
    }

    String createConditionForQueryFromMap(Map<String, Object> map) {

        return map.keySet().stream()
                .filter(this::isColumnId)
                .map(filteredIdColumn -> filteredIdColumn + "=" + map.get(filteredIdColumn))
                .collect(Collectors.joining());
    }
}
