package com.amoney.rentify_backend.controller;

import com.amoney.rentify_backend.model.*;
import com.amoney.rentify_backend.repository.OwnershipVerificationRepository;
import com.amoney.rentify_backend.repository.UserRepository;
import com.amoney.rentify_backend.repository.VerificationProfileRepository;
import com.amoney.rentify_backend.service.AzureBlobStorageService;
import com.amoney.rentify_backend.service.VerificationWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {

    private final VerificationWorkflowService workflowService;
    private final VerificationProfileRepository profileRepository;
    private final OwnershipVerificationRepository ownershipRepository;
    private final UserRepository userRepository;
    private final AzureBlobStorageService blobStorageService;

    public VerificationController(
            VerificationWorkflowService workflowService,
            VerificationProfileRepository profileRepository,
            OwnershipVerificationRepository ownershipRepository,
            UserRepository userRepository,
            AzureBlobStorageService blobStorageService) {
        this.workflowService = workflowService;
        this.profileRepository = profileRepository;
        this.ownershipRepository = ownershipRepository;
        this.userRepository = userRepository;
        this.blobStorageService = blobStorageService;
    }

    /**
     * Layer 1: Upload Government-issued ID (National ID, Passport, Driver's License)
     */
    @PostMapping("/identity/upload-id")
    public ResponseEntity<?> uploadIdentityDocument(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("frontFile") MultipartFile frontFile,
            @RequestParam(value = "backFile", required = false) MultipartFile backFile,
            @RequestParam(value = "idNumber", required = false) String idNumber,
            @RequestParam(value = "documentType", required = false, defaultValue = "NATIONAL_ID_FRONT") String documentType) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        DocumentType docType;
        try {
            docType = DocumentType.valueOf(documentType);
        } catch (Exception e) {
            docType = DocumentType.NATIONAL_ID_FRONT;
        }

        VerificationProfile profile = workflowService.submitIdentityDocuments(
                user, frontFile, backFile, idNumber, docType
        );

        return ResponseEntity.ok(toProfileDto(profile));
    }

    /**
     * Layer 1: Live Selfie & Biometric Anti-spoofing Check
     */
    @PostMapping("/identity/biometric-check")
    public ResponseEntity<?> submitBiometricCheck(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("selfieFile") MultipartFile selfieFile) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        VerificationProfile profile = workflowService.submitBiometricCheck(user, selfieFile);
        return ResponseEntity.ok(toProfileDto(profile));
    }

    /**
     * Retrieve User's Identity Verification Status
     */
    @GetMapping("/identity/status")
    public ResponseEntity<?> getIdentityStatus(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        return profileRepository.findByUserId(user.getId())
                .map(p -> ResponseEntity.ok(toProfileDto(p)))
                .orElse(ResponseEntity.ok(Map.of("status", "UNVERIFIED", "userId", user.getId())));
    }

    /**
     * Layer 2: Property Ownership Verification Submission
     */
    @PostMapping("/property/submit")
    public ResponseEntity<?> submitPropertyOwnership(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("propertyId") Long propertyId,
            @RequestParam(value = "ownershipType", defaultValue = "INDIVIDUAL") String ownershipTypeStr,
            @RequestParam("titleDeed") MultipartFile titleDeed,
            @RequestParam(value = "corporateDoc", required = false) MultipartFile corporateDoc,
            @RequestParam(value = "authorizationLetter", required = false) MultipartFile authorizationLetter,
            @RequestParam(value = "notes", required = false) String notes) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        OwnershipType type;
        try {
            type = OwnershipType.valueOf(ownershipTypeStr.toUpperCase());
        } catch (Exception e) {
            type = OwnershipType.INDIVIDUAL;
        }

        OwnershipVerification verification = workflowService.submitPropertyOwnership(
                user, propertyId, type, titleDeed, corporateDoc, authorizationLetter, notes
        );

        return ResponseEntity.ok(toOwnershipDto(verification));
    }

    /**
     * Retrieve Property Ownership Verification Status
     */
    @GetMapping("/property/status/{propertyId}")
    public ResponseEntity<?> getPropertyVerificationStatus(
            @PathVariable Long propertyId) {

        return ownershipRepository.findByPropertyId(propertyId)
                .map(v -> ResponseEntity.ok(toOwnershipDto(v)))
                .orElse(ResponseEntity.ok(Map.of("status", "UNVERIFIED", "propertyId", propertyId)));
    }

    private Map<String, Object> toProfileDto(VerificationProfile profile) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", profile.getId());
        dto.put("userId", profile.getUser().getId());
        dto.put("userFullName", profile.getUser().getFullName());
        dto.put("status", profile.getStatus().name());
        dto.put("idDocumentType", profile.getIdDocumentType() != null ? profile.getIdDocumentType().name() : null);
        dto.put("idNumber", profile.getIdNumber());
        dto.put("extractedFullName", profile.getExtractedFullName());
        dto.put("extractedDob", profile.getExtractedDob() != null ? profile.getExtractedDob().toString() : null);
        dto.put("extractedExpiryDate", profile.getExtractedExpiryDate() != null ? profile.getExtractedExpiryDate().toString() : null);
        dto.put("faceMatchScore", profile.getFaceMatchScore());
        dto.put("livenessScore", profile.getLivenessScore());
        dto.put("overallConfidence", profile.getOverallConfidence());
        dto.put("isLive", profile.getIsLive());
        dto.put("spoofDetected", profile.getSpoofDetected());
        dto.put("antiSpoofDetails", profile.getAntiSpoofDetails());
        dto.put("rejectionReason", profile.getRejectionReason());
        dto.put("idFrontUrl", blobStorageService.generateSasUrl(profile.getIdFrontBlobName()));
        dto.put("idBackUrl", blobStorageService.generateSasUrl(profile.getIdBackBlobName()));
        dto.put("selfieUrl", blobStorageService.generateSasUrl(profile.getSelfieBlobName()));
        dto.put("submittedAt", profile.getSubmittedAt() != null ? profile.getSubmittedAt().toString() : null);
        return dto;
    }

    private Map<String, Object> toOwnershipDto(OwnershipVerification v) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", v.getId());
        dto.put("propertyId", v.getProperty().getId());
        dto.put("propertyTitle", v.getProperty().getTitle());
        dto.put("applicantId", v.getApplicantUser().getId());
        dto.put("applicantName", v.getApplicantUser().getFullName());
        dto.put("ownershipType", v.getOwnershipType().name());
        dto.put("extractedOwnerName", v.getExtractedOwnerName());
        dto.put("extractedTitleNumber", v.getExtractedTitleNumber());
        dto.put("extractedPlotNumber", v.getExtractedPlotNumber());
        dto.put("extractedParcelNumber", v.getExtractedParcelNumber());
        dto.put("extractedCounty", v.getExtractedCounty());
        dto.put("nameMatchScore", v.getNameMatchScore());
        dto.put("ownershipConfidenceScore", v.getOwnershipConfidenceScore());
        dto.put("status", v.getStatus().name());
        dto.put("escalatedToManualReview", v.getEscalatedToManualReview());
        dto.put("specialScenarioNotes", v.getSpecialScenarioNotes());
        dto.put("rejectionReason", v.getRejectionReason());
        dto.put("titleDeedSasUrl", blobStorageService.generateSasUrl(v.getCorporateRecordBlobName()));
        dto.put("submittedAt", v.getSubmittedAt() != null ? v.getSubmittedAt().toString() : null);
        return dto;
    }
}
