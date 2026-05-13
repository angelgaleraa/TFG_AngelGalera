package com.tfg.rentalplatform.service;

import com.tfg.rentalplatform.dto.AdminDtos;
import com.tfg.rentalplatform.entity.PaymentStatus;
import com.tfg.rentalplatform.entity.ReportStatus;
import com.tfg.rentalplatform.entity.Item;
import com.tfg.rentalplatform.entity.NotificationType;
import com.tfg.rentalplatform.entity.Report;
import com.tfg.rentalplatform.entity.User;
import com.tfg.rentalplatform.entity.UserRole;
import com.tfg.rentalplatform.exception.ApiException;
import com.tfg.rentalplatform.repository.ItemRepository;
import com.tfg.rentalplatform.repository.NotificationRepository;
import com.tfg.rentalplatform.repository.PaymentRepository;
import com.tfg.rentalplatform.repository.ReportRepository;
import com.tfg.rentalplatform.repository.ReservationRepository;
import com.tfg.rentalplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final ReportRepository reportRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public AdminDtos.OverviewResponse overview() {
        long captured = paymentRepository.findAll().stream().filter(p -> p.getStatus() == PaymentStatus.CAPTURED).count();
        long unread = notificationRepository.findAll().stream().filter(n -> !Boolean.TRUE.equals(n.getRead())).count();
        long activeItems = itemRepository.findByActiveTrueAndModerationBlockedFalseAndOwnerRemovedFalseAndOwnerActiveTrueOrderByCreatedAtDesc().size();
        return new AdminDtos.OverviewResponse(
                userRepository.count(),
                activeItems,
                reservationRepository.count(),
                captured,
                unread,
                reportRepository.countByStatus(ReportStatus.OPEN),
                reportRepository.findTop20ByOrderByCreatedAtDesc().stream()
                        .map(com.tfg.rentalplatform.dto.ReportDtos.ReportResponse::from)
                        .toList()
        );
    }

    @Transactional
    public void updateUserRole(Long userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario no encontrado"));
        user.setRole(role);
    }

    @Transactional
    public void setUserActive(Long userId, Boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario no encontrado"));
        if (user.getRole() == UserRole.ADMIN && !Boolean.TRUE.equals(active)) {
            throw ApiException.badRequest("ADMIN_BLOCK_NOT_ALLOWED", "No puedes bloquear a otro administrador");
        }
        user.setActive(Boolean.TRUE.equals(active));
    }

    @Transactional
    public void setItemActive(Long itemId, Boolean active) {
        itemService.setActive(itemId, active);
    }

    @Transactional
    public void closeReport(Long reportId) {
        Report report = getReport(reportId);
        report.setStatus(ReportStatus.REVIEWED);
        notificationService.create(report.getReporter().getId(), NotificationType.ADMIN_ALERT,
                "Tu reporte ha sido revisado por el equipo de administracion.");
    }

    @Transactional
    public void pauseReportedItem(Long reportId) {
        Report report = getReport(reportId);
        Item item = report.getItem();
        if (item == null) {
            throw ApiException.badRequest("REPORT_ITEM_REQUIRED", "Este reporte no está asociado a un objeto");
        }
        itemService.blockByModeration(item.getId());
        report.setStatus(ReportStatus.REVIEWED);
        notificationService.create(item.getOwner().getId(), NotificationType.ADMIN_ALERT,
                "Tu objeto '" + item.getTitle() + "' ha sido pausado tras una revision de administracion.");
        notificationService.create(report.getReporter().getId(), NotificationType.ADMIN_ALERT,
                "Tu reporte sobre '" + item.getTitle() + "' ha sido revisado.");
    }

    @Transactional
    public void reactivateReportedItem(Long reportId) {
        Report report = getReport(reportId);
        Item item = report.getItem();
        if (item == null) {
            throw ApiException.badRequest("REPORT_ITEM_REQUIRED", "Este reporte no estÃ¡ asociado a un objeto");
        }
        itemService.unblockByModeration(item.getId());
        report.setStatus(ReportStatus.REVIEWED);
        notificationService.create(item.getOwner().getId(), NotificationType.ADMIN_ALERT,
                "Tu objeto '" + item.getTitle() + "' ha sido reactivado por administracion.");
    }

    @Transactional
    public void blockReportedUser(Long reportId) {
        Report report = getReport(reportId);
        User user = report.getReportedUser();
        if (user == null) {
            throw ApiException.badRequest("REPORT_USER_REQUIRED", "Este reporte no está asociado a un usuario");
        }
        if (user.getRole() == UserRole.ADMIN) {
            throw ApiException.badRequest("ADMIN_BLOCK_NOT_ALLOWED", "No puedes bloquear a otro administrador");
        }
        user.setActive(false);
        report.setStatus(ReportStatus.REVIEWED);
        notificationService.create(report.getReporter().getId(), NotificationType.ADMIN_ALERT,
                "Tu reporte sobre " + user.getName() + " ha sido revisado.");
    }

    private Report getReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> ApiException.notFound("REPORT_NOT_FOUND", "Reporte no encontrado"));
    }
}
