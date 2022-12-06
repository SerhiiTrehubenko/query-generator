package com.tsa.orm.entity;

import com.tsa.orm.annotation.Entity;

@Entity
public class Worker extends Employee {

    String position;

    public Worker(String hiringDate, String name, String position) {
        super(hiringDate, name);
        this.position = position;
    }
}
