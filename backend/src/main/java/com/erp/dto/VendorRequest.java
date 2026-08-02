package com.erp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VendorRequest(
        @NotBlank String name,
        String gstin,
        String contactPerson,
        String phone,
        @Email String email,
        String address,
        String city,
        String state) {
}
