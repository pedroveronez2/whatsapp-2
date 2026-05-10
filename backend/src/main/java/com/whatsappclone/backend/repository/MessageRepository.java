package com.whatsappclone.backend.repository;

import com.whatsappclone.backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderBySentAtAsc(
        Long senderId, Long receiverId,
        Long receiverId2, Long senderId2
    );

    @Query("""
        SELECT m FROM Message m
        WHERE (m.sender.id = :userId OR m.receiver.id = :userId)
        AND m.sentAt = (
            SELECT MAX(m2.sentAt) FROM Message m2
            WHERE (m2.sender.id = m.sender.id AND m2.receiver.id = m.receiver.id)
            OR (m2.sender.id = m.receiver.id AND m2.receiver.id = m.sender.id)
        )
        ORDER BY m.sentAt DESC
    """)
    List<Message> findRecentConversations(@Param("userId") Long userId);
}