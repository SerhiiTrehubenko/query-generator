package com.tsa.orm.entity;

import com.tsa.orm.annotation.Entity;
import com.tsa.orm.annotation.Id;

@Entity
public class Employee {
    @Id
    String professionalRate;

    String name;

    public Employee(String professionalRate, String name) {
        this.professionalRate = professionalRate;
        this.name = name;
    }
}
