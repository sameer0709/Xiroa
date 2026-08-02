package com.erp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
        @NotBlank String name,
        String gstin,
        @Email String email,
        String phone,
        String city,
        String state,
        String address,
        String company) {
}
