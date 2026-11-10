package com.amoney.rentify_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WebRtcSignalingService {

    private static final Logger logger = LoggerFactory.getLogger(WebRtcSignalingService.class);

    private final SimpMessagingTemplate messagingTemplate;

    @Value("${rentify.webrtc.ice-servers:stun:stun.l.google.com:19302}")
    private String iceServersConfig;

    public WebRtcSignalingService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public List<Map<String, String>> getIceServers() {
        return Arrays.stream(iceServersConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    Map<String, String> server = new HashMap<>();
                    server.put("urls", s);
                    return server;
                })
                .toList();
    }

    public void processSignal(Map<String, Object> signalPayload) {
        String callId = (String) signalPayload.get("callId");
        String targetUserId = signalPayload.get("targetUserId") != null ? signalPayload.get("targetUserId").toString() : null;
        String type = (String) signalPayload.get("type"); // "offer", "answer", "candidate", "hangup", "screenshare"

        logger.info("Relaying WebRTC signal [{}] for callId: {}, targetUser: {}", type, callId, targetUserId);

        // Broadcast to call channel
        if (callId != null && !callId.isBlank()) {
            messagingTemplate.convertAndSend("/topic/call/" + callId, signalPayload);
        }

        // Also route directly to target user if specified
        if (targetUserId != null && !targetUserId.isBlank()) {
            messagingTemplate.convertAndSend("/topic/webrtc/" + targetUserId, signalPayload);
        }
    }
}
