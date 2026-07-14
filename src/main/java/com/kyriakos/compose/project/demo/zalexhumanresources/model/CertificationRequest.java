package com.kyriakos.compose.project.demo.zalexhumanresources.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
/*
    Adding employee Id index as for all the queries in the certification_requests we search with employee id,
    this will make our search more efficient.
    In addition adding composite index for employee id and issued On, and employee id and status, as it will make
    our sorting faster and more efficient
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "certification_requests", indexes = {
        @Index(name = "idx_employee_id", columnList = "employee_id"),
        @Index(name = "idx_employee_id_issued_on", columnList = "employee_id, issued_on"),
        @Index(name = "idx_employee_id_status", columnList = "employee_id, status")
})
public class CertificationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long referenceNo ;

    @JsonProperty("address_to")
    @Column(nullable = false)
    private String addressTo;
    @Column(nullable = false)
    private String purpose;
    @JsonProperty("issued_on")
    @JsonFormat(pattern = "d/M/yyyy")
    @Column(nullable = false)
    private Date issuedOn;
    @JsonProperty("employee_id")
    @Column(nullable = false)
    private Long employeeId;
    @Column(nullable = false)
    private Status status;
}
