package com.kyriakos.compose.project.demo.zalexhumanresources.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record EmployeeCertificationDTO (

        @JsonProperty("address_to")
        String addressTo,
        String purpose,

        @JsonProperty("issued_on")
        @JsonFormat(pattern = "d/M/yyyy")
        LocalDate issuedOn,
        @JsonProperty("reference_no")
        Long referenceNo,
        String status,
        @JsonProperty("employee_id")
        Long employeeId
){


}
