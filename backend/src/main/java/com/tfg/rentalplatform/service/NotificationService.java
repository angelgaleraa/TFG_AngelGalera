package com.tfg.rentalplatform.service;

import com.tfg.rentalplatform.dto.NotificationDtos;
import com.tfg.rentalplatform.entity.Notification;
import com.tfg.rentalplatform.entity.NotificationType;
import com.tfg.rentalplatform.entity.User;
import com.tfg.rentalplatform.exception.ApiException;
import com.tfg.rentalplatform.repository.NotificationRepository;
import com.tfg.rentalplatform.repository.UserRepository;
import com.tfg.rentalplatform.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ChatRealtimeService chatRealtimeService;

    @Transactional
    public void create(Long userId, NotificationType type, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario no encontrado"));
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        Notification saved = notificationRepository.save(notification);
        chatRealtimeService.sendNotificationToUser(userId, NotificationDtos.NotificationResponse.from(saved));
    }

    @Transactional(readOnly = true)
    public List<NotificationDtos.NotificationResponse> myNotifications(AuthenticatedUser currentUser) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.id())
                .stream()
                .map(NotificationDtos.NotificationResponse::from)
                .toList();
    }

    @Transactional
    public NotificationDtos.NotificationResponse markAsRead(AuthenticatedUser currentUser, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> ApiException.notFound("NOTIFICATION_NOT_FOUND", "Notificacion no encontrada"));
        if (!notification.getUser().getId().equals(currentUser.id())) {
            throw ApiException.forbidden("NOTIFICATION_FORBIDDEN", "No puedes modificar esta notificación");
        }
        notification.setRead(true);
        return NotificationDtos.NotificationResponse.from(notification);
    }

    @Transactional
    public void markAllAsRead(AuthenticatedUser currentUser) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.id());
        notifications.forEach(n -> n.setRead(true));
    }
}
