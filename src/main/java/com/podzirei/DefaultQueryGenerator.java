package com.podzirei;

import com.podzirei.annotation.Column;
import com.podzirei.annotation.Table;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.StringJoiner;

public class DefaultQueryGenerator implements QueryGenerator {

    @Override
    //SELECT id, person_name, person_salary FROM Person
    public String findAll(Class<?> clazz) {
        String tableName = getTableName(clazz);

        StringBuilder query = new StringBuilder("SELECT ");
        StringJoiner columnNames = addColumnNames(clazz);
        query.append(columnNames);
        query.append(" FROM ");
        query.append(tableName);
        query.append(";");

        return query.toString();
    }

    @Override
    //"SELECT id, person_name, person_salary FROM Person WHERE id = '" + id + "'"
    public String findById(Class<?> type, Serializable id) {
        String tableName = getTableName(type);

        StringBuilder query = new StringBuilder("SELECT ");
        StringJoiner columnNames = addColumnNames(type);
        query.append(columnNames);
        query.append(" FROM ");
        query.append(tableName);
        query.append(" WHERE id = '");
        query.append(id);
        query.append("'");

        return query.toString();
    }

    @Override
    //DELETE id, person_name, person_salary FROM Person WHERE id = '" + id + "'
    public String deleteById(Class<?> type, Serializable id) {
        String tableName = getTableName(type);

        StringBuilder query = new StringBuilder("DELETE ");
        StringJoiner columnNames = addColumnNames(type);
        query.append(columnNames);
        query.append(" FROM ");
        query.append(tableName);
        query.append(" WHERE id = '");
        query.append(id);
        query.append("'");

        return query.toString();
    }

    @Override
    //INSERT INTO Person (id, person_name, person_salary)
    // VALUES (3, Anton, 234.0)
    public String insert(Object value) throws IllegalAccessException {
        Class<?> type = value.getClass();
        String tableName = getTableName(type);

        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(tableName);
        query.append(" (");

        StringJoiner columnNames = addColumnNames(type);
        query.append(columnNames);
        query.append(")\n");
        query.append("VALUES (");

        StringJoiner columnValues = new StringJoiner(", ");
        for (Field field : type.getDeclaredFields()) {
            field.setAccessible(true);
            Object fieldValue = field.get(value);
            columnValues.add(fieldValue.toString());
        }
        query.append(columnValues);
        query.append(")");

        return query.toString();
    }

    @Override
    //UPDATE Person
    // SET id = 3, person_name = Anton, person_salary = 1000.0
    // WHERE id = 3
    public String update(Object value) throws IllegalAccessException {
        Class<?> type = value.getClass();
        String tableName = getTableName(type);

        StringBuilder query = new StringBuilder("UPDATE ");
        query.append(tableName);
        query.append("\n");
        query.append("SET ");

        StringJoiner columnNames = new StringJoiner(", ");
        int id = 0;
        for (Field field : value.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getName().equals("id")) {
                id = (int) field.get(value);
            }

            Column columnAnnotation = field.getAnnotation(Column.class);
            String columnName = columnAnnotation.name().isEmpty() ? field.getName() : columnAnnotation.name();
            Object fieldValue = field.get(value);

            columnNames.add(columnName + " = " + fieldValue.toString()); // конкантенация, можно переделать если заморочиться
        }

        query.append(columnNames);
        query.append("\n");
        query.append("WHERE id = ");
        query.append(id);

        return query.toString();
    }

    private String getTableName(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("Class is not ORM entity");
        }
        String tableName = tableAnnotation.name().isEmpty() ? clazz.getSimpleName() : tableAnnotation.name();
        return tableName;
    }

    private StringJoiner addColumnNames(Class<?> clazz) {
        StringJoiner columnNames = new StringJoiner(", ");
        for (Field declaredField : clazz.getDeclaredFields()) {
            Column columnAnnotation = declaredField.getAnnotation(Column.class);
            if (columnAnnotation != null) {
                String columnName = columnAnnotation.name().isEmpty() ? declaredField.getName() : columnAnnotation.name();
                columnNames.add(columnName);
            }
        }
        return columnNames;
    }
}
