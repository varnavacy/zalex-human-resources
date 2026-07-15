package com.kyriakos.compose.project.demo.zalexhumanresources.controller;

import com.kyriakos.compose.project.demo.zalexhumanresources.dto.EmployeeCertificationDTO;
import com.kyriakos.compose.project.demo.zalexhumanresources.model.CertificationRequest;
import com.kyriakos.compose.project.demo.zalexhumanresources.model.Status;
import com.kyriakos.compose.project.demo.zalexhumanresources.dto.UpdateCertificationRequestDTO;
import com.kyriakos.compose.project.demo.zalexhumanresources.service.CertificationRequestService;
import com.kyriakos.compose.project.demo.zalexhumanresources.sorting.SortDirection;
import com.kyriakos.compose.project.demo.zalexhumanresources.sorting.SortField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class CertificationRequestController {

    private final CertificationRequestService certificationRequestService;

    public CertificationRequestController(CertificationRequestService certificationRequestService) {
        this.certificationRequestService = certificationRequestService;
    }

    @PostMapping("/certification-requests")
    public EmployeeCertificationDTO createCertificationRequest(@RequestBody CertificationRequest certificationRequest) {
        log.info("POST /certification-requests - employeeId: {}", certificationRequest.getEmployeeId());
        return certificationRequestService.createCertificationRequest(certificationRequest);
    }

    /**
        Included the Status and referenceNo in the response, as it makes more sense to have it.
        User can see the status of the ticket and can point to a specific ticket using the referenceNo
     */
    @GetMapping("/certification-requests")
    public List<EmployeeCertificationDTO> getEmployeeCertifications(
            @RequestParam Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ISSUED_ON" ) SortField sortBy,
            @RequestParam(defaultValue = "DESC" ) SortDirection sortDirection,
            @RequestParam(required = false) Long referenceNo,
            @RequestParam(required = false) String addressTo,
            @RequestParam(required = false) Status status
    ) {
        log.info("GET /certification-requests - employeeId: {}, page: {}, size: {}, sortBy: {}, sortDirection: {}", employeeId, page, size, sortBy, sortDirection);
        return certificationRequestService.getEmployeeCertifications(employeeId, page, size, sortBy, sortDirection
                , referenceNo, addressTo, status).getContent();
    }

    /**
        returning the details of a single certification request.
        currently we return EmployeeCertificationDTO, but in the future when we add more fields
        e.g. description, comments etc we can create a dedicate DTO for this
     */
    @GetMapping("/certification-requests/{referenceNo}")
    public EmployeeCertificationDTO getEmployeeCertificationByReferenceNo(
            @PathVariable Long referenceNo
    ) {
        log.info("GET /certification-requests/{}", referenceNo);
        return certificationRequestService.getEmployeeCertificationByReferenceNo(referenceNo);
    }


    @PatchMapping("/certification-requests/{referenceNo}")
    public EmployeeCertificationDTO updatePurposeOnCertificationRequests(
            @PathVariable Long referenceNo,
            @RequestParam Long employeeId,
            @RequestBody UpdateCertificationRequestDTO updateCertificationRequestDTO
    ) {
        log.info("PATCH /certification-requests/{} - employeeId: {}", referenceNo, employeeId);
        return certificationRequestService.updatePurposeOnCertificationRequests(referenceNo, employeeId, updateCertificationRequestDTO);
    }

}
