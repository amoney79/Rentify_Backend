package com.amoney.rentify_backend.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Document(collection="users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable, MongoIdentifiable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    
    private String email;

    
    private String password;

   
    private String fullName;

    private String phone;

    
    @Builder.Default
    private Role role = Role.ROLE_TENANT;

    @Builder.Default
    private IdentityStatus identityStatus = IdentityStatus.UNVERIFIED;

    @Builder.Default
    private AgentStatus agentStatus = AgentStatus.NOT_APPLICABLE;

    private String idNumber;

    private String avatarUrl;

    private String county;

    private String agencyName;

    private String agencyLicenseNumber;

    @CreatedDate
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    }
