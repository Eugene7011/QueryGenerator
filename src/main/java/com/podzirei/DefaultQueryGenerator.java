package com.podzirei;

import com.podzirei.annotation.Column;
import com.podzirei.annotation.Id;
import com.podzirei.annotation.Table;

import java.lang.reflect.Field;
import java.util.StringJoiner;

public class DefaultQueryGenerator implements QueryGenerator {

    @Override
    //SELECT id, person_name, person_salary FROM Person;
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
    //"SELECT id, person_name, person_salary FROM Person WHERE id = "id";
    public String findByArgument(Class<?> type, Object id) {
        String tableName = getTableName(type);

        StringBuilder query = new StringBuilder("SELECT ");
        StringJoiner columnNames = addColumnNames(type);
        String argumentColumnName = getIdColumnName(type);

        query.append(columnNames);
        query.append(" FROM ");
        query.append(tableName);
        query.append(" WHERE ");
        query.append(argumentColumnName);
        query.append(" = ");
        query.append(id);
        query.append(";");

        return query.toString();
    }

    @Override
    //DELETE id, person_name, person_salary FROM Person WHERE id = "id";
    public String deleteById(Class<?> type, Object value) {
        String tableName = getTableName(type);

        StringBuilder query = new StringBuilder("DELETE ");
        StringJoiner fields = addColumnNames(type);
        String argumentColumnName = getIdColumnName(type);

        query.append(fields);
        query.append(" FROM ");
        query.append(tableName);
        query.append(" WHERE ");
        query.append(argumentColumnName);
        query.append(" = ");
        if (value instanceof String) {
            query.append("'").append(value).append("'");
        }
        query.append(value);
        query.append(";");

        return query.toString();
    }

    @Override
    //INSERT INTO Person (id, person_name, person_salary)
    // VALUES (3, 'Anton', 234.0);
    public String insert(Object object) {
        Class<?> type = object.getClass();
        String tableName = getTableName(type);

        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(tableName);
        query.append(" (");

        StringJoiner columnNames = addColumnNames(type);
        query.append(columnNames);
        query.append(")");
        query.append(System.getProperty("line.separator"));
        query.append("VALUES (");

        StringJoiner columnValues = getColumnsValues(object);

        query.append(columnValues);
        query.append(")");
        query.append(";");

        return query.toString();
    }

    @Override
    //UPDATE Person
    // SET id = 3, person_name = 'Anton', person_salary = 1000.0
    // WHERE id = 3;
    public String update(Object object) {
        Class<?> type = object.getClass();
        String tableName = getTableName(type);
        String argumentColumnNameWithValue = getArgumentColumnNameWithValue(object);
        String columnsNamesWithValues = getColumnsNamesWithValues(object);

        return "UPDATE " + tableName +
                System.getProperty("line.separator") +
                "SET " +
                columnsNamesWithValues +
                System.getProperty("line.separator") +
                "WHERE " +
                argumentColumnNameWithValue +
                ";";
    }

    private String getArgumentColumnNameWithValue(Object object) {
        StringJoiner columnsNamesAndValues = new StringJoiner(", ");
        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            String columnAnnotationName = field.getAnnotation(Column.class).name();
            if (columnAnnotationName != null && (field.getAnnotation(Id.class) != null)) {
                String columnName = columnAnnotationName.isEmpty() ? field.getName() : columnAnnotationName;
                columnsNamesAndValues.add(columnName + " = " + getColumnValue(field, object));
            }
        }
        return columnsNamesAndValues.toString();
    }

    private String getColumnsNamesWithValues(Object object) {
        StringJoiner columnsNamesAndValues = new StringJoiner(", ");
        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            String columnAnnotationName = field.getAnnotation(Column.class).name();
            if (columnAnnotationName != null && (field.getAnnotation(Id.class) == null)) {
                String columnName = columnAnnotationName.isEmpty() ? field.getName() : columnAnnotationName;
                columnsNamesAndValues.add(columnName + " = " + getColumnValue(field, object));
            }
        }
        return columnsNamesAndValues.toString();
    }

    private String getColumnValue(Field field, Object object) {
        field.setAccessible(true);
        try {
            Object columnValue = field.get(object);
            if (columnValue == null) {
                return null;
            }
            if (field.getType().isAssignableFrom(CharSequence.class)) {
                return "'" + columnValue + "'";
            }
            return "" + columnValue + "";
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Invalid data. Entered value is not an instance of the class declared to the field", e);
        }
    }

    private String getIdColumnName(Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            if (field.getAnnotation(Id.class) != null && (field.getAnnotation(Column.class) != null)) {
                String columnNameAnnotation = field.getAnnotation(Column.class).name();
                return columnNameAnnotation.isEmpty() ? field.getName() : columnNameAnnotation;
            }
        }
        throw new RuntimeException("Annotation of column is not exist");
    }

    private StringJoiner getColumnsValues(Object object) {
        StringJoiner columnsValues = new StringJoiner(", ");
        Class<?> clazz = object.getClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            if (field.getAnnotation(Column.class) != null) {
                columnsValues.add(getColumnValue(field, object));
            }
        }
        return columnsValues;
    }

    private String getTableName(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("Class is not ORM entity");
        }
        return tableAnnotation.name().isEmpty() ? clazz.getSimpleName() : tableAnnotation.name();
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
