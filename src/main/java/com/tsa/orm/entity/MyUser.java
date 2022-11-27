package com.tsa.orm.entity;

import com.tsa.orm.annotation.Entity;

@Entity
public class MyUser {

    private Long id;

    private String name;

    private String password;

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
