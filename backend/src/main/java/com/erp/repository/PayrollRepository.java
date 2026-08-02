package com.erp.repository;

import com.erp.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    List<Payroll> findByEmployeeId(Long employeeId);

    List<Payroll> findByMonthAndYear(String month, int year);
}
