package com.amoney.rentify_backend.controller;

import com.amoney.rentify_backend.model.User;
import com.amoney.rentify_backend.model.ViewingRequest;
import com.amoney.rentify_backend.model.ViewingStatus;
import com.amoney.rentify_backend.repository.UserRepository;
import com.amoney.rentify_backend.repository.ViewingRequestRepository;
import com.amoney.rentify_backend.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/viewings")
public class ViewingScheduleController {

    private final ChatService chatService;
    private final ViewingRequestRepository viewingRequestRepository;
    private final UserRepository userRepository;

    public ViewingScheduleController(
            ChatService chatService,
            ViewingRequestRepository viewingRequestRepository,
            UserRepository userRepository) {
        this.chatService = chatService;
        this.viewingRequestRepository = viewingRequestRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/schedule")
    public ResponseEntity<?> bookViewing(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {

        User tenant = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Long propertyId = Long.valueOf(body.get("propertyId").toString());
        String dateStr = (String) body.get("viewingDate");
        String timeStr = (String) body.get("viewingTime");
        String notes = (String) body.get("notes");

        LocalDate date = LocalDate.parse(dateStr);
        ViewingRequest request = chatService.scheduleViewing(tenant, propertyId, date, timeStr, notes);

        return ResponseEntity.ok(toDto(request));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyViewingRequests(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        List<ViewingRequest> list = viewingRequestRepository.findByTenantIdOrderByCreatedAtDesc(user.getId());
        if (list.isEmpty()) {
            list = viewingRequestRepository.findByLandlordIdOrderByCreatedAtDesc(user.getId());
        }

        List<Map<String, Object>> dtos = list.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<?> respondToViewing(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {

        User respondent = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        String statusStr = body.get("status");
        String comment = body.get("comment");

        ViewingStatus status;
        try {
            status = ViewingStatus.valueOf(statusStr.toUpperCase());
        } catch (Exception e) {
            status = ViewingStatus.ACCEPTED;
        }

        ViewingRequest updated = chatService.respondToViewing(respondent, id, status, comment);
        return ResponseEntity.ok(toDto(updated));
    }

    private Map<String, Object> toDto(ViewingRequest r) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", r.getId());
        map.put("propertyId", r.getProperty().getId());
        map.put("propertyTitle", r.getProperty().getTitle());
        map.put("propertyAddress", r.getProperty().getAddress());
        map.put("tenantId", r.getTenant().getId());
        map.put("tenantName", r.getTenant().getFullName());
        map.put("landlordId", r.getLandlord().getId());
        map.put("landlordName", r.getLandlord().getFullName());
        map.put("viewingDate", r.getViewingDate().toString());
        map.put("viewingTime", r.getViewingTime());
        map.put("notes", r.getNotes());
        map.put("status", r.getStatus().name());
        map.put("createdAt", r.getCreatedAt().toString());
        return map;
    }
}
