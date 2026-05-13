package com.tfg.rentalplatform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.rentalplatform.dto.ChatDtos;
import com.tfg.rentalplatform.dto.NotificationDtos;
import com.tfg.rentalplatform.dto.ReservationDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class ChatRealtimeService {

    private final ObjectMapper objectMapper;
    private final Map<Long, List<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();

    public void register(Long userId, WebSocketSession session) {
        sessionsByUser.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).add(session);
    }

    public void unregister(Long userId, WebSocketSession session) {
        List<WebSocketSession> sessions = sessionsByUser.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByUser.remove(userId);
        }
    }

    public void sendToUser(Long userId, ChatDtos.MessageResponse message) {
        sendEventToUser(userId, "CHAT_MESSAGE", message);
    }

    public void sendReservationUpdateToUser(Long userId, ReservationDtos.ReservationResponse reservation) {
        sendEventToUser(userId, "RESERVATION_UPDATED", reservation);
    }

    public void sendNotificationToUser(Long userId, NotificationDtos.NotificationResponse notification) {
        sendEventToUser(userId, "NOTIFICATION_CREATED", notification);
    }

    private void sendEventToUser(Long userId, String type, Object payloadObject) {
        List<WebSocketSession> sessions = sessionsByUser.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "type", type,
                    "payload", payloadObject
            ));
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(payload));
                }
            }
        } catch (IOException ignored) {
            // Best effort realtime delivery; persisted messages remain available through REST.
        }
    }
}
