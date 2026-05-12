package com.tfg.rentalplatform.controller;

import com.tfg.rentalplatform.dto.NotificationDtos;
import com.tfg.rentalplatform.security.SecurityUtils;
import com.tfg.rentalplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    public List<NotificationDtos.NotificationResponse> mine() {
        return notificationService.myNotifications(securityUtils.currentUser());
    }

    @PutMapping("/{notificationId}/read")
    public NotificationDtos.NotificationResponse markRead(@PathVariable Long notificationId) {
        return notificationService.markAsRead(securityUtils.currentUser(), notificationId);
    }

    @PutMapping("/me/read-all")
    public void markAllRead() {
        notificationService.markAllAsRead(securityUtils.currentUser());
    }
}
