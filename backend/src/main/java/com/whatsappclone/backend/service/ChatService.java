package com.whatsappclone.backend.service;

import com.whatsappclone.backend.model.Message;
import com.whatsappclone.backend.model.User;
import com.whatsappclone.backend.repository.MessageRepository;
import com.whatsappclone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public Message sendMessage(Long senderId, Long receiverId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Remetente não encontrado!"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Destinatário não encontrado!"));

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);

        return messageRepository.save(message);
    }

    public List<Message> getHistory(Long senderId, Long receiverId) {
        return messageRepository
            .findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderBySentAtAsc(
                senderId, receiverId, receiverId, senderId
            );
    }

    public List<Message> getRecentConversations(Long userId) {
        return messageRepository.findRecentConversations(userId);
    }
}
