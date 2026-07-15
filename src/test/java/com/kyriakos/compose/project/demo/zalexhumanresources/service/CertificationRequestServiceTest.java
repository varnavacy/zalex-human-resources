package com.kyriakos.compose.project.demo.zalexhumanresources.service;


import com.kyriakos.compose.project.demo.zalexhumanresources.dto.EmployeeCertificationDTO;
import com.kyriakos.compose.project.demo.zalexhumanresources.dto.UpdateCertificationRequestDTO;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class CertificationRequestServiceTest {

    public static final String HR_DEPARTMENT = "HR Department";
    public static final String PROOF_OF_EMPLOYMENT = "Proof of employment for visa application to the embassy";
    public static final String UPDATED_PURPOSE = "Updated purpose for the certification request resubmission";
    @Mock
    private CertificationRequestRepository certificationRequestRepository;

    @InjectMocks
    private CertificationRequestService certificationRequestService;

    @Test
    void createCertificationRequest_success() {
        LocalDate date = LocalDate.now();
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
    void createCertificationRequest_purposeTooShort_throwsBadRequest() {
        CertificationRequest request = CertificationRequest.builder()
                .addressTo(HR_DEPARTMENT)
                .purpose("Too short")
                .employeeId(1L)
                .build();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.createCertificationRequest(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Purpose must be at least 50 characters", exception.getReason());
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
    void createCertificationRequest_multilineAddressTo_success() {
        String multilineAddress = "HR Department\nBuilding 3, Floor 2\nAthens, Greece";

        CertificationRequest request = CertificationRequest.builder()
                .addressTo(multilineAddress)
                .purpose(PROOF_OF_EMPLOYMENT)
                .employeeId(1L)
                .build();

        CertificationRequest savedRequest = CertificationRequest.builder()
                .referenceNo(1L)
                .addressTo(multilineAddress)
                .purpose(PROOF_OF_EMPLOYMENT)
                .issuedOn(LocalDate.now())
                .status(Status.OPEN)
                .employeeId(1L)
                .build();

        when(certificationRequestRepository.save(any(CertificationRequest.class))).thenReturn(savedRequest);

        EmployeeCertificationDTO result = certificationRequestService.createCertificationRequest(request);

        assertNotNull(result);
        assertEquals(multilineAddress, result.addressTo());
    }

    @Test
    void createCertificationRequest_invalidAddressTo_throwsBadRequest() {
        CertificationRequest request = CertificationRequest.builder()
                .addressTo("HR Department @#$")
                .purpose(PROOF_OF_EMPLOYMENT)
                .employeeId(1L)
                .build();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.createCertificationRequest(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
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
                .issuedOn(LocalDate.now())
                .build();

        CertificationRequest cert2 = CertificationRequest.builder()
                .addressTo("Cyprus Embassy")
                .purpose("Work Permit")
                .employeeId(employeeId)
                .status(Status.OPEN)
                .issuedOn(LocalDate.now())
                .build();

        Page<CertificationRequest> mockPage = new PageImpl<>(List.of(cert1, cert2));

        when(certificationRequestRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<EmployeeCertificationDTO> result = certificationRequestService.getEmployeeCertifications(employeeId,
                0, 10, SortField.ISSUED_ON, SortDirection.DESC, null, null, null);

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
    void getEmployeeCertifications_filterByStatus_returnsMatchingResults() {
        Long employeeId = 123456L;

        CertificationRequest cert = CertificationRequest.builder()
                .addressTo(HR_DEPARTMENT)
                .purpose(PROOF_OF_EMPLOYMENT)
                .employeeId(employeeId)
                .status(Status.OPEN)
                .issuedOn(LocalDate.now())
                .build();

        Page<CertificationRequest> mockPage = new PageImpl<>(List.of(cert));

        when(certificationRequestRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<EmployeeCertificationDTO> result = certificationRequestService.getEmployeeCertifications(
                employeeId, 0, 10, SortField.ISSUED_ON, SortDirection.DESC, null, null, Status.OPEN);

        assertEquals(1, result.getTotalElements());
        assertEquals(Status.OPEN.name(), result.getContent().getFirst().status());
    }

    @Test
    void getEmployeeCertifications_filterByReferenceNo_returnsMatchingResult() {
        Long employeeId = 123456L;

        CertificationRequest cert = CertificationRequest.builder()
                .referenceNo(5L)
                .addressTo(HR_DEPARTMENT)
                .purpose(PROOF_OF_EMPLOYMENT)
                .employeeId(employeeId)
                .status(Status.OPEN)
                .issuedOn(LocalDate.now())
                .build();

        Page<CertificationRequest> mockPage = new PageImpl<>(List.of(cert));

        when(certificationRequestRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<EmployeeCertificationDTO> result = certificationRequestService.getEmployeeCertifications(
                employeeId, 0, 10, SortField.ISSUED_ON, SortDirection.DESC, 5L, null, null);

        assertEquals(1, result.getTotalElements());
        assertEquals(5L, result.getContent().getFirst().referenceNo());
    }

    @Test
    void getEmployeeCertifications_filterByAddressTo_returnsMatchingResults() {
        Long employeeId = 123456L;

        CertificationRequest cert = CertificationRequest.builder()
                .addressTo("Embassy of Neptune")
                .purpose(PROOF_OF_EMPLOYMENT)
                .employeeId(employeeId)
                .status(Status.OPEN)
                .issuedOn(LocalDate.now())
                .build();

        Page<CertificationRequest> mockPage = new PageImpl<>(List.of(cert));

        when(certificationRequestRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<EmployeeCertificationDTO> result = certificationRequestService.getEmployeeCertifications(
                employeeId, 0, 10, SortField.ISSUED_ON, SortDirection.DESC, null, "Embassy", null);

        assertEquals(1, result.getTotalElements());
        assertEquals("Embassy of Neptune", result.getContent().getFirst().addressTo());
    }

    @Test
    void getEmployeeCertifications_noFiltersMatch_returnsEmptyPage() {
        Long employeeId = 123456L;

        when(certificationRequestRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<EmployeeCertificationDTO> result = certificationRequestService.getEmployeeCertifications(
                employeeId, 0, 10, SortField.ISSUED_ON, SortDirection.DESC, null, "NonExistent", null);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void getEmployeeCertifications_invalidEmployeeId_throwsBadRequest() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.getEmployeeCertifications(null,
                        0, 10, SortField.ISSUED_ON, SortDirection.DESC, null, null, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Employee id field is required", exception.getReason());
    }

    @Test
    void getEmployeeCertifications_zeroEmployeeId_throwsBadRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.getEmployeeCertifications(0L,
                        0, 10, SortField.ISSUED_ON, SortDirection.DESC, null, null, null)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Employee id field is required", exception.getReason());
    }

    @Test
    void getEmployeeCertifications_pageSizeCappedAt10() {
        when(certificationRequestRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        certificationRequestService.getEmployeeCertifications(
                123456L, 0, 100, SortField.ISSUED_ON, SortDirection.DESC, null, null, null);

        verify(certificationRequestRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals(10, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void getEmployeeCertifications_negativePage_throwsBadRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.getEmployeeCertifications(1L,
                        -1, 10, SortField.ISSUED_ON, SortDirection.DESC, null, null, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Page number cannot be negative", exception.getReason());
    }

    @Test
    void getEmployeeCertificationByReferenceNo_success() {
        CertificationRequest cert = CertificationRequest.builder()
                .referenceNo(1L)
                .addressTo(HR_DEPARTMENT)
                .purpose(PROOF_OF_EMPLOYMENT)
                .employeeId(123456L)
                .status(Status.OPEN)
                .issuedOn(LocalDate.now())
                .build();

        when(certificationRequestRepository.findById(1L)).thenReturn(Optional.of(cert));

        EmployeeCertificationDTO result = certificationRequestService.getEmployeeCertificationByReferenceNo(1L);

        assertNotNull(result);
        assertEquals(HR_DEPARTMENT, result.addressTo());
        assertEquals(PROOF_OF_EMPLOYMENT, result.purpose());
        assertEquals(Status.OPEN.name(), result.status());
        assertEquals(1L, result.referenceNo());
    }

    @Test
    void getEmployeeCertificationByReferenceNo_notFound_throwsNotFound() {
        when(certificationRequestRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.getEmployeeCertificationByReferenceNo(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Certification request not found", exception.getReason());
    }

    @Test
    void updatePurpose_success() {
        CertificationRequest cert = CertificationRequest.builder()
                .referenceNo(1L)
                .addressTo(HR_DEPARTMENT)
                .purpose(PROOF_OF_EMPLOYMENT)
                .employeeId(123456L)
                .status(Status.OPEN)
                .issuedOn(LocalDate.now())
                .build();

        CertificationRequest updated = CertificationRequest.builder()
                .referenceNo(1L)
                .addressTo(HR_DEPARTMENT)
                .purpose(UPDATED_PURPOSE)
                .employeeId(123456L)
                .status(Status.OPEN)
                .issuedOn(LocalDate.now())
                .build();

        when(certificationRequestRepository.findById(1L)).thenReturn(Optional.of(cert));
        when(certificationRequestRepository.save(any(CertificationRequest.class))).thenReturn(updated);

        EmployeeCertificationDTO result = certificationRequestService.updatePurposeOnCertificationRequests(
                1L, 123456L, new UpdateCertificationRequestDTO(UPDATED_PURPOSE));

        assertNotNull(result);
        assertEquals(UPDATED_PURPOSE, result.purpose());
        assertEquals(HR_DEPARTMENT, result.addressTo());
        assertEquals(Status.OPEN.name(), result.status());
    }

    @Test
    void updatePurpose_notFound_throwsNotFound() {
        when(certificationRequestRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.updatePurposeOnCertificationRequests(
                        99L, 123456L, new UpdateCertificationRequestDTO(UPDATED_PURPOSE))
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Certification request not found", exception.getReason());
    }

    @Test
    void updatePurpose_wrongEmployee_throwsForbidden() {
        CertificationRequest cert = CertificationRequest.builder()
                .referenceNo(1L)
                .addressTo(HR_DEPARTMENT)
                .purpose(PROOF_OF_EMPLOYMENT)
                .employeeId(123456L)
                .status(Status.OPEN)
                .issuedOn(LocalDate.now())
                .build();

        when(certificationRequestRepository.findById(1L)).thenReturn(Optional.of(cert));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.updatePurposeOnCertificationRequests(
                        1L, 999L, new UpdateCertificationRequestDTO(UPDATED_PURPOSE))
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void updatePurpose_emptyPurpose_throwsBadRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.updatePurposeOnCertificationRequests(
                        1L, 123456L, new UpdateCertificationRequestDTO(""))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Purpose field is required", exception.getReason());
    }

    @Test
    void updatePurpose_purposeTooShort_throwsBadRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> certificationRequestService.updatePurposeOnCertificationRequests(
                        1L, 123456L, new UpdateCertificationRequestDTO("Too short"))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Purpose must be at least 50 characters", exception.getReason());
    }

}
