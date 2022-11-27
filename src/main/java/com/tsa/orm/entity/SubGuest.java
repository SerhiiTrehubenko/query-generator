package com.tsa.orm.entity;

import com.tsa.orm.annotation.Column;
import com.tsa.orm.annotation.Entity;

@Entity
public class SubGuest extends Guest {
    @Column(name = "subguest_address")
    String address;


    public SubGuest(Long id, String name, String password, double salary, String address) {
        super(id, name, password, salary);
        this.address = address;
    }
}
