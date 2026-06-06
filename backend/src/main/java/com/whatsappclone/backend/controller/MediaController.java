package com.whatsappclone.backend.controller;

import com.whatsappclone.backend.model.Message;
import com.whatsappclone.backend.model.MessageType;
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
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Arquivo deve ser uma imagem!"));
            }

            Message message = chatService.sendMediaMessage(senderId, receiverId, file, MessageType.IMAGE);

            Map<String, String> imagePayload = new HashMap<>();
            imagePayload.put("senderId", senderId.toString());
            imagePayload.put("senderUsername", message.getSender().getName());
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
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("audio/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Arquivo deve ser um áudio!"));
            }

            Message message = chatService.sendMediaMessage(senderId, receiverId, file, MessageType.AUDIO);

            Map<String, String> audioPayload = new HashMap<>();
            audioPayload.put("senderId", senderId.toString());
            audioPayload.put("senderUsername", message.getSender().getName());
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

    @PostMapping("/document")
    public ResponseEntity<?> sendDocument(
            @RequestParam("senderId") Long senderId,
            @RequestParam("receiverId") Long receiverId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            String contentType = file.getContentType();
            String originalFilename = file.getOriginalFilename();

            if (!isValidDocument(contentType, originalFilename)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Tipo não suportado! Use PDF, Word, Excel, PowerPoint ou TXT."
                ));
            }

            Message message = chatService.sendMediaMessage(senderId, receiverId, file, MessageType.DOCUMENT);

            Map<String, String> payload = new HashMap<>();
            payload.put("senderId", senderId.toString());
            payload.put("senderUsername", message.getSender().getName());
            payload.put("type", "DOCUMENT");
            payload.put("mediaUrl", message.getMediaUrl());
            payload.put("fileName", originalFilename != null ? originalFilename : "documento");
            payload.put("sentAt", message.getSentAt().toString());

            messagingTemplate.convertAndSend("/topic/messages/" + receiverId, payload);

            return ResponseEntity.ok(Map.of(
                "mediaUrl", message.getMediaUrl(),
                "fileName", originalFilename != null ? originalFilename : "documento",
                "message", "Documento enviado com sucesso!"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private boolean isValidDocument(String contentType, String filename) {
        if (contentType == null && filename == null) return false;

        String[] validTypes = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain"
        };

        if (contentType != null) {
            for (String type : validTypes) {
                if (type.equals(contentType)) return true;
            }
        }

        if (filename != null) {
            String lower = filename.toLowerCase();
            return lower.endsWith(".pdf") || lower.endsWith(".doc") ||
                   lower.endsWith(".docx") || lower.endsWith(".xls") ||
                   lower.endsWith(".xlsx") || lower.endsWith(".ppt") ||
                   lower.endsWith(".pptx") || lower.endsWith(".txt");
        }

        return false;
    }
}