package com.podzirei;

import java.io.Serializable;

public interface QueryGenerator {

    String findAll(Class<?> clazz);

    String findById(Class<?> type, Serializable id);

    String deleteById(Class<?> type, Serializable id);

    String insert(Object value) throws IllegalAccessException, NoSuchFieldException;

    String update(Object value) throws IllegalAccessException;

}
