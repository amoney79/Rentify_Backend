package com.amoney.rentify_backend.controller;

import com.amoney.rentify_backend.model.ChatMessage;
import com.amoney.rentify_backend.model.MessageType;
import com.amoney.rentify_backend.model.User;
import com.amoney.rentify_backend.repository.ChatMessageRepository;
import com.amoney.rentify_backend.repository.UserRepository;
import com.amoney.rentify_backend.service.AzureBlobStorageService;
import com.amoney.rentify_backend.service.ChatService;
import com.amoney.rentify_backend.service.WebRtcSignalingService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final AzureBlobStorageService blobStorageService;
    private final WebRtcSignalingService webRtcSignalingService;

    public ChatController(
            ChatService chatService,
            ChatMessageRepository chatMessageRepository,
            UserRepository userRepository,
            AzureBlobStorageService blobStorageService,
            WebRtcSignalingService webRtcSignalingService) {
        this.chatService = chatService;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.blobStorageService = blobStorageService;
        this.webRtcSignalingService = webRtcSignalingService;
    }

    /**
     * List user's active conversations with peer user info and last message
     */
    @GetMapping("/conversations")
    public ResponseEntity<?> getUserConversations(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        List<String> convIds = chatMessageRepository.findConversationIdsByUserId(currentUser.getId());
        List<Map<String, Object>> result = new ArrayList<>();

        for (String cId : convIds) {
            List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByTimestampAsc(cId);
            if (messages.isEmpty()) continue;

            ChatMessage lastMsg = messages.get(messages.size() - 1);
            User partner = lastMsg.getSender().getId().equals(currentUser.getId())
                    ? lastMsg.getRecipient()
                    : lastMsg.getSender();

            long unread = messages.stream()
                    .filter(m -> m.getRecipient().getId().equals(currentUser.getId()) && !m.getIsRead())
                    .count();

            Map<String, Object> conv = new HashMap<>();
            conv.put("conversationId", cId);
            conv.put("partnerId", partner.getId());
            conv.put("partnerName", partner.getFullName());
            conv.put("partnerAvatar", partner.getAvatarUrl());
            conv.put("partnerRole", partner.getRole().name());
            conv.put("partnerIdentityStatus", partner.getIdentityStatus().name());
            conv.put("lastMessage", lastMsg.getContent());
            conv.put("lastMessageTimestamp", lastMsg.getTimestamp().toString());
            conv.put("unreadCount", unread);
            result.add(conv);
        }

        // If no conversations found, provide default conversation with Sarah Johnson (Agent)
        if (result.isEmpty()) {
            userRepository.findByEmail("sarah.agent@rentify.com").ifPresent(sarah -> {
                String cId = chatService.generateConversationId(currentUser.getId(), sarah.getId());
                Map<String, Object> conv = new HashMap<>();
                conv.put("conversationId", cId);
                conv.put("partnerId", sarah.getId());
                conv.put("partnerName", sarah.getFullName());
                conv.put("partnerAvatar", sarah.getAvatarUrl());
                conv.put("partnerRole", sarah.getRole().name());
                conv.put("partnerIdentityStatus", sarah.getIdentityStatus().name());
                conv.put("lastMessage", "Hello! The apartment in Kilimani is available for viewing.");
                conv.put("lastMessageTimestamp", new Date().toString());
                conv.put("unreadCount", 0);
                result.add(conv);
            });
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Retrieve messages for a conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<?> getConversationMessages(
            @PathVariable String conversationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ChatMessage> messages = chatService.getConversationHistory(conversationId);
        List<Map<String, Object>> response = messages.stream()
                .map(chatService::toMessagePayload)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Send chat message via REST
     */
    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {

        User sender = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Long recipientId = Long.valueOf(body.get("recipientId").toString());
        String content = (String) body.get("content");
        String typeStr = (String) body.getOrDefault("messageType", "CHAT");
        String mediaUrl = (String) body.get("mediaUrl");
        Long viewingId = body.get("viewingRequestId") != null ? Long.valueOf(body.get("viewingRequestId").toString()) : null;

        MessageType type = MessageType.CHAT;
        try {
            type = MessageType.valueOf(typeStr);
        } catch (Exception ignored) {}

        ChatMessage msg = chatService.sendMessage(sender, recipientId, content, type, mediaUrl, viewingId);
        return ResponseEntity.ok(chatService.toMessagePayload(msg));
    }

    /**
     * Upload chat media attachment (images, documents)
     */
    @PostMapping("/messages/upload")
    public ResponseEntity<?> uploadAttachment(
            @RequestParam("file") MultipartFile file) {

        String blobName = blobStorageService.uploadFile(file, "chat_attachment");
        String sasUrl = blobStorageService.generateSasUrl(blobName);

        Map<String, Object> response = new HashMap<>();
        response.put("blobName", blobName);
        response.put("url", sasUrl);
        response.put("fileName", file.getOriginalFilename());
        response.put("fileSize", file.getSize());
        response.put("contentType", file.getContentType());
        return ResponseEntity.ok(response);
    }

    /**
     * Mark conversation as read
     */
    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<?> markConversationAsRead(
            @PathVariable String conversationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        chatService.markConversationAsRead(conversationId, currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "Conversation marked as read"));
    }

    /**
     * ICE servers configuration for WebRTC video calling
     */
    @GetMapping("/webrtc/ice-servers")
    public ResponseEntity<?> getIceServers() {
        return ResponseEntity.ok(webRtcSignalingService.getIceServers());
    }

    // -------------------------------------------------------------
    // STOMP WebSocket Message Mappings
    // -------------------------------------------------------------

    @MessageMapping("/chat.send")
    public void handleStompMessage(@Payload Map<String, Object> payload, Principal principal) {
        String email = principal != null ? principal.getName() : (String) payload.get("senderEmail");
        if (email == null) return;

        userRepository.findByEmail(email).ifPresent(sender -> {
            Long recipientId = Long.valueOf(payload.get("recipientId").toString());
            String content = (String) payload.get("content");
            String typeStr = (String) payload.getOrDefault("messageType", "CHAT");
            String mediaUrl = (String) payload.get("mediaUrl");
            Long viewingId = payload.get("viewingRequestId") != null ? Long.valueOf(payload.get("viewingRequestId").toString()) : null;

            MessageType type;
            try {
                type = MessageType.valueOf(typeStr);
            } catch (Exception e) {
                type = MessageType.CHAT;
            }

            chatService.sendMessage(sender, recipientId, content, type, mediaUrl, viewingId);
        });
    }

    @MessageMapping("/webrtc.signal")
    public void handleWebRtcSignal(@Payload Map<String, Object> signalPayload) {
        webRtcSignalingService.processSignal(signalPayload);
    }
}
