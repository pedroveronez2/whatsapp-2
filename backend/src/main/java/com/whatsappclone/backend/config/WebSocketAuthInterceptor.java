package com.whatsappclone.backend.config;

import com.whatsappclone.backend.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Token JWT ausente!");
            }

            token = token.substring(7);

            if (!jwtService.isTokenValid(token)) {
                throw new IllegalArgumentException("Token JWT inválido!");
            }

            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);

            accessor.setUser(() -> username);
            accessor.getSessionAttributes().put("userId", userId);
            accessor.getSessionAttributes().put("username", username);
        }

        return message;
    }
}