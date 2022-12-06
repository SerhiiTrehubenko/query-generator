package com.tsa.orm.entity;

import com.tsa.orm.annotation.Id;

public class SubMoreThanOneId extends MoreThanOneId {
    @Id
    String name;
}
