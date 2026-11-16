package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.FaceVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface FaceVerificationRepository extends JpaRepository<FaceVerification, Long> {
    List<FaceVerification> findByVerificationProfileId(Long profileId);
}
