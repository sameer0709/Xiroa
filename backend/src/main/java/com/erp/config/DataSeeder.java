package com.erp.config;

import com.erp.entity.*;
import com.erp.enums.*;
import com.erp.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Database initializer.
 *
 * Always creates the three core roles (ADMIN / MANAGER / EMPLOYEE) and a
 * default admin bootstrap user (only if no users exist yet) so the app is
 * usable on a fresh database.
 *
 * When xiroa.seed-demo=true (env SEED_DEMO=true), a full demo dataset is also
 * loaded: departments, employees, products, inventory, customers, vendors,
 * invoices and payroll. This is OFF by default for production.
 */
@Configuration
public class DataSeeder {

        @Value("${xiroa.seed-demo:false}")
        private boolean seedDemo;

        @Value("${xiroa.admin-password:admin123}")
        private String adminPassword;

        @Bean
        CommandLineRunner seed(RoleRepository roleRepository,
                        UserRepository userRepository,
                        DepartmentRepository departmentRepository,
                        EmployeeRepository employeeRepository,
                        CategoryRepository categoryRepository,
                        ProductRepository productRepository,
                        InventoryRepository inventoryRepository,
                        CustomerRepository customerRepository,
                        VendorRepository vendorRepository,
                        InvoiceRepository invoiceRepository,
                        StockMovementRepository stockMovementRepository,
                        PayrollRepository payrollRepository,
                        PasswordEncoder passwordEncoder) {

                return args -> {
                        // ---------- Always ensure roles exist ----------
                        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                                        .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ADMIN)));
                        roleRepository.findByName(RoleName.MANAGER)
                                        .orElseGet(() -> roleRepository.save(new Role(null, RoleName.MANAGER)));
                        roleRepository.findByName(RoleName.EMPLOYEE)
                                        .orElseGet(() -> roleRepository.save(new Role(null, RoleName.EMPLOYEE)));

                        // ---------- Bootstrap admin user (only if no users exist) ----------
                        if (userRepository.count() == 0) {
                                User admin = new User();
                                admin.setUsername("admin");
                                admin.setPassword(passwordEncoder.encode(adminPassword));
                                admin.setFullName("Administrator");
                                admin.setEmail("admin@xiroa.app");
                                admin.setEnabled(true);
                                admin.setRole(adminRole);
                                admin.setEmployee(null);
                                userRepository.save(admin);
                                System.out.println(
                                                ">>> Xiroa: created default admin user (username=admin, password from ADMIN_PASSWORD env).");
                        }

                        // ---------- Demo data (optional, off by default) ----------
                        if (!seedDemo || userRepository.count() > 1) {
                                return;
                        }

                        // ---------- Departments ----------
                        Department sales = departmentRepository
                                        .save(new Department(null, "Sales", "Sales and marketing"));
                        Department ops = departmentRepository
                                        .save(new Department(null, "Operations", "Operations and logistics"));
                        Department finance = departmentRepository
                                        .save(new Department(null, "Finance", "Finance and accounting"));

                        // ---------- Employees ----------
                        Employee eAdmin = employeeRepository
                                        .save(employee("Rahul Sharma", "EMP001", "rahul@erpstartup.com", "9876500001",
                                                        "Founder & CEO", sales, new BigDecimal("150000.00")));
                        Employee eManager = employeeRepository
                                        .save(employee("Priya Patel", "EMP002", "priya@erpstartup.com", "9876500002",
                                                        "Operations Manager", ops, new BigDecimal("80000.00")));
                        Employee eAmit = employeeRepository
                                        .save(employee("Amit Verma", "EMP003", "amit@erpstartup.com", "9876500003",
                                                        "Sales Executive", sales, new BigDecimal("45000.00")));
                        Employee eSneha = employeeRepository
                                        .save(employee("Sneha Reddy", "EMP004", "sneha@erpstartup.com", "9876500004",
                                                        "Accountant", finance, new BigDecimal("55000.00")));

                        // ---------- Demo users (manager / employee) ----------
                        Role managerRole = roleRepository.findByName(RoleName.MANAGER).orElseThrow();
                        Role employeeRole = roleRepository.findByName(RoleName.EMPLOYEE).orElseThrow();
                        userRepository.save(user("manager", "manager123", "Priya Patel", "manager@erpstartup.com",
                                        managerRole, eManager, passwordEncoder));
                        userRepository.save(user("employee", "employee123", "Amit Verma", "amit@erpstartup.com",
                                        employeeRole, eAmit, passwordEncoder));

                        // ---------- Categories ----------
                        Category electronics = categoryRepository
                                        .save(new Category(null, "Electronics", "Electronic goods"));
                        Category furniture = categoryRepository
                                        .save(new Category(null, "Furniture", "Office and home furniture"));
                        Category apparel = categoryRepository
                                        .save(new Category(null, "Apparel", "Clothing and accessories"));

                        // ---------- Products ----------
                        Product laptop = product("LAP-001", "Dell Laptop Inspiron", "15 inch business laptop",
                                        electronics,
                                        new BigDecimal("45000.00"), new BigDecimal("55000.00"), new BigDecimal("18.00"),
                                        "84713000", 5, 25,
                                        productRepository, inventoryRepository, stockMovementRepository);
                        Product monitor = product("MON-001", "Samsung 24in Monitor", "Full HD LED monitor", electronics,
                                        new BigDecimal("8000.00"), new BigDecimal("11500.00"), new BigDecimal("18.00"),
                                        "85285200", 10, 40,
                                        productRepository, inventoryRepository, stockMovementRepository);
                        Product chair = product("CHR-001", "Ergonomic Office Chair", "Adjustable height office chair",
                                        furniture,
                                        new BigDecimal("6000.00"), new BigDecimal("9500.00"), new BigDecimal("12.00"),
                                        "94013000", 5, 15,
                                        productRepository, inventoryRepository, stockMovementRepository);
                        Product desk = product("DSK-001", "Office Desk 4ft", "Wooden office desk", furniture,
                                        new BigDecimal("12000.00"), new BigDecimal("18000.00"), new BigDecimal("12.00"),
                                        "94032090", 3, 10,
                                        productRepository, inventoryRepository, stockMovementRepository);
                        Product tshirt = product("TEE-001", "Cotton T-Shirt", "100% cotton round neck", apparel,
                                        new BigDecimal("250.00"), new BigDecimal("499.00"), new BigDecimal("5.00"),
                                        "61091000", 20, 100,
                                        productRepository, inventoryRepository, stockMovementRepository);

                        // ---------- Customers ----------
                        Customer c1 = customerRepository
                                        .save(customer("GreenLeaf Retail", "27AABCG1283P1ZK", "buyer@greenleaf.in",
                                                        "9876501234",
                                                        "Mumbai", "Maharashtra", "12 Linking Road, Bandra",
                                                        "GreenLeaf Retail Pvt Ltd"));
                        Customer c2 = customerRepository.save(customer("TechNova Solutions", "27AAACN1160P1ZC",
                                        "purchase@technova.in", "9988776655", "Pune", "Maharashtra", "44 Baner Road",
                                        "TechNova Solutions"));
                        Customer c3 = customerRepository.save(customer("Sharma Traders", "07AAXPS7214L1ZB",
                                        "sharma.traders@gmail.com", "9765432109", "Delhi", "Delhi", "12 Chandni Chowk",
                                        "Sharma Traders"));

                        // ---------- Vendors ----------
                        vendorRepository.save(vendor("Global Electronics Ltd", "27AABCT2234M1ZT", "Rajesh K",
                                        "9822113344",
                                        "vendors@globalelec.in", "23 MIDC, Pune", "Pune", "Maharashtra"));
                        vendorRepository.save(vendor("WoodCraft Furniture", "27AAFCW8890K1ZP", "Meena S", "9012345678",
                                        "sales@woodcraft.in", "8 Gandhi Road, Surat", "Surat", "Gujarat"));

                        // ---------- Invoices (last 6 months) ----------
                        List<Product> demoProducts = List.of(laptop, monitor, chair, desk, tshirt);
                        LocalDateTime now = LocalDateTime.now();
                        for (int m = 5; m >= 0; m--) {
                                LocalDateTime base = now.minusMonths(m);
                                int invoicesInMonth = 2 + (int) (Math.random() * 3);
                                for (int i = 0; i < invoicesInMonth; i++) {
                                        Customer customer = (m + i) % 3 == 0 ? c1 : ((m + i) % 3 == 1 ? c2 : c3);
                                        Invoice invoice = new Invoice();
                                        invoice.setCustomer(customer);
                                        invoice.setInvoiceNumber(
                                                        "INV-" + base.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                                                        + "-" + (i + 1) + "-" + (m * 7 + 1));
                                        invoice.setInvoiceDate(base.minusDays(i * 3 + 1));
                                        invoice.setNotes("Auto-generated demo invoice");

                                        Product p = demoProducts.get((m + i) % demoProducts.size());
                                        int qty = 2 + (int) (Math.random() * 6);
                                        BigDecimal subtotal = p.getSellingPrice().multiply(BigDecimal.valueOf(qty));
                                        BigDecimal gst = subtotal.multiply(p.getGstRate())
                                                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                                        InvoiceItem item = new InvoiceItem();
                                        item.setProduct(p);
                                        item.setQuantity(qty);
                                        item.setUnitPrice(p.getSellingPrice());
                                        item.setGstRate(p.getGstRate());
                                        item.setSubtotal(subtotal);
                                        item.setGstAmount(gst);
                                        item.setLineTotal(subtotal.add(gst));
                                        invoice.addItem(item);

                                        invoice.setSubtotal(subtotal);
                                        invoice.setGstAmount(gst);
                                        invoice.setTotal(subtotal.add(gst));

                                        PaymentStatus ps = i % 3 == 0 ? PaymentStatus.PAID
                                                        : (i % 3 == 1 ? PaymentStatus.PARTIALLY_PAID
                                                                        : PaymentStatus.UNPAID);
                                        invoice.setPaymentStatus(ps);
                                        BigDecimal paid = switch (ps) {
                                                case PAID -> invoice.getTotal();
                                                case PARTIALLY_PAID ->
                                                        invoice.getTotal().multiply(BigDecimal.valueOf(0.5)).setScale(2,
                                                                        RoundingMode.HALF_UP);
                                                default -> BigDecimal.ZERO;
                                        };
                                        invoice.setAmountPaid(paid);
                                        invoice.setBalanceDue(invoice.getTotal().subtract(paid));
                                        invoice.setPaymentMode(ps == PaymentStatus.UNPAID ? null : "UPI");

                                        invoiceRepository.save(invoice);

                                        inventoryRepository.findByProductId(p.getId()).ifPresent(inv -> {
                                                inv.setQuantityOnHand(Math.max(0, inv.getQuantityOnHand() - qty));
                                                inventoryRepository.save(inv);
                                        });
                                        customer.setTotalOrders(customer.getTotalOrders() + 1);
                                        customer.setTotalPurchases(
                                                        customer.getTotalPurchases().add(invoice.getTotal()));
                                }
                        }
                        customerRepository.save(c1);
                        customerRepository.save(c2);
                        customerRepository.save(c3);

                        // ---------- Payroll (current month, all employees) ----------
                        List<Employee> allEmployees = List.of(eAdmin, eManager, eAmit, eSneha);
                        String currentMonth = String.format("%02d", LocalDate.now().getMonthValue());
                        int currentYear = LocalDate.now().getYear();
                        for (Employee emp : allEmployees) {
                                BigDecimal salary = emp.getMonthlySalary();
                                BigDecimal basic = salary.multiply(BigDecimal.valueOf(0.50)).setScale(2,
                                                RoundingMode.HALF_UP);
                                BigDecimal hra = salary.multiply(BigDecimal.valueOf(0.40)).setScale(2,
                                                RoundingMode.HALF_UP);
                                BigDecimal allowances = salary.multiply(BigDecimal.valueOf(0.10)).setScale(2,
                                                RoundingMode.HALF_UP);
                                BigDecimal deductions = salary.multiply(BigDecimal.valueOf(0.08)).setScale(2,
                                                RoundingMode.HALF_UP);
                                BigDecimal netPay = basic.add(hra).add(allowances).subtract(deductions);
                                payrollRepository.save(new com.erp.entity.Payroll(null, emp,
                                                LocalDate.now().withDayOfMonth(1),
                                                currentMonth, currentYear, basic, hra, allowances, deductions, netPay,
                                                PaymentStatus.PAID));
                        }

                        System.out.println(">>> Xiroa: demo data loaded (SEED_DEMO=true).");
                };
        }

        private Employee employee(String name, String code, String email, String phone,
                        String designation, Department dept, BigDecimal salary) {
                Employee e = new Employee();
                e.setName(name);
                e.setEmployeeCode(code);
                e.setEmail(email);
                e.setPhone(phone);
                e.setDesignation(designation);
                e.setDepartment(dept);
                e.setJoiningDate(LocalDate.now().minusMonths(6));
                e.setMonthlySalary(salary);
                e.setCtc(salary.multiply(BigDecimal.valueOf(12)));
                e.setActive(true);
                return e;
        }

        private User user(String username, String password, String fullName, String email,
                        Role role, Employee employee, PasswordEncoder encoder) {
                User u = new User();
                u.setUsername(username);
                u.setPassword(encoder.encode(password));
                u.setFullName(fullName);
                u.setEmail(email);
                u.setEnabled(true);
                u.setRole(role);
                u.setEmployee(employee);
                return u;
        }

        private Product product(String sku, String name, String desc, Category category,
                        BigDecimal cost, BigDecimal selling, BigDecimal gst, String hsn,
                        int reorderLevel, int openingStock,
                        ProductRepository productRepository,
                        InventoryRepository inventoryRepository,
                        StockMovementRepository stockMovementRepository) {
                Product p = new Product();
                p.setSku(sku);
                p.setName(name);
                p.setDescription(desc);
                p.setCategory(category);
                p.setCostPrice(cost);
                p.setSellingPrice(selling);
                p.setGstRate(gst);
                p.setHsnCode(hsn);
                p.setReorderLevel(reorderLevel);
                p.setActive(true);
                p = productRepository.save(p);

                Inventory inv = new Inventory();
                inv.setProduct(p);
                inv.setQuantityOnHand(openingStock);
                inv.setWarehouse("Main");
                inventoryRepository.save(inv);

                StockMovement movement = new StockMovement();
                movement.setProduct(p);
                movement.setType(StockMovementType.ADJUSTMENT);
                movement.setQuantity(openingStock);
                movement.setUnitCost(cost);
                movement.setReference("OPENING");
                movement.setNotes("Opening stock");
                stockMovementRepository.save(movement);
                return p;
        }

        private Customer customer(String name, String gstin, String email, String phone,
                        String city, String state, String address, String company) {
                Customer c = new Customer();
                c.setName(name);
                c.setGstin(gstin);
                c.setEmail(email);
                c.setPhone(phone);
                c.setCity(city);
                c.setState(state);
                c.setAddress(address);
                c.setCompany(company);
                c.setTotalPurchases(BigDecimal.ZERO);
                c.setActive(true);
                return c;
        }

        private Vendor vendor(String name, String gstin, String contact, String phone,
                        String email, String address, String city, String state) {
                Vendor v = new Vendor();
                v.setName(name);
                v.setGstin(gstin);
                v.setContactPerson(contact);
                v.setPhone(phone);
                v.setEmail(email);
                v.setAddress(address);
                v.setCity(city);
                v.setState(state);
                return v;
        }
}
