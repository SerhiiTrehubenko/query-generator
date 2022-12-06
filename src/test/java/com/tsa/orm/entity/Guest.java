package com.tsa.orm.entity;

import com.tsa.orm.annotation.Column;
import com.tsa.orm.annotation.Entity;
import com.tsa.orm.annotation.Id;
import com.tsa.orm.annotation.Table;

@Entity
@Table(name = "Guest_Table")
public class Guest {
    @Id(name = "guest_id")
    private final Long id;
    @Column(name = "guest_name")
    private final String name;
    @Column(name = "guest_password")
    private final String password;

    @Column(name = "guest_salary")
    private final double salary;

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
