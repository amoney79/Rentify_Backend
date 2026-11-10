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
public class OwnershipExtractionResult {
    private String registeredOwnerName;
    private String titleNumber;
    private String plotNumber;
    private String parcelNumber;
    private LocalDate registrationDate;
    private String county;
    private String propertyAddress;
    private Double confidence;
    private String rawText;
}
