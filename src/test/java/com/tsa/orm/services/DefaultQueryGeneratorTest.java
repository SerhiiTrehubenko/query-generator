package com.tsa.orm.services;

import com.tsa.orm.entity.*;
import com.tsa.orm.interfaces.QueryGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;

class DefaultQueryGeneratorTest {

    public static QueryGenerator queryGenerator;

    @BeforeAll
    public static void initialization() {
        queryGenerator = new DefaultQueryGenerator();
    }

    @DisplayName("Test findAll(), @Table and @Column are not present. @Entity is present")
    @Test
    void testFindAllAnnotationsTableAndColumnAreNotPresent() {
        String query = "SELECT id, name, password FROM myuser;";
        assertEquals(query, queryGenerator.findAll(MyUser.class));
    }

    @DisplayName("Test findAll(), IllegalArgumentException is thrown When @Entity is not present")
    @Test
    void testFindAllThrowsExceptionWhenClassDoesNotHaveEntityAnnotation() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.findAll(String.class),
                "not an Entity");
    }

    @DisplayName("Test findAll(), NullPointerException is thrown When Parameter is Null")
    @Test
    void testFindAllThrowsExceptionWhenClassEqualsNull() {
        assertThrows(NullPointerException.class, () -> queryGenerator.findAll(null),
                "is mandatory");
    }

    @DisplayName("Test findAll(), When @Column, @Table and @Entity are present")
    @Test
    void testFindAllWithProvidedColumnsAnnotationsHaveValues() {
        String query = "SELECT guest_id, guest_name, guest_password, guest_salary FROM guest_table;";
        assertEquals(query, queryGenerator.findAll(Guest.class));
    }

    @DisplayName("Test findById(), NullPointerException is thrown When One Of Arguments Is Null")
    @Test
    void testFindByIdThrowsExceptionWhenOneOfArgumentsIsNull() {
        assertThrows(NullPointerException.class, () -> queryGenerator.findById(null, 10));
        assertThrows(NullPointerException.class, () -> queryGenerator.findById(Guest.class, null));
    }

    @DisplayName("Test findById(), IllegalArgumentException is thrown when @Entity is not present")
    @Test
    void testFindByIdThrowsExceptionWhenClassDoesNotHaveEntityAnnotation() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.findById(String.class, 10),
                "not an Entity");
    }

    @DisplayName("Test findById(), IllegalArgumentException is thrown when parameter(Id) Is Not String, Long or Integer")
    @Test
    void testFindByIdThrowsExceptionWhenIdIsNotStringLongInteger() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.findById(Guest.class, 25.00),
                "Entity id should be String, Integer or Long");
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.findById(Guest.class, 'l'),
                "Entity id should be String, Integer or Long");
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.findById(Guest.class, false),
                "Entity id should be String, Integer or Long");
    }

    @DisplayName("Test findById(), when @Columns and @Table are not present. @Entity is present")
    @Test
    void testFindByIdClassWithoutAnnotationsOnFieldsAndTypeLevels() {
        String query = "SELECT id, name, password FROM myuser WHERE id = 5;";
        assertEquals(query, queryGenerator.findById(MyUser.class, "5"));

    }

    @DisplayName("Test findById(), when @Columns, @Table and @Entity are present.")
    @Test
    void testFindByIdClassHasAnnotationsOnFieldsAndTypeLevels() {
        String query = "SELECT guest_id, guest_name, guest_password, guest_salary FROM guest_table WHERE guest_id = 5;";
        assertEquals(query, queryGenerator.findById(Guest.class, "5"));

    }

    @DisplayName("Test deleteById(), when @Columns and @Table are not present. @Entity is present")
    @Test
    void testDeleteByIdColumnTableAnnotationsAreAbsent() {
        String query = "DELETE FROM myuser WHERE id = 5;";
        String query2 = queryGenerator.deleteById(MyUser.class, "5");
        assertEquals(query, query2);
    }

    @DisplayName("Test deleteById(), when @Columns, @Table and @Entity are present.")
    @Test
    void testDeleteByIdColumnTableAnnotationsArePresent() {
        String query = "DELETE FROM guest_table WHERE guest_id = 5;";
        assertEquals(query, queryGenerator.deleteById(Guest.class, "5"));
    }

    @DisplayName("Test deleteById(), NullPointerException is thrown When One Of Arguments Is Null")
    @Test
    void testDeleteByIdThrowsExceptionWhenOneOfArgumentsIsNull() {
        assertThrows(NullPointerException.class, () -> queryGenerator.deleteById(null, 10));
        assertThrows(NullPointerException.class, () -> queryGenerator.deleteById(Guest.class, null));
    }

    @DisplayName("Test deleteById(), IllegalArgumentException is thrown when @Entity is not present")
    @Test
    void testDeleteByIdThrowsExceptionWhenClassDoesNotHaveEntityAnnotation() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.deleteById(String.class, 10),
                "not an Entity");
    }

    @DisplayName("Test deleteById(), IllegalArgumentException is thrown when parameter(Id) Is Not String, Long or Integer")
    @Test
    void testDeleteByIdThrowsExceptionWhenIdIsNotStringLongInteger() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.deleteById(Guest.class, 25.00),
                "Entity id should be String, Integer or Long");
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.deleteById(Guest.class, 'i'),
                "Entity id should be String, Integer or Long");
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.deleteById(Guest.class, false),
                "Entity id should be String, Integer or Long");
    }

    @DisplayName("Test insert(), NullPointerException is thrown when parameter is null")
    @Test
    void testInsertThrowsExceptionWhenParameterNull() {
        assertThrows(NullPointerException.class, () -> queryGenerator.insert(null));
    }

    @DisplayName("Test insert(), IllegalArgumentException is thrown when Object does not have @Entity")
    @Test
    void testInsertThrowsExceptionWhenIsNotEntity() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.insert(""), " not an Entity");
    }

    @DisplayName("Test insert(), when @Columns and @Table are not present. @Entity is present")
    @Test
    void testInsertColumnTableAnnotationsAreNotPresent() {
        String query = "INSERT INTO myuser (password, name) VALUES ('126587', 'Mike');";
        MyUser myUser = createUser();
        String resultQuery = queryGenerator.insert(myUser);
        assertEquals(query, resultQuery);
    }

    @DisplayName("Test insert(), when @Columns, @Table and @Entity are present.")
    @Test
    void testInsertColumnTableEntityAnnotationsArePresent() {
        String query = "INSERT INTO guest_table (guest_name, guest_password, guest_salary) VALUES ('Hello', 'null', 544.88);";
        Guest guest = createGuest();
        assertEquals(query, queryGenerator.insert(guest));
    }

    @DisplayName("Test update(), NullPointerException is thrown when parameter is null")
    @Test
    void testUpdateThrowsExceptionWhenParameterNull() {
        assertThrows(NullPointerException.class, () -> queryGenerator.update(null));
    }

    @DisplayName("Test update(), IllegalArgumentException is thrown when Object does not have @Entity")
    @Test
    void testUpdateThrowsExceptionWhenIsNotEntity() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.update(""), " not an Entity");
    }

    @DisplayName("Test update(), when @Columns and @Table are not present. @Entity is present")
    @Test
    void testUpdateColumnTableAnnotationsAreNotPresent() {
        String query1 = "UPDATE myuser SET password='126587', name='Mike' WHERE id=5;";
        MyUser myUser = createUser();
        assertEquals(query1, queryGenerator.update(myUser));

    }

    @DisplayName("Test update(), when @Columns, @Table and @Entity are present.")
    @Test
    void testUpdateColumnTableEntityAnnotationsArePresent() {
        String query2 = "UPDATE guest_table SET guest_name='Hello', guest_password='null', guest_salary=544.88 WHERE guest_id=5;";
        Guest guest = createGuest();
        assertEquals(query2, queryGenerator.update(guest));
    }

    @DisplayName("Tests Hierarchy when @Columns and @Table are not present. @Entity is present")
    @Test
    void testHierarchyClassColumnAndTableAnnotationsAreAbsent() {
        String queryFindAll = "SELECT id, name, password, salary FROM submyuser;";
        String queryFindById = "SELECT id, name, password, salary FROM submyuser WHERE id = 10;";
        String queryDeleteById = "DELETE FROM submyuser WHERE id = 10;";
        String queryInsert = "INSERT INTO submyuser (password, name, salary) VALUES ('126587', 'Mike', 1555.45);";
        String queryUpdate = "UPDATE submyuser SET password='126587', name='Mike', salary=1555.45 WHERE id=25;";

        SubMyUser subUser = createSubUser();

        String resultFindAll = queryGenerator.findAll(SubMyUser.class);
        String resultFindById = queryGenerator.findById(SubMyUser.class, 10);
        String resultDeleteById = queryGenerator.deleteById(SubMyUser.class, 10);
        String resultInsert = queryGenerator.insert(subUser);
        String resultUpdate = queryGenerator.update(subUser);

        assertEquals(queryFindAll, resultFindAll);
        assertEquals(queryFindById, resultFindById);
        assertEquals(queryDeleteById, resultDeleteById);
        assertEquals(queryInsert, resultInsert);
        assertEquals(queryUpdate, resultUpdate);

    }

    @DisplayName("Tests Hierarchy when @Columns, @Table and @Entity are present.")
    @Test
    void testHierarchyClassColumnAndTableAnnotationsArePresent() {
        String queryFindAll = "SELECT guest_id, guest_name, guest_password, guest_salary, subguest_address FROM subguest;";
        String queryFindById = "SELECT guest_id, guest_name, guest_password, guest_salary, subguest_address FROM subguest WHERE guest_id = 60;";
        String queryDeleteById = "DELETE FROM subguest WHERE guest_id = 60;";
        String queryInsert = "INSERT INTO subguest (subguest_address, guest_name, guest_password, guest_salary) VALUES ('Dnipro', 'Oleg', 'password', 544.88);";
        String queryUpdate = "UPDATE subguest SET subguest_address='Dnipro', guest_name='Oleg', guest_password='password', guest_salary=544.88 WHERE guest_id=50;";

        SubGuest subGuest = createSubGuest();

        String resultFindAll = queryGenerator.findAll(SubGuest.class);
        String resultFindById = queryGenerator.findById(SubGuest.class, 60);
        String resultDeleteById = queryGenerator.deleteById(SubGuest.class, 60);
        String resultInsert = queryGenerator.insert(subGuest);
        String resultUpdate = queryGenerator.update(subGuest);

        assertEquals(queryFindAll, resultFindAll);
        assertEquals(queryFindById, resultFindById);
        assertEquals(queryDeleteById, resultDeleteById);
        assertEquals(queryInsert, resultInsert);
        assertEquals(queryUpdate, resultUpdate);

    }

    @DisplayName("Test getTableName(), NullPointerException is thrown when parameter is NULL")
    @Test
    void testGetTableNameParameterIsNull() {
        assertThrows(NullPointerException.class,
                () -> ((DefaultQueryGenerator) queryGenerator).getTableName(null));

    }

    @DisplayName("Test getTableName(), IllegalArgumentException is thrown when parameter Object does not have @Entity")
    @Test
    void testGetTableNameParameterIsObjectWhichIsNotEntity() {
        Object object = new Object();
        assertThrows(IllegalArgumentException.class,
                () -> ((DefaultQueryGenerator) queryGenerator).getTableName(object));
    }

    @DisplayName("Test getTableName(), IllegalArgumentException is thrown when parameter Class does not have @Entity")
    @Test
    void testGetTableNameParameterIsClassWhichIsNotEntity() {
        assertThrows(IllegalArgumentException.class,
                () -> ((DefaultQueryGenerator) queryGenerator).getTableName(String.class));
    }

    @DisplayName("Test getTableName(), @Table is not present and Parameter is Class.class")
    @Test
    void testGetTableNameParameterIsClass() {
        String expected = "myuser";
        assertEquals(expected, ((DefaultQueryGenerator) queryGenerator).getTableName(MyUser.class));
    }

    @DisplayName("Test getTableName(), @Table is not present and Parameter is Object.class")
    @Test
    void testGetTableNameParameterIsObject() {
        String expected = "myuser";
        MyUser myUser = createUser();
        assertEquals(expected, ((DefaultQueryGenerator) queryGenerator).getTableName(myUser));
    }

    @DisplayName("Test getTableName(), Parameter is Class.class and @Table is present ")
    @Test
    void testGetTableNameParameterIsClassWithTableAnnotation() {
        String expected = "guest_table";
        assertEquals(expected, ((DefaultQueryGenerator) queryGenerator).getTableName(Guest.class));
    }

    @DisplayName("Test getTableName(), Parameter is Object.class and @Table is present")
    @Test
    void testGetTableNameParameterIsObjectWithTableAnnotation() {
        String expected = "guest_table";
        Guest guest = createGuest();
        assertEquals(expected, ((DefaultQueryGenerator) queryGenerator).getTableName(guest));
    }

    @DisplayName("Test getColumnNameConsideringAnnotation(), @Column is not present")
    @Test
    void testGetColumnNameConsideringAnnotationWithoutAnnotation() {
        String expected = "password";
        try {
            Field fieldNoAnnotation = MyUser.class.getDeclaredField("password");
            assertEquals(expected,
                    ((DefaultQueryGenerator) queryGenerator).getColumnNameConsideringAnnotation(fieldNoAnnotation));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @DisplayName("Test getColumnNameConsideringAnnotation(), @Column is present")
    @Test
    void testGetColumnNameConsideringAnnotationAnnotationIsPresent() {
        String expected = "guest_password";
        try {
            Field fieldNoAnnotation = Guest.class.getDeclaredField("password");
            assertEquals(expected,
                    ((DefaultQueryGenerator) queryGenerator).getColumnNameConsideringAnnotation(fieldNoAnnotation));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @DisplayName("Test getSuperClassesFromHierarchy()")
    @Test
    void testGetSuperClassesFromHierarchy() {
        String expected = "[class com.tsa.orm.entity.Guest, class com.tsa.orm.entity.SubGuest]";
        Deque<Class<?>> listOfSuperClasses = new ArrayDeque<>();
        ((DefaultQueryGenerator) queryGenerator).enrichWithSuperClassesFromHierarchy(SubGuest.class, listOfSuperClasses);
        assertEquals(expected, listOfSuperClasses.toString());
    }

    @DisplayName("Test parseId(), checks parameter to be String (which contains a decimal), Integer or Long")
    @Test
    void testParseId() {
        String expected = "10";

        assertEquals(expected, ((DefaultQueryGenerator) queryGenerator).parseId("10"));
        assertEquals(expected, ((DefaultQueryGenerator) queryGenerator).parseId(10));
        assertEquals(expected, ((DefaultQueryGenerator) queryGenerator).parseId(10L));
    }

    @DisplayName("Test parseId(), IllegalArgumentException is thrown" +
            " when parameter is not String(which does not contain a decimal), Integer or Long")
    @Test
    void testParseIdThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> ((DefaultQueryGenerator) queryGenerator).parseId(10.55));
        assertThrows(IllegalArgumentException.class,
                () -> ((DefaultQueryGenerator) queryGenerator).parseId('a'));
        assertThrows(IllegalArgumentException.class,
                () -> ((DefaultQueryGenerator) queryGenerator).parseId(55.55F));
    }

    @DisplayName("Test parseId(), NullPointerException is thrown" +
            " when parameter is not NULL")
    @Test
    void testParseIdThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> ((DefaultQueryGenerator) queryGenerator).parseId(null));
    }

    @DisplayName("Test getListOfColumnsFromOneClassConsideringAnnotation(), return a " +
            "List of Column names, @Column is not present")
    @Test
    void testGetListOfColumnsFromOneClassConsideringAnnotationColumnAnnotationIsNotPresent() {
        String expected = "[id, name, password]";
        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getListOfColumnsFromOneClass(MyUser.class).toString());
    }

    @DisplayName("Test getListOfColumnsFromOneClassConsideringAnnotation(), return a " +
            "List of Column names, @Column is present")
    @Test
    void testGetListOfColumnsFromOneClassConsideringAnnotationColumnAnnotationIsPresent() {
        String expected = "[guest_id, guest_name, guest_password, guest_salary]";
        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getListOfColumnsFromOneClass(Guest.class).toString());
    }

    @DisplayName("Test getColumns(), @Column is not present in Class")
    @Test
    void testGetColumnsColumnAnnotationIsNotPresent() {
        String expected = "id, name, password";
        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getColumns(MyUser.class));

    }

    @DisplayName("Test getColumns(), @Column is not present in SubClass")
    @Test
    void testGetColumnsColumnAnnotationIsNotPresentInSubClass() {
        String expected = "id, name, password, salary";
        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getColumns(SubMyUser.class));

    }

    @DisplayName("Test getColumns(), @Column is present in Class")
    @Test
    void testGetColumnsColumnAnnotationPresent() {
        String expected = "guest_id, guest_name, guest_password, guest_salary";
        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getColumns(Guest.class));

    }

    @DisplayName("Test getColumns(), @Column is present in SubClass")
    @Test
    void testGetColumnsColumnAnnotationIsPresentInSubClass() {
        String expected = "guest_id, guest_name, guest_password, guest_salary, subguest_address";
        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getColumns(SubGuest.class));

    }

    @DisplayName("Test getListOfColumnsFromHierarchyConsideringAnnotation(), @Column is not present in SuperClass")
    @Test
    void testGetListOfColumnsFromHierarchyConsideringAnnotationColumnAnnotationIsNotPresentInSuperClass() {
        String expected = "[id, name, password]";
        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getListOfColumnsFromHierarchy(MyUser.class).toString());
    }

    @DisplayName("Test getListOfColumnsFromHierarchyConsideringAnnotation(), @Column is not present in SubClass")
    @Test
    void testGetListOfColumnsFromHierarchyConsideringAnnotationColumnAnnotationIsNotPresentInSubClass() {
        String expected = "[id, name, password, salary]";
        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getListOfColumnsFromHierarchy(SubMyUser.class).toString());
    }

    @DisplayName("Test getListOfColumnsFromHierarchyConsideringAnnotation(), @Column is present in SuperClass")
    @Test
    void testGetListOfColumnsFromHierarchyConsideringAnnotationColumnAnnotationIsPresentInSuperClass() {
        String expected = "[guest_id, guest_name, guest_password, guest_salary]";
        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getListOfColumnsFromHierarchy(Guest.class).toString());
    }

    @DisplayName("Test getListOfColumnsFromHierarchyConsideringAnnotation(), @Column is not present in SubClass")
    @Test
    void testGetListOfColumnsFromHierarchyConsideringAnnotationColumnAnnotationIsPresentInSubClass() {
        String expected = "[guest_id, guest_name, guest_password, guest_salary, subguest_address]";
        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getListOfColumnsFromHierarchy(SubGuest.class).toString());
    }

    @DisplayName("Test getMapOfColumnsAndValues(), @Column is absent in SuperClass")
    @Test
    void testGetMapOfColumnsAndValuesColumnAnnotationIsAbsentInSuperClass() {
        String expected = "{password=126587, name=Mike, id=5}";

        MyUser myUser = createUser();

        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getMapOfColumnsAndValues(myUser).toString());

    }

    @DisplayName("Test getMapOfColumnsAndValues(), @Column is absent in SubClass")
    @Test
    void testGetMapOfColumnsAndValuesColumnAnnotationIsAbsentInSubClass() {
        String expected = "{password=126587, name=Mike, id=25, salary=1555.45}";

        SubMyUser subUser = createSubUser();

        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getMapOfColumnsAndValues(subUser).toString());
    }

    @DisplayName("Test getMapOfColumnsAndValues(), @Column is present in SuperClass")
    @Test
    void testGetMapOfColumnsAndValuesColumnAnnotationIsPresentInSuperClass() {
        String expected = "{guest_id=5, guest_name=Hello, guest_password=null, guest_salary=544.88}";

        Guest guest = createGuest();

        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getMapOfColumnsAndValues(guest).toString());

    }

    @DisplayName("Test getMapOfColumnsAndValues(), @Column is absent in SubClass")
    @Test
    void testGetMapOfColumnsAndValuesColumnAnnotationIsPresentInSubClass() {
        String expected = "{subguest_address=Dnipro, guest_id=50, guest_name=Oleg, guest_password=password, guest_salary=544.88}";

        var subGuest = createSubGuest();

        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getMapOfColumnsAndValues(subGuest).toString());
    }

    @DisplayName("Test getColumnsFromMap() from SuperClass")
    @Test
    void testGetColumnsFromMapFromSuperClass() {
        String expected = "guest_name, guest_password, guest_salary";

        var guest = createGuest();
        var map = ((DefaultQueryGenerator) queryGenerator).getMapOfColumnsAndValues(guest);

        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getColumnsFromMap(map));
    }

    @DisplayName("Test getColumnsFromMap() from SubClass")
    @Test
    void testGetColumnsFromMapFromSubClass() {
        String expected = "subguest_address, guest_name, guest_password, guest_salary";

        var subGuest = createSubGuest();
        var map = ((DefaultQueryGenerator) queryGenerator).getMapOfColumnsAndValues(subGuest);

        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getColumnsFromMap(map));
    }

    @DisplayName("Test getValuesFromMap() from SuperClass")
    @Test
    void testGetValuesFromMapFromSuperClass() {
        String expected = "'Hello', 'null', 544.88";

        var guest = createGuest();
        var map = ((DefaultQueryGenerator) queryGenerator).getMapOfColumnsAndValues(guest);

        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getValuesFromMap(map));
    }

    @DisplayName("Test getValuesFromMap() from SubClass")
    @Test
    void testGetValuesFromMapFromSubClass() {
        String expected = "'Dnipro', 'Oleg', 'password', 544.88";

        var subGuest = createSubGuest();
        var map = ((DefaultQueryGenerator) queryGenerator).getMapOfColumnsAndValues(subGuest);

        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).getValuesFromMap(map));
    }

    @DisplayName("Test createSetForUpdateQueryFromMap() from SuperClass")
    @Test
    void testCreateSetForUpdateQueryFromMapFromSuperClass() {
        String expected = "guest_name='Hello', guest_password='null', guest_salary=544.88";

        var guest = createGuest();
        var map = ((DefaultQueryGenerator) queryGenerator).getMapOfColumnsAndValues(guest);

        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).createSetForQueryFromMap(map));
    }

    @DisplayName("Test createSetForUpdateQueryFromMap() from SubClass")
    @Test
    void testCreateSetForUpdateQueryFromMapFromSubClass() {
        String expected = "subguest_address='Dnipro', guest_name='Oleg', guest_password='password', guest_salary=544.88";

        var subGuest = createSubGuest();
        var map = ((DefaultQueryGenerator) queryGenerator).getMapOfColumnsAndValues(subGuest);

        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).createSetForQueryFromMap(map));
    }

    @DisplayName("Test createConditionForUpdateQueryFromMap() from SuperClass")
    @Test
    void testCreateConditionForUpdateQueryFromMapFromSuperClass() {
        String expected = "guest_id=5";

        var guest = createGuest();
        var map = ((DefaultQueryGenerator) queryGenerator).getMapOfColumnsAndValues(guest);

        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).createConditionForQueryFromMap(map));
    }

    @DisplayName("Test createConditionForUpdateQueryFromMap() from SubClass")
    @Test
    void testCreateConditionForUpdateQueryFromMapFromSubClass() {
        String expected = "guest_id=50";

        var subGuest = createSubGuest();
        var map = ((DefaultQueryGenerator) queryGenerator).getMapOfColumnsAndValues(subGuest);

        assertEquals(expected,
                ((DefaultQueryGenerator) queryGenerator).createConditionForQueryFromMap(map));
    }

    @DisplayName("test findAll(), IllegalArgumentException when @Id is absent")
    @Test
    void testFindAllIdAbsent() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.findAll(SubIdNil.class));
    }

    @DisplayName("test findById(), IllegalArgumentException when @Id is absent")
    @Test
    void testFindByIdWhereIdAbsent() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.findById(SubIdNil.class, 5));
    }

    @DisplayName("test deleteById(), IllegalArgumentException when @Id is absent")
    @Test
    void testDeleteByIdWhereIdAbsent() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.deleteById(SubIdNil.class, 5));
    }

    @DisplayName("test insert(), IllegalArgumentException when @Id is absent")
    @Test
    void testInsertWhereIdAbsent() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.insert(new SubIdNil()));
    }

    @DisplayName("test update(), IllegalArgumentException when @Id is absent")
    @Test
    void testUpdateWhereIdAbsent() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.update(new SubIdNil()));
    }

    @DisplayName("test findAll(), IllegalArgumentException when @Id more then one")
    @Test
    void testFindAllIdMoreThenOne() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.findAll(SubMoreThanOneId.class));
    }

    @DisplayName("test findById(), IllegalArgumentException when @Id more then one")
    @Test
    void testFindByIdWhereIdMoreThenOne() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.findById(SubMoreThanOneId.class, 5));
    }

    @DisplayName("test deleteById(), IllegalArgumentException when @Id more then one")
    @Test
    void testDeleteByIdWhereIdMoreThenOne() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.deleteById(SubMoreThanOneId.class, 5));
    }

    @DisplayName("test insert(), IllegalArgumentException when @Id more then one")
    @Test
    void testInsertWhereIdMoreThenOne() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.insert(new SubMoreThanOneId()));
    }

    @DisplayName("test update(), IllegalArgumentException when @Id is absent")
    @Test
    void testUpdateWhereIdMoreThenOne() {
        assertThrows(IllegalArgumentException.class, () -> queryGenerator.update(new SubMoreThanOneId()));
    }

    @Test
    void testFindAllCustomId() {
        String query = "SELECT professionalRate, name, position FROM worker;";

        assertEquals(query, queryGenerator.findAll(Worker.class));
    }

    @Test
    void testFindByCustomId() {
        String query = "SELECT professionalRate, name, position FROM worker WHERE professionalRate = 1060;";

        assertEquals(query, queryGenerator.findById(Worker.class, "1060"));
    }

    @Test
    void testDeleteByCustomId() {
        String query = "DELETE FROM worker WHERE professionalRate = 1060;";

        assertEquals(query, queryGenerator.deleteById(Worker.class, "1060"));
    }

    @Test
    void testInsertByCustomId() {
        String query = "INSERT INTO worker (name, position) VALUES ('Mark', 'manager');";

        assertEquals(query, queryGenerator.insert(createWorker()));
    }

    @Test
    void testUpdateByCustomId() {
        String query = "UPDATE worker SET name='Mark', position='manager' WHERE professionalRate=1060;";

        assertEquals(query, queryGenerator.update(createWorker()));
    }

    private MyUser createUser() {
        return new MyUser(5L, "Mike", "126587");
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

    private Worker createWorker() {
        return new Worker("1060", "Mark", "manager");
    }
}