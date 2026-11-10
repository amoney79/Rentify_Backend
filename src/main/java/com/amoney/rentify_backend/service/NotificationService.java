package com.amoney.rentify_backend.service;

import com.amoney.rentify_backend.model.Notification;
import com.amoney.rentify_backend.model.User;
import com.amoney.rentify_backend.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public Notification sendNotification(User recipient, String title, String message, String type, String actionUrl) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .type(type)
                .actionUrl(actionUrl)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", saved.getId());
        payload.put("recipientId", recipient.getId());
        payload.put("title", saved.getTitle());
        payload.put("message", saved.getMessage());
        payload.put("type", saved.getType());
        payload.put("actionUrl", saved.getActionUrl());
        payload.put("createdAt", saved.getCreatedAt().toString());

        try {
            // Push to personal queue /topic/notifications/{userId}
            messagingTemplate.convertAndSend("/topic/notifications/" + recipient.getId(), (Object) payload);
            // Push to general topic /topic/notifications
            messagingTemplate.convertAndSend("/topic/notifications", (Object) payload);
            logger.info("Dispatched WebSocket notification to user {}: {}", recipient.getId(), title);
        } catch (Exception e) {
            logger.warn("Could not dispatch WebSocket notification: {}", e.getMessage());
        }

        return saved;
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }
}
