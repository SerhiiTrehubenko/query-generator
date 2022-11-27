package com.tsa.orm.services;

import com.tsa.database.services.ResultViewerImpl;
import com.tsa.database.util.DbConnector;
import com.tsa.database.util.PropertyParser;
import com.tsa.database.util.QueryProcessor;
import com.tsa.orm.entity.Guest;
import com.tsa.orm.entity.MyUser;
import com.tsa.orm.entity.SubGuest;
import com.tsa.orm.entity.SubMyUser;
import com.tsa.orm.interfaces.QueryGenerator;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;
import java.util.Map;


public class DatabaseTest {

    QueryGenerator queryGenerator = new DefaultQueryGenerator();

    @Test
    void insertToDb() {
        MyUser myUser = createUser();
        QueryProcessor.insert(getConnection(),
                queryGenerator.insert(myUser));
    }

    @Test
    void findAll() {
        List<Map<String, Object>> list = QueryProcessor.select(getConnection(),
                queryGenerator.findAll(MyUser.class));
        var viewer = new ResultViewerImpl(list);
        viewer.view();
    }

    @Test
    void deleteById() {
        QueryProcessor.insert(getConnection(),
                queryGenerator.deleteById(MyUser.class, 1));
    }

    @Test
    void findById() {
        List<Map<String, Object>> list = QueryProcessor.select(getConnection(),
                queryGenerator.findById(MyUser.class, 2));
        var viewer = new ResultViewerImpl(list);
        viewer.view();
    }

    @Test
    void update() {
        MyUser myUser = createUser();
        QueryProcessor.insert(getConnection(),
                queryGenerator.update(myUser));
    }

    private Connection getConnection() {
        return DbConnector.getConnection(PropertyParser.getProperties());
    }

    private MyUser createUser() {
        return new MyUser(5L, "Green", "World");
    }

    private Guest createGuest() {
        return new Guest(5L, "Hello", "null", 544.88);
    }

    private SubMyUser createSubUser() {
        return new SubMyUser(25L, "Mike", "126587", 1_555.45);
    }

    private SubGuest createSubGuest() {
        return new SubGuest(50L, "Oleg", "password", 544.88, "Dnipro");
    }
}
