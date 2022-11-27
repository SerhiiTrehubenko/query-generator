package com.tsa.orm.interfaces;

import java.io.Serializable;

public interface QueryGenerator {
    String findAll (Class<?> type);
    String findById (Class<?> type, Serializable id);
    String deleteById (Class<?> type, Serializable id);

    String insert(Object object);

    String update(Object object);
}
