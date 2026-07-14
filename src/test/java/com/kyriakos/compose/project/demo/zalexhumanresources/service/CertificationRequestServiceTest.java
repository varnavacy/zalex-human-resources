package com.kyriakos.compose.project.demo.zalexhumanresources.service;


import com.kyriakos.compose.project.demo.zalexhumanresources.dto.EmployeeCertificationDTO;
import com.kyriakos.compose.project.demo.zalexhumanresources.model.CertificationRequest;
import com.kyriakos.compose.project.demo.zalexhumanresources.model.Status;
import com.kyriakos.compose.project.demo.zalexhumanresources.repositories.CertificationRequestRepository;
import com.kyriakos.compose.project.demo.zalexhumanresources.sorting.SortDirection;
import com.kyriakos.compose.project.demo.zalexhumanresources.sorting.SortField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class CertificationRequestServiceTest {

    public static final String HR_DEPARTMENT = "HR Department";
    public static final String PROOF_OF_EMPLOYMENT = "Proof of employment";
    @Mock
    private CertificationRequestRepository certificationRequestRepository;

    @InjectMocks
    private CertificationRequestService certificationRequestService;

    @Test
    void createCertificationRequest_success() {
        Date date = new Date();
        CertificationRequest request = CertificationRequest.builder()
                .addressTo(HR_DEPARTMENT)
                .purpose(PROOF_OF_EMPLOYMENT)
                .employeeId(1L)
                .build();

        CertificationRequest savedRequest = CertificationRequest.builder()
                .referenceNo(123L)
                .addressTo(HR_DEPARTMENT)
                .purpose(PROOF_OF_EMPLOYMENT)
                .issuedOn(date)
                .status(Status.OPEN)
                .employeeId(1L)
                .build();

        when(certificationRequestRepository.save(any(CertificationRequest.class))).thenReturn(savedRequest);

        EmployeeCertificationDTO result = certificationRequestService.createCertificationRequest(request);

        assertNotNull(result);
        assertEquals(HR_DEPARTMENT, result.addressTo());
        assertEquals(PROOF_OF_EMPLOYMENT, result.purpose());
        assertEquals(date, result.issuedOn());
        assertEquals(Status.OPEN.name(), result.status());
        assertInstanceOf(Long.class, result.referenceNo());
    }

    @Test
    void createCertificationRequest_dbFailure_throwsInternalServerError() {
        CertificationRequest request = CertificationRequest.builder()
                .addressTo(HR_DEPARTMENT)
                .purpose(PROOF_OF_EMPLOYMENT)
                .employeeId(1L)
                .build();

        when(certificationRequestRepository.save(any(CertificationRequest.class)))
                .thenThrow(new DataAccessException("DB connection failed") {});

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.createCertificationRequest(request)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("Failed to create certification request", exception.getReason());
    }

    @Test
    void createCertificationRequest_missingPurpose_throwsBadRequest() {
        CertificationRequest request = CertificationRequest.builder()
                .addressTo(HR_DEPARTMENT)
                .employeeId(1L)
                .build();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.createCertificationRequest(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Purpose field is required", exception.getReason());
    }

    @Test
    void createCertificationRequest_missingAddressTo_throwsBadRequest() {
        CertificationRequest request = CertificationRequest.builder()
                .purpose(PROOF_OF_EMPLOYMENT)
                .employeeId(1L)
                .build();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.createCertificationRequest(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Address to field is required", exception.getReason());
    }

    @Test
    void createCertificationRequest_missingEmployeeId_throwsBadRequest() {
        CertificationRequest request = CertificationRequest.builder()
                .addressTo(HR_DEPARTMENT)
                .purpose(PROOF_OF_EMPLOYMENT)
                .build();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.createCertificationRequest(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Employee id field is required", exception.getReason());
    }

    @Test
    void getEmployeeCertifications_success() {
        Long employeeId = 123456L;

        CertificationRequest cert1 = CertificationRequest.builder()
                .addressTo(HR_DEPARTMENT)
                .purpose(PROOF_OF_EMPLOYMENT)
                .employeeId(employeeId)
                .status(Status.OPEN)
                .issuedOn(new Date())
                .build();

        CertificationRequest cert2 = CertificationRequest.builder()
                .addressTo("Cyprus Embassy")
                .purpose("Work Permit")
                .employeeId(employeeId)
                .status(Status.OPEN)
                .issuedOn(new Date())
                .build();

        Page<CertificationRequest> mockPage = new PageImpl<>(List.of(cert1, cert2));

        when(certificationRequestRepository.findByEmployeeId(eq(employeeId), any()))
                .thenReturn(mockPage);

        Page<EmployeeCertificationDTO> result = certificationRequestService.getEmployeeCertifications(employeeId,
                0, 10, SortField.ISSUED_ON, SortDirection.DESC);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        EmployeeCertificationDTO first = result.getContent().getFirst();
        assertEquals(HR_DEPARTMENT, first.addressTo());
        assertEquals(PROOF_OF_EMPLOYMENT, first.purpose());
        assertEquals(Status.OPEN.name(), first.status());

        EmployeeCertificationDTO second = result.getContent().get(1);
        assertEquals("Cyprus Embassy", second.addressTo());
        assertEquals("Work Permit", second.purpose());
        assertEquals(Status.OPEN.name(), second.status());
    }

    @Test
    void getEmployeeCertifications_invalidEmployeeId_throwsBadRequest() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.getEmployeeCertifications(null,
                        0, 10, SortField.ISSUED_ON, SortDirection.DESC)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Employee id field is required", exception.getReason());
    }

    @Test
    void getEmployeeCertifications_zeroEmployeeId_throwsBadRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.getEmployeeCertifications(0L,
                        0, 10, SortField.ISSUED_ON, SortDirection.DESC)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Employee id field is required", exception.getReason());
    }

    @Test
    void getEmployeeCertifications_negativePage_throwsBadRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.getEmployeeCertifications(1L,
                        -1, 10, SortField.ISSUED_ON, SortDirection.DESC)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Page number cannot be negative", exception.getReason());
    }

}
