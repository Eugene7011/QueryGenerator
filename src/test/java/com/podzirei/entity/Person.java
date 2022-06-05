package com.podzirei.entity;

import com.podzirei.annotation.Column;
import com.podzirei.annotation.Id;
import com.podzirei.annotation.Table;

@Table
public class Person {
    @Id
    @Column
    private int id;

    @Column(name = "person_name")
    private String name;

    @Column(name = "person_salary")
    private double salary;

    public Person(int id, String name, double salary) {
        this.id = id;
        this.name = name;
        this.salary = salary;
    }
}
