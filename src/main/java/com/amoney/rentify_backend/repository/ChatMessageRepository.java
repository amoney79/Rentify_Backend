package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversationIdOrderByTimestampAsc(String conversationId);

    @Query("SELECT DISTINCT m.conversationId FROM ChatMessage m WHERE m.sender.id = :userId OR m.recipient.id = :userId")
    List<String> findConversationIdsByUserId(Long userId);

    long countByRecipientIdAndIsReadFalse(Long recipientId);
}
