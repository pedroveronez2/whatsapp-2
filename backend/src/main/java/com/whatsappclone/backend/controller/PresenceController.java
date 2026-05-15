package com.whatsappclone.backend.controller;

import com.whatsappclone.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PresenceController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userIdStr = accessor.getFirstNativeHeader("userId");

        if (userIdStr != null) {
            Long userId = Long.parseLong(userIdStr);
            userService.findById(userId).ifPresent(u -> {
                u.setOnline(true);
                messagingTemplate.convertAndSend("/topic/presence",
                    (Object) Map.of(
                        "userId", userId.toString(),
                        "name", u.getName(),
                        "phone", u.getPhone(),
                        "online", "true"
                    )
                );
            });
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userIdStr = accessor.getFirstNativeHeader("userId");

        if (userIdStr != null) {
            Long userId = Long.parseLong(userIdStr);
            userService.findById(userId).ifPresent(u -> {
                userService.logout(userId);
                messagingTemplate.convertAndSend("/topic/presence",
                    (Object) Map.of(
                        "userId", userId.toString(),
                        "name", u.getName(),
                        "phone", u.getPhone(),
                        "online", "false"
                    )
                );
            });
        }
    }

    @MessageMapping("/presence")
    public void updatePresence(@Payload Map<String, String> payload) {
        Long userId = Long.parseLong(payload.get("userId"));
        String online = payload.get("online");

        userService.findById(userId).ifPresent(u -> {
            if (online.equals("true")) {
                userService.login(u.getPhone(), u.getPassword());
            } else {
                userService.logout(userId);
            }

            messagingTemplate.convertAndSend("/topic/presence",
                (Object) Map.of(
                    "userId", userId.toString(),
                    "name", u.getName(),
                    "phone", u.getPhone(),
                    "online", online
                )
            );
        });
    }
}