package com.whatsappclone.backend.controller;

import com.whatsappclone.backend.model.Message;
import com.whatsappclone.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat")
    public void sendMessage(@Payload Map<String, String> payload) {
        Long senderId = Long.parseLong(payload.get("senderId"));
        Long receiverId = Long.parseLong(payload.get("receiverId"));
        String content = payload.get("content");

        Message message = chatService.sendMessage(senderId, receiverId, content);

        messagingTemplate.convertAndSend(
            "/topic/messages/" + receiverId,
            (Object) Map.of(
                "senderId", senderId.toString(),
                "senderUsername", message.getSender().getUsername(),
                "content", content,
                "sentAt", message.getSentAt().toString()
            )
        );
    }

    @GetMapping("/api/messages/{senderId}/{receiverId}")
    public ResponseEntity<List<Message>> getHistory(
        @PathVariable Long senderId,
        @PathVariable Long receiverId
    ) {
        return ResponseEntity.ok(chatService.getHistory(senderId, receiverId));
    }

    @GetMapping("/api/messages/recent/{userId}")
    public ResponseEntity<List<Message>> getRecentConversations(
        @PathVariable Long userId
    ) {
        return ResponseEntity.ok(chatService.getRecentConversations(userId));
    }
}
