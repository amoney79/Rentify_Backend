package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.FaceVerification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;


public interface FaceVerificationRepository extends MongoRepository<FaceVerification, Long> {
    List<FaceVerification> findByVerificationProfileId(Long profileId);
}
