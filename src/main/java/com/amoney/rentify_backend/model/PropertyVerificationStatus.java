package com.amoney.rentify_backend.model;

public enum PropertyVerificationStatus {
    UNVERIFIED,
    PENDING_REVIEW,
    ELIGIBLE_FOR_REVIEW,
    VERIFIED_OWNERSHIP,
    REJECTED,
    FLAGGED_FRAUD,
    ESCALATED_MANUAL_REVIEW
}
