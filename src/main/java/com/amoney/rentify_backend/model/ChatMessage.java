package com.amoney.rentify_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDateTime;

@Document(collection = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage implements MongoIdentifiable {

    @Id
        private Long id;

        private String conversationId; // Format: min(user1, user2) + "_" + max(user1, user2)

    @DBRef
        private User sender;

    @DBRef
        private User recipient;

        private String content;

            @Builder.Default
    private MessageType messageType = MessageType.CHAT;

    private String mediaUrl;

    @DBRef
        private ViewingRequest viewingRequest;

        @Builder.Default
    private Boolean isRead = false;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
