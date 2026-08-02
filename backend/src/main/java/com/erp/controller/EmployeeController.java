package com.erp.controller;

import com.erp.dto.DepartmentRequest;
import com.erp.dto.EmployeeRequest;
import com.erp.dto.LeaveRequestDto;
import com.erp.dto.PayrollRequest;
import com.erp.entity.Department;
import com.erp.entity.Employee;
import com.erp.entity.LeaveRequest;
import com.erp.entity.Payroll;
import com.erp.enums.LeaveStatus;
import com.erp.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<Employee>> getAll() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Employee> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployee(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Employee> create(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Employee> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    // ---- Departments ----
    @GetMapping("/departments/all")
    public ResponseEntity<List<Department>> getDepartments() {
        return ResponseEntity.ok(employeeService.getDepartments());
    }

    @PostMapping("/departments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createDepartment(request));
    }

    // ---- Leave ----
    @GetMapping("/leaves")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<LeaveRequest>> getLeaves() {
        return ResponseEntity.ok(employeeService.getLeaves());
    }

    @PostMapping("/leaves/request")
    public ResponseEntity<LeaveRequest> requestLeave(@Valid @RequestBody LeaveRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.requestLeave(request));
    }

    @PutMapping("/leaves/{leaveId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<LeaveRequest> updateLeaveStatus(@PathVariable Long leaveId,
            @RequestParam LeaveStatus status) {
        return ResponseEntity.ok(employeeService.updateLeaveStatus(leaveId, status));
    }

    // ---- Payroll ----
    @GetMapping("/payroll")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<Payroll>> getAllPayroll() {
        return ResponseEntity.ok(employeeService.getAllPayroll());
    }

    @GetMapping("/payroll/month")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<Payroll>> getPayrollByMonth(@RequestParam String month, @RequestParam int year) {
        return ResponseEntity.ok(employeeService.getPayrollByMonth(month, year));
    }

    @PostMapping("/payroll")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Payroll> processPayroll(@Valid @RequestBody PayrollRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.processPayroll(request));
    }

    @PutMapping("/payroll/{id}/paid")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Payroll> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.markPaid(id));
    }
}
