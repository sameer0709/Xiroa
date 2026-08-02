package com.erp.service;

import com.erp.dto.AuthResponse;
import com.erp.dto.LoginRequest;
import com.erp.dto.RegisterRequest;
import com.erp.entity.Employee;
import com.erp.entity.Role;
import com.erp.entity.User;
import com.erp.enums.RoleName;
import com.erp.repository.EmployeeRepository;
import com.erp.repository.RoleRepository;
import com.erp.repository.UserRepository;
import com.erp.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
            RoleRepository roleRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return buildResponse(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Username already taken");
        }
        Role employeeRole = roleRepository.findByName(RoleName.EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("EMPLOYEE role not seeded"));

        Employee employee = new Employee();
        employee.setName(request.fullName());
        employee.setEmail(request.email());
        employee.setActive(true);
        employee = employeeRepository.save(employee);

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setEnabled(true);
        user.setRole(employeeRole);
        user.setEmployee(employee);
        user = userRepository.save(user);

        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities("ROLE_" + user.getRole().getName().name())
                        .build());
        return new AuthResponse(token, "Bearer", user.getId(), user.getUsername(),
                user.getFullName(), user.getRole().getName().name());
    }
}
