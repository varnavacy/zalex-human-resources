package com.kyriakos.compose.project.demo.zalexhumanresources.service;

import com.kyriakos.compose.project.demo.zalexhumanresources.dto.EmployeeCertificationDTO;
import com.kyriakos.compose.project.demo.zalexhumanresources.model.CertificationRequest;
import com.kyriakos.compose.project.demo.zalexhumanresources.model.Status;
import com.kyriakos.compose.project.demo.zalexhumanresources.repositories.CertificationRequestRepository;
import com.kyriakos.compose.project.demo.zalexhumanresources.sorting.SortDirection;
import com.kyriakos.compose.project.demo.zalexhumanresources.sorting.SortField;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

import static com.kyriakos.compose.project.demo.zalexhumanresources.specifications.CertificationRequestSpecifications.*;

@Service
public class CertificationRequestService {

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
                        .issuedOn(new Date())
                        .employeeId(certificationRequest.getEmployeeId())
                        .build();
        try {
            return toDTO(certificationRequestRepository.save(cert));
        } catch (DataAccessException e) {
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

        Specification<CertificationRequest> spec = getSpecifications(employeeId,referenceNo,addressTo,status);

        int cappedSize = Math.min(size, 10);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.name()), sortBy.getFieldName());
        Pageable pageable = PageRequest.of(page, cappedSize, sort);

        return certificationRequestRepository.findAll(spec, pageable)
                .map(this::toDTO);
    }

    public EmployeeCertificationDTO getEmployeeCertificationByReferenceNo(Long referenceNo) {
        return certificationRequestRepository.findById(referenceNo)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certification request not found"));
    }

    private Specification<CertificationRequest> getSpecifications(Long employeeId, Long referenceNo, String addressTo, Status status) {
        Specification<CertificationRequest> spec = Specification.where(hasEmployeeId(employeeId));
        if (referenceNo != null) spec = spec.and(hasReferenceNo(referenceNo));
        if (addressTo != null) spec = spec.and(addressToContains(addressTo));
        if (status != null)    spec = spec.and(hasStatus(status));
        return spec;
    }

    private void verifyCertificationRequest(CertificationRequest certificationRequest) {
        requireNonBlank(certificationRequest.getPurpose(), "Purpose");
        requireNonBlank(certificationRequest.getAddressTo(), "Address to");
        verifyEmployeeId(certificationRequest.getEmployeeId());
    }

    private void verifyEmployeeId(Long employeeId) {
        if (employeeId == null || employeeId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee id field is required");
        }
    }

    private void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " field is required");
        }
    }

    private EmployeeCertificationDTO toDTO(CertificationRequest cert) {
        return new EmployeeCertificationDTO(
                cert.getAddressTo(),
                cert.getPurpose(),
                cert.getIssuedOn(),
                cert.getReferenceNo(),
                cert.getStatus().name()
        );
    }

}
