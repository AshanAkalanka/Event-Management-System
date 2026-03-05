package com.event.event_management.repository;

import com.event.event_management.entity.ChatMessage;
import com.event.event_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByCustomerOrderByCreatedAtAsc(User customer);

    @Query("select distinct u from ChatMessage cm join cm.customer u order by u.id")
    List<User> findDistinctCustomers();

    @Query("SELECT DISTINCT cm.customer.id FROM ChatMessage cm WHERE cm.senderRole = 'USER' AND (cm.isRead = false OR cm.isRead IS NULL)")
    List<Long> findCustomersWithUnreadMessages();
}