package com.kyriakos.compose.project.demo.zalexhumanresources.service;

import com.kyriakos.compose.project.demo.zalexhumanresources.dto.EmployeeCertificationDTO;
import com.kyriakos.compose.project.demo.zalexhumanresources.model.CertificationRequest;
import com.kyriakos.compose.project.demo.zalexhumanresources.model.Status;
import com.kyriakos.compose.project.demo.zalexhumanresources.dto.UpdateCertificationRequestDTO;
import com.kyriakos.compose.project.demo.zalexhumanresources.repositories.CertificationRequestRepository;
import com.kyriakos.compose.project.demo.zalexhumanresources.sorting.SortDirection;
import com.kyriakos.compose.project.demo.zalexhumanresources.sorting.SortField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.regex.Pattern;

import static com.kyriakos.compose.project.demo.zalexhumanresources.specifications.CertificationRequestSpecifications.*;

@Slf4j
@Service
public class CertificationRequestService {

    // set it as static final so it will compile once when the class is load and not with every request to validate the address to
    private static final Pattern ADDRESS_TO_PATTERN = Pattern.compile("^[a-zA-Z0-9 .,'\n\r-]+$");

    private final CertificationRequestRepository certificationRequestRepository;

    public CertificationRequestService(CertificationRequestRepository certificationRequestRepository) {
        this.certificationRequestRepository = certificationRequestRepository;
    }

    /*
    We are ignoring the status and issuedOn from the fe as it can be anything.
    when we create a request the status should always be open and for the current date
    In addition, if we had an authentication method, most likely I will use that to get employee id
    instead of getting it from the request body.
     */
    public EmployeeCertificationDTO createCertificationRequest(CertificationRequest certificationRequest) {
        verifyCertificationRequest(certificationRequest);
        CertificationRequest cert = CertificationRequest.builder()
                        .addressTo(certificationRequest.getAddressTo())
                        .purpose(certificationRequest.getPurpose())
                        .status(Status.OPEN)
                        .issuedOn(LocalDate.now())
                        .employeeId(certificationRequest.getEmployeeId())
                        .build();
        try {
            EmployeeCertificationDTO result = toDTO(certificationRequestRepository.save(cert));
            log.info("Certification request created successfully - referenceNo: {}, employeeId: {}", result.referenceNo(), result.employeeId());
            return result;
        } catch (DataAccessException e) {
            log.error("Failed to save certification request for employeeId: {}", certificationRequest.getEmployeeId(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create certification request");
        }
    }

    /*
    Mapping the CertificationRequest to a new DTO object as is a better standard,
    if we rename columns in the future of the db for example (not likely but never know)
    we can break the contract with client without a DTO, also sometimes we don't want to expose
    all the columns to the FE.
    In addition, capped the pagination size to 10 (we can agree to a bigger if we want)
    but we don't want to allow a huge value as then define the purpose of pagination
     */
    public Page<EmployeeCertificationDTO> getEmployeeCertifications(Long employeeId, int page, int size,
                                                                    SortField sortBy, SortDirection sortDirection,
                                                                    Long referenceNo, String addressTo, Status status) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page number cannot be negative");
        }
        verifyEmployeeId(employeeId);

        Specification<CertificationRequest> spec = getSpecifications(employeeId, referenceNo, addressTo, status);

        int cappedSize = Math.min(size, 10);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.name()), sortBy.getFieldName());
        Pageable pageable = PageRequest.of(page, cappedSize, sort);

        Page<EmployeeCertificationDTO> result = certificationRequestRepository.findAll(spec, pageable).map(this::toDTO);
        log.info("Found {} certifications for employeeId: {}", result.getTotalElements(), employeeId);
        return result;
    }

    public EmployeeCertificationDTO getEmployeeCertificationByReferenceNo(Long referenceNo) {
        log.info("Fetching certification request - referenceNo: {}", referenceNo);
        return toDTO(findCertificationRequestById(referenceNo));
    }

    public EmployeeCertificationDTO updatePurposeOnCertificationRequests(Long referenceNo, Long employeeId, UpdateCertificationRequestDTO dto) {
        requireValidPurpose(dto.purpose());
        CertificationRequest cert = findCertificationRequestById(referenceNo);
        verifyOwnership(cert, employeeId, referenceNo);
        cert.setPurpose(dto.purpose());
        EmployeeCertificationDTO result = toDTO(certificationRequestRepository.save(cert));
        log.info("Purpose updated successfully - referenceNo: {}, employeeId: {}", referenceNo, employeeId);
        return result;
    }

    private CertificationRequest findCertificationRequestById(Long referenceNo) {
        return certificationRequestRepository.findById(referenceNo)
                .orElseThrow(() -> {
                    log.warn("Certification request not found - referenceNo: {}", referenceNo);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Certification request not found");
                });
    }

    private void verifyOwnership(CertificationRequest cert, Long employeeId, Long referenceNo) {
        if (employeeId != null && !employeeId.equals(cert.getEmployeeId())) {
            log.warn("Unauthorized update attempt - referenceNo: {}, requestingEmployeeId: {}, ownerEmployeeId: {}", referenceNo, employeeId, cert.getEmployeeId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this certification request");
        }
    }

    private Specification<CertificationRequest> getSpecifications(Long employeeId, Long referenceNo, String addressTo, Status status) {
        Specification<CertificationRequest> spec = Specification.where(hasEmployeeId(employeeId));
        if (referenceNo != null) spec = spec.and(hasReferenceNo(referenceNo));
        if (addressTo != null) spec = spec.and(addressToContains(addressTo));
        if (status != null)    spec = spec.and(hasStatus(status));
        return spec;
    }

    private void verifyCertificationRequest(CertificationRequest certificationRequest) {
        requireValidPurpose(certificationRequest.getPurpose());
        requireValidAddressTo(certificationRequest.getAddressTo());
        verifyEmployeeId(certificationRequest.getEmployeeId());
    }

    private void verifyEmployeeId(Long employeeId) {
        if (employeeId == null || employeeId <= 0) {
            log.warn("Validation failed - employeeId is missing or invalid: {}", employeeId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee id field is required");
        }
    }

    private void requireValidPurpose(String value) {
        if (value == null || value.isEmpty()) {
            log.warn("Validation failed - purpose is missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Purpose field is required");
        }
        if (value.length() < 50) {
            log.warn("Validation failed - purpose too short: {} characters", value.length());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Purpose must be at least 50 characters");
        }
    }

    /*
    Added basic punctuation and spaces as make sense .e.g Embassy of U.S.
    \n\r will handle multiple lines as this is a text area
     */
    private void requireValidAddressTo(String value) {
        if (value == null || value.isEmpty()) {
            log.warn("Validation failed - address to is missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address to field is required");
        }
        if (!ADDRESS_TO_PATTERN.matcher(value).matches()) {
            log.warn("Validation failed - address to contains invalid characters: {}", value);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Address to field contains invalid characters. Only letters, numbers, spaces, and . , ' - are allowed");
        }
    }

    private EmployeeCertificationDTO toDTO(CertificationRequest cert) {
        return new EmployeeCertificationDTO(
                cert.getAddressTo(),
                cert.getPurpose(),
                cert.getIssuedOn(),
                cert.getReferenceNo(),
                cert.getStatus().name(),
                cert.getEmployeeId()
        );
    }

}
