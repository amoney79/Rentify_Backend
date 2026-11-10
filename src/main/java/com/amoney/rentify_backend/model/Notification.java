package com.amoney.rentify_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(nullable = false)
    private String type; // "IDENTITY_APPROVED", "IDENTITY_REJECTED", "PROPERTY_APPROVED", "DOC_REQUESTED", "VIEWING_UPDATE"

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    private String actionUrl;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
