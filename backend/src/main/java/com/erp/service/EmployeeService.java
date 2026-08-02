package com.erp.service;

import com.erp.dto.DepartmentRequest;
import com.erp.dto.EmployeeRequest;
import com.erp.dto.LeaveRequestDto;
import com.erp.dto.PayrollRequest;
import com.erp.entity.Department;
import com.erp.entity.Employee;
import com.erp.entity.LeaveRequest;
import com.erp.entity.Payroll;
import com.erp.enums.LeaveStatus;
import com.erp.enums.PaymentStatus;
import com.erp.repository.DepartmentRepository;
import com.erp.repository.EmployeeRepository;
import com.erp.repository.LeaveRequestRepository;
import com.erp.repository.PayrollRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PayrollRepository payrollRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            LeaveRequestRepository leaveRequestRepository,
            PayrollRepository payrollRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.payrollRepository = payrollRepository;
    }

    // ---- Employees ----
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));
    }

    @Transactional
    public Employee createEmployee(EmployeeRequest req) {
        Employee e = new Employee();
        apply(e, req);
        return employeeRepository.save(e);
    }

    @Transactional
    public Employee updateEmployee(Long id, EmployeeRequest req) {
        Employee e = getEmployee(id);
        apply(e, req);
        return employeeRepository.save(e);
    }

    private void apply(Employee e, EmployeeRequest req) {
        e.setName(req.name());
        e.setEmployeeCode(req.employeeCode());
        e.setEmail(req.email());
        e.setPhone(req.phone());
        e.setDesignation(req.designation());
        e.setMonthlySalary(req.monthlySalary() == null ? BigDecimal.ZERO : req.monthlySalary());
        e.setCtc(req.ctc() == null ? e.getMonthlySalary().multiply(BigDecimal.valueOf(12)) : req.ctc());
        if (req.joiningDate() != null)
            e.setJoiningDate(req.joiningDate());
        if (req.departmentId() != null) {
            e.setDepartment(departmentRepository.findById(req.departmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found")));
        }
    }

    // ---- Departments ----
    public List<Department> getDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional
    public Department createDepartment(DepartmentRequest req) {
        Department d = new Department();
        d.setName(req.name());
        d.setDescription(req.description());
        return departmentRepository.save(d);
    }

    // ---- Leave ----
    public List<LeaveRequest> getLeaves() {
        return leaveRequestRepository.findAll();
    }

    @Transactional
    public LeaveRequest requestLeave(LeaveRequestDto req) {
        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(getEmployee(req.employeeId()));
        leave.setStartDate(req.startDate());
        leave.setEndDate(req.endDate());
        leave.setType(req.type());
        leave.setReason(req.reason());
        leave.setStatus(LeaveStatus.PENDING);
        return leaveRequestRepository.save(leave);
    }

    @Transactional
    public LeaveRequest updateLeaveStatus(Long leaveId, LeaveStatus status) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        leave.setStatus(status);
        return leaveRequestRepository.save(leave);
    }

    // ---- Payroll ----
    public List<Payroll> getAllPayroll() {
        return payrollRepository.findAll();
    }

    public List<Payroll> getPayrollByMonth(String month, int year) {
        return payrollRepository.findByMonthAndYear(month, year);
    }

    @Transactional
    public Payroll processPayroll(PayrollRequest req) {
        Employee e = getEmployee(req.employeeId());

        BigDecimal basic = req.basic() == null ? e.getMonthlySalary().multiply(BigDecimal.valueOf(0.5)) : req.basic();
        BigDecimal hra = req.hra() == null ? e.getMonthlySalary().multiply(BigDecimal.valueOf(0.4)) : req.hra();
        BigDecimal allowances = req.allowances() == null ? e.getMonthlySalary().multiply(BigDecimal.valueOf(0.1))
                : req.allowances();
        BigDecimal deductions = req.deductions() == null ? BigDecimal.ZERO : req.deductions();
        BigDecimal netPay = basic.add(hra).add(allowances).subtract(deductions);

        Payroll payroll = new Payroll();
        payroll.setEmployee(e);
        payroll.setPayDate(req.payDate());
        payroll.setMonth(req.month() == null ? String.format("%02d", req.payDate().getMonthValue()) : req.month());
        payroll.setYear(req.year() == 0 ? req.payDate().getYear() : req.year());
        payroll.setBasic(basic);
        payroll.setHra(hra);
        payroll.setAllowances(allowances);
        payroll.setDeductions(deductions);
        payroll.setNetPay(netPay);
        payroll.setStatus(PaymentStatus.UNPAID);
        return payrollRepository.save(payroll);
    }

    @Transactional
    public Payroll markPaid(Long payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));
        payroll.setStatus(PaymentStatus.PAID);
        return payrollRepository.save(payroll);
    }
}
