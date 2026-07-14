package com.kyriakos.compose.project.demo.zalexhumanresources.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public record EmployeeCertificationDTO (

        @JsonProperty("address_to")
        String addressTo,
        String purpose,

        @JsonProperty("issued_on")
        @JsonFormat(pattern = "d/M/yyyy")
        Date issuedOn,
        @JsonProperty("reference_no")
        Long referenceNo,
        String status
){


}
