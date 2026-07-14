package com.kyriakos.compose.project.demo.zalexhumanresources.controller;

import com.kyriakos.compose.project.demo.zalexhumanresources.dto.EmployeeCertificationDTO;
import com.kyriakos.compose.project.demo.zalexhumanresources.model.CertificationRequest;
import com.kyriakos.compose.project.demo.zalexhumanresources.service.CertificationRequestService;
import com.kyriakos.compose.project.demo.zalexhumanresources.sorting.SortDirection;
import com.kyriakos.compose.project.demo.zalexhumanresources.sorting.SortField;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class CertificationRequestController {

    private final CertificationRequestService certificationRequestService;

    public CertificationRequestController(CertificationRequestService certificationRequestService) {
        this.certificationRequestService = certificationRequestService;
    }

    @PostMapping("/certification-requests")
    public EmployeeCertificationDTO createCertificationRequest(@RequestBody CertificationRequest certificationRequest) {
        return certificationRequestService.createCertificationRequest(certificationRequest);
    }

    /*
    I included the Status and referenceNo in the response, as it makes more sense to have it.
    User can see the status of the ticket and can point to a specific ticket using the referenceNo
    We can remove them if is needed
     */
    @GetMapping("/certification-requests")
    public List<EmployeeCertificationDTO> getEmployeeCertifications(
            @RequestParam Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ISSUED_ON" ) SortField sortBy,
            @RequestParam(defaultValue = "DESC" ) SortDirection sortDirection
    ) {
        return certificationRequestService.getEmployeeCertifications(employeeId, page, size, sortBy, sortDirection).getContent();
    }
}
