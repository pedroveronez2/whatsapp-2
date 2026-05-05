package com.whatsappclone.backend.model;

import com.whatsappclone.backend.repository.MessageRepository;
import com.whatsappclone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @MessageMapping("/chat")
    public void sendMessage(@Payload Map<String, String> payload) {
        Long senderId = Long.parseLong(payload.get("senderId"));
        Long receiverId = Long.parseLong(payload.get("receiverId"));
        String content = payload.get("content");

        User sender = userRepository.findById(senderId).orElseThrow();
        User receiver = userRepository.findById(receiverId).orElseThrow();

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);

        messageRepository.save(message);

        messagingTemplate.convertAndSend(
            "/topic/messages/" + receiverId,
            (Object) Map.of(
                "senderId", senderId.toString(),
                "senderUsername", sender.getUsername(),
                "content", content,
                "sentAt", message.getSentAt().toString()
            )
        );
    }

    @GetMapping("/api/messages/{senderId}/{receiverId}")
    public List<Message> getHistory(
        @PathVariable Long senderId,
        @PathVariable Long receiverId
    ) {
        return messageRepository
            .findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderBySentAtAsc(
                senderId, receiverId, receiverId, senderId
            );
    }
}