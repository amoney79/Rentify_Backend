package com.amoney.rentify_backend.controller;

import com.amoney.rentify_backend.model.Property;
import com.amoney.rentify_backend.model.PropertyVerificationStatus;
import com.amoney.rentify_backend.model.User;
import com.amoney.rentify_backend.repository.PropertyRepository;
import com.amoney.rentify_backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public PropertyController(PropertyRepository propertyRepository, UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    /**
     * Public Marketplace Feed: Returns only verified listings
     */
    @GetMapping("/public")
    public ResponseEntity<?> getPublicListings() {
        List<Property> list = propertyRepository.findByIsPubliclyVisibleTrue();
        return ResponseEntity.ok(list.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping
    public ResponseEntity<?> getAllProperties() {
        List<Property> list = propertyRepository.findAll();
        return ResponseEntity.ok(list.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPropertyById(@PathVariable Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + id));

        return ResponseEntity.ok(toDto(property));
    }

    @PostMapping
    public ResponseEntity<?> createProperty(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {

        User landlord = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Property property = Property.builder()
                .landlord(landlord)
                .title((String) body.get("title"))
                .description((String) body.get("description"))
                .propertyType((String) body.get("propertyType"))
                .county((String) body.get("county"))
                .sublocation((String) body.get("sublocation"))
                .address((String) body.get("address"))
                .price(body.get("price") != null ? new BigDecimal(body.get("price").toString()) : BigDecimal.ZERO)
                .bedrooms(body.get("bedrooms") != null ? Integer.valueOf(body.get("bedrooms").toString()) : 1)
                .bathrooms(body.get("bathrooms") != null ? Integer.valueOf(body.get("bathrooms").toString()) : 1)
                .imageUrl((String) body.get("imageUrl"))
                .verificationStatus(PropertyVerificationStatus.UNVERIFIED)
                .isPubliclyVisible(false)
                .build();

        Property saved = propertyRepository.save(property);
        return ResponseEntity.ok(toDto(saved));
    }

    private Map<String, Object> toDto(Property p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("title", p.getTitle());
        map.put("description", p.getDescription());
        map.put("propertyType", p.getPropertyType());
        map.put("county", p.getCounty());
        map.put("sublocation", p.getSublocation());
        map.put("address", p.getAddress());
        map.put("price", p.getPrice());
        map.put("bedrooms", p.getBedrooms());
        map.put("bathrooms", p.getBathrooms());
        map.put("imageUrl", p.getImageUrl());
        map.put("verificationStatus", p.getVerificationStatus().name());
        map.put("isPubliclyVisible", p.getIsPubliclyVisible());
        map.put("landlordId", p.getLandlord().getId());
        map.put("landlordName", p.getLandlord().getFullName());
        map.put("landlordRole", p.getLandlord().getRole().name());
        map.put("landlordIdentityStatus", p.getLandlord().getIdentityStatus().name());
        map.put("landlordAgentStatus", p.getLandlord().getAgentStatus().name());
        map.put("landlordAvatar", p.getLandlord().getAvatarUrl());
        map.put("createdAt", p.getCreatedAt().toString());
        return map;
    }
}
