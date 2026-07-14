package com.kyriakos.compose.project.demo.zalexhumanresources.specifications;

import com.kyriakos.compose.project.demo.zalexhumanresources.model.CertificationRequest;
import com.kyriakos.compose.project.demo.zalexhumanresources.model.Status;
import org.springframework.data.jpa.domain.Specification;

public class CertificationRequestSpecifications {

    public static Specification<CertificationRequest> hasEmployeeId(Long employeeId) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("employeeId"), employeeId));
    }

    public static Specification<CertificationRequest> hasStatus(Status status) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status));
    }

    public static Specification<CertificationRequest> hasReferenceNo(Long referenceNo) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("referenceNo"), referenceNo));
    }

    public static Specification<CertificationRequest> addressToContains(String addressTo){
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("addressTo")),
                        "%" + addressTo.toLowerCase() + "%"));
    }
}
