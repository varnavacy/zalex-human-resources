package com.kyriakos.compose.project.demo.zalexhumanresources.repositories;

import com.kyriakos.compose.project.demo.zalexhumanresources.model.CertificationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificationRequestRepository extends JpaRepository<CertificationRequest, Long> {

    Page<CertificationRequest> findByEmployeeId(Long employeeId, Pageable pageable);
}
