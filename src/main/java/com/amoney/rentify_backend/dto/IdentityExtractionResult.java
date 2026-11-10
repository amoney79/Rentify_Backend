package com.amoney.rentify_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityExtractionResult {
    private String fullName;
    private String idNumber;
    private LocalDate dateOfBirth;
    private LocalDate expiryDate;
    private String documentNumber;
    private String documentType;
    private Double confidence;
    private String rawText;
}
