package com.erp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeRequest(
        @NotBlank String name,
        String employeeCode,
        @Email String email,
        String phone,
        String designation,
        Long departmentId,
        LocalDate joiningDate,
        BigDecimal monthlySalary,
        BigDecimal ctc) {
}
