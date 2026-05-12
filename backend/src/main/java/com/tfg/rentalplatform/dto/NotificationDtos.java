package com.tfg.rentalplatform.dto;

import com.tfg.rentalplatform.entity.Notification;
import com.tfg.rentalplatform.entity.NotificationType;

import java.time.LocalDateTime;

public class NotificationDtos {

    public record NotificationResponse(
            Long id,
            NotificationType type,
            String message,
            Boolean read,
            LocalDateTime createdAt
    ) {
        public static NotificationResponse from(Notification notification) {
            return new NotificationResponse(
                    notification.getId(),
                    notification.getType(),
                    notification.getMessage(),
                    notification.getRead(),
                    notification.getCreatedAt()
            );
        }
    }
}
