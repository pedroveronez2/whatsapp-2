package com.whatsappclone.backend.controller;


import com.whatsappclone.backend.model.Message;
import com.whatsappclone.backend.model.Message.MessageType;
import com.whatsappclone.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/image")
    
    public ResponseEntity<?> sendImage(
        
            @RequestParam("senderId") Long senderId,
            @RequestParam("receiverId") Long receiverId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // valida se é imagem
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                
                return ResponseEntity.badRequest().body(Map.of("error", "Arquivo deve ser uma imagem!"));
            }

            Message message = chatService.sendMediaMessage(senderId, receiverId, file, MessageType.IMAGE);

            // notifica o destinatário via WebSocket
            Map<String, String> imagePayload = new HashMap<>();
            imagePayload.put("senderId", senderId.toString());
            imagePayload.put("senderUsername", message.getSender().getUsername());
            imagePayload.put("type", "IMAGE");
            imagePayload.put("mediaUrl", message.getMediaUrl());
            imagePayload.put("sentAt", message.getSentAt().toString());

            messagingTemplate.convertAndSend("/topic/messages/" + receiverId, imagePayload);

            return ResponseEntity.ok(Map.of(
                "mediaUrl", message.getMediaUrl(),
                "message", "Imagem enviada com sucesso!"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/audio")
    public ResponseEntity<?> sendAudio(
            @RequestParam("senderId") Long senderId,
            @RequestParam("receiverId") Long receiverId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // valida se é audio
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("audio/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Arquivo deve ser um áudio!"));
            }

            Message message = chatService.sendMediaMessage(senderId, receiverId, file, MessageType.AUDIO);

            // notifica o destinatário via WebSocket
            Map<String, String> audioPayload = new HashMap<>();
            audioPayload.put("senderId", senderId.toString());
            audioPayload.put("senderUsername", message.getSender().getUsername());
            audioPayload.put("type", "AUDIO");
            audioPayload.put("mediaUrl", message.getMediaUrl());
            audioPayload.put("sentAt", message.getSentAt().toString());

            messagingTemplate.convertAndSend("/topic/messages/" + receiverId, audioPayload);

            return ResponseEntity.ok(Map.of(
                "mediaUrl", message.getMediaUrl(),
                "message", "Áudio enviado com sucesso!"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}