package com.tsa.orm.entity;

import com.tsa.orm.annotation.Entity;
import com.tsa.orm.annotation.Id;

@Entity
public class MyUser {
    @Id
    private final Long id;

    private final String name;

    private final String password;

    public MyUser(Long id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    @Override
    public String toString() {
        return "MyUser{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
