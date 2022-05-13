package com.podzirei;

import com.podzirei.annotation.Column;
import com.podzirei.annotation.Table;

import java.lang.reflect.Field;
import java.util.StringJoiner;

public class DefaultQueryGenerator implements QueryGenerator {

    @Override
    //SELECT id, person_name, person_salary FROM Person
    public String findAll(Class<?> clazz) {
        String tableName = getTableName(clazz);

        StringBuilder query = new StringBuilder("SELECT ");
        StringJoiner fields = addFields(clazz);
        query.append(fields);
        query.append(" FROM ");
        query.append(tableName);
        query.append(";");

        return query.toString();
    }

    @Override
    //"SELECT id, person_name, person_salary FROM Person WHERE id = '" + id + "'"
    public String findByArgument(Class<?> type, Object object) {
        String tableName = getTableName(type);

        StringBuilder query = new StringBuilder("SELECT ");
        StringJoiner fields = addFields(type);
        String[] fieldNames = fields.toString().split(", ");

        query.append(fields);
        query.append(" FROM ");
        query.append(tableName);
        query.append(" WHERE ");
        query.append("id = '");
        if (object instanceof String) {
            query.append("object");
        }
        query.append(object);
        query.append("'");

        return query.toString();
    }

    @Override
    //DELETE id, person_name, person_salary FROM Person WHERE id = '" + id + "'
    public String deleteById(Class<?> type, Object id) {
        String tableName = getTableName(type);

        StringBuilder query = new StringBuilder("DELETE ");
        StringJoiner fields = addFields(type);
        query.append(fields);
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

        StringJoiner columnNames = addFields(type);
        query.append(columnNames);
        query.append(")");
        query.append(System.getProperty("line.separator"));
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
        query.append(System.getProperty("line.separator"));
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
            if (fieldValue == null) {
                columnNames.add(columnName + " = " + null);
                generateQuery(query, columnNames, id);
                return query.toString();
            }

            columnNames.add(columnName + " = " + fieldValue.toString());
        }

        generateQuery(query, columnNames, id);

        return query.toString();
    }

    private void generateQuery(StringBuilder query, StringJoiner columnNames, int id) {
        query.append(columnNames);
        query.append(System.getProperty("line.separator"));
        query.append("WHERE id = ");
        query.append(id);
    }

    private String getTableName(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("Class is not ORM entity");
        }
        String tableName = tableAnnotation.name().isEmpty() ? clazz.getSimpleName() : tableAnnotation.name();
        return tableName;
    }

    private StringJoiner addFields(Class<?> clazz) {
        StringJoiner fields = new StringJoiner(", ");
        for (Field declaredField : clazz.getDeclaredFields()) {
            Column columnAnnotation = declaredField.getAnnotation(Column.class);
            if (columnAnnotation != null) {
                String columnName = columnAnnotation.name().isEmpty() ? declaredField.getName() : columnAnnotation.name();
                fields.add(columnName);
            }
        }
        return fields;
    }
}
