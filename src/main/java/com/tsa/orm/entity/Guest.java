package com.tsa.orm.entity;

import com.tsa.orm.annotation.Column;
import com.tsa.orm.annotation.Entity;
import com.tsa.orm.annotation.Table;

@Entity
@Table(name = "Guest_Table")
public class Guest {
    @Column(name = "guest_id")
    private Long id;
    @Column(name = "guest_name")
    private String name;
    @Column(name = "guest_password")
    private String password;

    @Column(name = "guest_salary")
    private double salary;

    public Guest(Long id, String name, String password, double salary) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.salary = salary;
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
