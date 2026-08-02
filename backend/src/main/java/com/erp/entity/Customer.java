package com.erp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 20)
    private String gstin;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(length = 200)
    private String address;

    @Column(length = 50)
    private String company;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPurchases = BigDecimal.ZERO;

    private int totalOrders = 0;

    @Column(nullable = false)
    private boolean active = true;
}
