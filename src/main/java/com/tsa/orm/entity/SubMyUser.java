package com.tsa.orm.entity;

import com.tsa.orm.annotation.Entity;

@Entity
public class SubMyUser extends MyUser {

    private double salary;

    public SubMyUser(Long id, String name, String password, double salary) {
        super(id, name, password);
        this.salary = salary;
    }
}
