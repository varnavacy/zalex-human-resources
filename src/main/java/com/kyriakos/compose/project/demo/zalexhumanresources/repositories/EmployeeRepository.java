package com.kyriakos.compose.project.demo.zalexhumanresources.repositories;

import com.kyriakos.compose.project.demo.zalexhumanresources.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
