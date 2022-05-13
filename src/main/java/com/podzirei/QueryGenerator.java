package com.podzirei;

import java.io.Serializable;

public interface QueryGenerator {

    String findAll(Class<?> clazz);

    String findByArgument(Class<?> type, Object id);

    String deleteById(Class<?> type, Object id);

    String insert(Object value) throws IllegalAccessException, NoSuchFieldException;

    String update(Object value) throws IllegalAccessException;

}
