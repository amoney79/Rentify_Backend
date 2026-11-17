package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversationIdOrderByTimestampAsc(String conversationId);

    List<ChatMessage> findBySenderIdOrRecipientId(Long senderId, Long recipientId);

    long countByRecipientIdAndIsReadFalse(Long recipientId);
}
