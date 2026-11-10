package com.amoney.rentify_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "listing_approvals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id", nullable = false)
    private User approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyVerificationStatus approvalStatus;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Builder.Default
    private LocalDateTime approvedAt = LocalDateTime.now();
}
