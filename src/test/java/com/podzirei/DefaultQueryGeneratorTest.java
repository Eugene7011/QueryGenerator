package com.podzirei;

import com.podzirei.entity.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultQueryGeneratorTest {
    @Test
    public void testGenerateSelectAll() {
        String expectedQuery = "SELECT id, person_name, person_salary FROM Person;";
        DefaultQueryGenerator queryGenerator = new DefaultQueryGenerator();
        String selectAll = queryGenerator.findAll(Person.class);
        assertEquals(expectedQuery, selectAll);
    }

    @DisplayName("test Generate Find By Id")
    @Test
    public void testGenerateFindById() {
        String expectedQuery = "SELECT id, person_name, person_salary FROM Person WHERE id = 213;";
        DefaultQueryGenerator queryGenerator = new DefaultQueryGenerator();
        String findById = queryGenerator.findByArgument(Person.class, 213);
        assertEquals(expectedQuery, findById);
    }

    @DisplayName("test Generate Delete By Id")
    @Test
    public void testGenerateDeleteById() {
        String expectedQuery = "DELETE id, person_name, person_salary FROM Person WHERE id = 213;";
        DefaultQueryGenerator queryGenerator = new DefaultQueryGenerator();
        String deleteById = queryGenerator.deleteById(Person.class, 213);
        assertEquals(expectedQuery, deleteById);
    }

    @DisplayName("test Generate Insert")
    @Test
    public void testGenerateInsert() {
        Person person = new Person(3, "Anton", 234);

        String expectedQuery = "INSERT INTO Person (id, person_name, person_salary)"
                + System.getProperty("line.separator") + "VALUES (3, 'Anton', 234.0);";
        DefaultQueryGenerator queryGenerator = new DefaultQueryGenerator();
        String deleteById = queryGenerator.insert(person);
        assertEquals(expectedQuery, deleteById);
    }

    @DisplayName("test Generate Update")
    @Test
    public void testGenerateUpdate() {
        Person person = new Person(3, "Anton", 1000);

        String expectedQuery = "UPDATE Person" + System.getProperty("line.separator") + "SET person_name = 'Anton', person_salary = 1000.0"
                + System.getProperty("line.separator") + "WHERE id = 3;";
        DefaultQueryGenerator queryGenerator = new DefaultQueryGenerator();
        String deleteById = queryGenerator.update(person);
        assertEquals(expectedQuery, deleteById);
    }
}
