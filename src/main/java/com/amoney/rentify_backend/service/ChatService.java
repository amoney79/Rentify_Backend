package com.amoney.rentify_backend.service;

import com.amoney.rentify_backend.model.*;
import com.amoney.rentify_backend.repository.ChatMessageRepository;
import com.amoney.rentify_backend.repository.PropertyRepository;
import com.amoney.rentify_backend.repository.UserRepository;
import com.amoney.rentify_backend.repository.ViewingRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatMessageRepository chatMessageRepository;
    private final ViewingRequestRepository viewingRequestRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    public ChatService(
            ChatMessageRepository chatMessageRepository,
            ViewingRequestRepository viewingRequestRepository,
            UserRepository userRepository,
            PropertyRepository propertyRepository,
            SimpMessagingTemplate messagingTemplate,
            NotificationService notificationService) {
        this.chatMessageRepository = chatMessageRepository;
        this.viewingRequestRepository = viewingRequestRepository;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
    }

    public String generateConversationId(Long userA, Long userB) {
        long min = Math.min(userA, userB);
        long max = Math.max(userA, userB);
        return min + "_" + max;
    }

    @Transactional
    public ChatMessage sendMessage(
            User sender,
            Long recipientId,
            String content,
            MessageType messageType,
            String mediaUrl,
            Long viewingRequestId) {

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found: " + recipientId));

        String conversationId = generateConversationId(sender.getId(), recipientId);

        ViewingRequest viewingRequest = null;
        if (viewingRequestId != null) {
            viewingRequest = viewingRequestRepository.findById(viewingRequestId).orElse(null);
        }

        ChatMessage message = ChatMessage.builder()
                .conversationId(conversationId)
                .sender(sender)
                .recipient(recipient)
                .content(content)
                .messageType(messageType != null ? messageType : MessageType.CHAT)
                .mediaUrl(mediaUrl)
                .viewingRequest(viewingRequest)
                .isRead(false)
                .timestamp(LocalDateTime.now())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        // Prepare STOMP payload
        Map<String, Object> payload = toMessagePayload(saved);

        try {
            // Push to conversation topic
            messagingTemplate.convertAndSend("/topic/messages/" + conversationId, payload);
            logger.info("Delivered chat message to /topic/messages/{}", conversationId);
        } catch (Exception e) {
            logger.warn("Could not broadcast message via STOMP: {}", e.getMessage());
        }

        // Send push notification if recipient is not in room
        notificationService.sendNotification(
                recipient,
                "New message from " + sender.getFullName(),
                content.length() > 60 ? content.substring(0, 57) + "..." : content,
                "CHAT_MESSAGE",
                "chat.html?conversation=" + conversationId
        );

        return saved;
    }

    public List<ChatMessage> getConversationHistory(String conversationId) {
        return chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    @Transactional
    public void markConversationAsRead(String conversationId, Long currentUserId) {
        List<ChatMessage> unread = chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
        for (ChatMessage msg : unread) {
            if (msg.getRecipient().getId().equals(currentUserId) && Boolean.FALSE.equals(msg.getIsRead())) {
                msg.setIsRead(true);
                chatMessageRepository.save(msg);
            }
        }
    }

    /**
     * Creates viewing request from schedule dialog / chat
     */
    @Transactional
    public ViewingRequest scheduleViewing(
            User tenant,
            Long propertyId,
            LocalDate date,
            String time,
            String notes) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));

        User landlord = property.getLandlord();

        ViewingRequest request = ViewingRequest.builder()
                .property(property)
                .tenant(tenant)
                .landlord(landlord)
                .viewingDate(date)
                .viewingTime(time)
                .notes(notes)
                .status(ViewingStatus.REQUESTED)
                .build();

        ViewingRequest saved = viewingRequestRepository.save(request);

        // Automatically inject card into the chat between tenant and landlord
        String text = String.format("📅 Viewing Request for %s on %s at %s. %s",
                property.getTitle(), date.toString(), time, notes != null ? "Note: " + notes : "");

        sendMessage(tenant, landlord.getId(), text, MessageType.VIEWING_REQUEST, null, saved.getId());

        return saved;
    }

    /**
     * Respond to viewing request (Accept / Decline / Reschedule)
     */
    @Transactional
    public ViewingRequest respondToViewing(User respondent, Long requestId, ViewingStatus newStatus, String comment) {
        ViewingRequest request = viewingRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Viewing request not found: " + requestId));

        request.setStatus(newStatus);
        ViewingRequest saved = viewingRequestRepository.save(request);

        String text = String.format("Viewing request for %s was %s. %s",
                request.getProperty().getTitle(), newStatus.name(), comment != null ? comment : "");

        // Determine recipient (the other party)
        Long targetUserId = respondent.getId().equals(request.getTenant().getId())
                ? request.getLandlord().getId()
                : request.getTenant().getId();

        sendMessage(respondent, targetUserId, text, MessageType.VIEWING_STATUS_UPDATE, null, saved.getId());

        notificationService.sendNotification(
                respondent.getId().equals(request.getTenant().getId()) ? request.getLandlord() : request.getTenant(),
                "Viewing " + newStatus.name(),
                text,
                "VIEWING_UPDATE",
                "chat.html"
        );

        return saved;
    }

    public Map<String, Object> toMessagePayload(ChatMessage msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", msg.getId());
        map.put("conversationId", msg.getConversationId());
        map.put("senderId", msg.getSender().getId());
        map.put("senderName", msg.getSender().getFullName());
        map.put("senderAvatar", msg.getSender().getAvatarUrl());
        map.put("recipientId", msg.getRecipient().getId());
        map.put("content", msg.getContent());
        map.put("messageType", msg.getMessageType().name());
        map.put("mediaUrl", msg.getMediaUrl());
        map.put("isRead", msg.getIsRead());
        map.put("timestamp", msg.getTimestamp().toString());

        if (msg.getViewingRequest() != null) {
            Map<String, Object> vMap = new HashMap<>();
            vMap.put("id", msg.getViewingRequest().getId());
            vMap.put("propertyId", msg.getViewingRequest().getProperty().getId());
            vMap.put("propertyTitle", msg.getViewingRequest().getProperty().getTitle());
            vMap.put("date", msg.getViewingRequest().getViewingDate().toString());
            vMap.put("time", msg.getViewingRequest().getViewingTime());
            vMap.put("status", msg.getViewingRequest().getStatus().name());
            map.put("viewingRequest", vMap);
        }

        return map;
    }
}
