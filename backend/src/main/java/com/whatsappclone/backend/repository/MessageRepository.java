package com.whatsappclone.backend.repository;

import com.whatsappclone.backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderBySentAtAsc(
        Long senderId, Long receiverId,
        Long receiverId2, Long senderId2
    );
}
