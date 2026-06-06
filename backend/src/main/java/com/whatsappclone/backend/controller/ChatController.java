package com.whatsappclone.backend.controller;

import com.whatsappclone.backend.model.Message;
import com.whatsappclone.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

        Message message = chatService.sendTextMessage(senderId, receiverId, content);

        messagingTemplate.convertAndSend(
            "/topic/messages/" + receiverId,
            (Object) Map.of(
                "senderId", senderId.toString(),
                "senderUsername", message.getSender().getName(),
                "content", content,
                "type", "TEXT",
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

    @PostMapping("/api/messages/send")
    public ResponseEntity<?> sendTextMessage(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {

        // pega o senderId do token JWT, não do body!
        Long senderId = (Long) ((UsernamePasswordAuthenticationToken) authentication).getDetails();
        Long receiverId = Long.parseLong(body.get("receiverId").toString());
        String content = body.get("content").toString();

        Message message = chatService.sendTextMessage(senderId, receiverId, content);

        Map<String, String> payload = new HashMap<>();
        payload.put("senderId", senderId.toString());
        payload.put("senderUsername", message.getSender().getName());
        payload.put("content", content);
        payload.put("type", "TEXT");
        payload.put("sentAt", message.getSentAt().toString());

        messagingTemplate.convertAndSend("/topic/messages/" + receiverId, payload);

        return ResponseEntity.ok(Map.of("message", "Mensagem enviada!"));
    }
}