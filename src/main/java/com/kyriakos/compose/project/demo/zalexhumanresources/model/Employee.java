package com.kyriakos.compose.project.demo.zalexhumanresources.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false)
    private Date dateJoined;
    @Column(nullable = false)
    private Date dateOfBirth;
    @Column(nullable = false)
    private String position;

    @Embedded
    private Address address;

    public Long getEmployeeId() {
        return employeeId;
    }


}
