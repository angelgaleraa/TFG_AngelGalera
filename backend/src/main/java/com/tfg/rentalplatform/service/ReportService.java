package com.tfg.rentalplatform.service;

import com.tfg.rentalplatform.dto.ReportDtos;
import com.tfg.rentalplatform.entity.*;
import com.tfg.rentalplatform.exception.ApiException;
import com.tfg.rentalplatform.repository.ItemRepository;
import com.tfg.rentalplatform.repository.ReportRepository;
import com.tfg.rentalplatform.repository.UserRepository;
import com.tfg.rentalplatform.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final NotificationService notificationService;

    @Transactional
    public ReportDtos.ReportResponse create(AuthenticatedUser currentUser, ReportDtos.CreateReportRequest request) {
        User reporter = userRepository.findById(currentUser.id())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario no encontrado"));

        Report report = new Report();
        report.setReporter(reporter);
        report.setType(request.type());
        report.setReason(request.reason().trim());
        report.setDetails(request.details() == null ? "" : request.details().trim());

        if (request.type() == ReportType.ITEM) {
            if (request.itemId() == null) {
                throw ApiException.badRequest("REPORT_ITEM_REQUIRED", "Selecciona el producto que quieres reportar");
            }
            Item item = itemRepository.findById(request.itemId())
                    .orElseThrow(() -> ApiException.notFound("ITEM_NOT_FOUND", "Objeto no encontrado"));
            if (item.getOwner().getId().equals(currentUser.id())) {
                throw ApiException.badRequest("REPORT_OWN_ITEM_NOT_ALLOWED", "No puedes reportar tu propio objeto");
            }
            report.setItem(item);
        } else {
            if (request.userId() == null) {
                throw ApiException.badRequest("REPORT_USER_REQUIRED", "Selecciona el usuario que quieres reportar");
            }
            if (request.userId().equals(currentUser.id())) {
                throw ApiException.badRequest("REPORT_SELF_NOT_ALLOWED", "No puedes reportarte a ti mismo");
            }
            report.setReportedUser(userRepository.findById(request.userId())
                    .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "Usuario no encontrado")));
        }

        Report saved = reportRepository.save(report);
        userRepository.findAll().stream()
                .filter(user -> user.getRole() == UserRole.ADMIN)
                .forEach(admin -> notificationService.create(admin.getId(), NotificationType.ADMIN_ALERT,
                        "Nuevo reporte de " + reporter.getName() + ": " + reportSummary(saved)));
        return ReportDtos.ReportResponse.from(saved);
    }

    private String reportSummary(Report report) {
        if (report.getType() == ReportType.ITEM && report.getItem() != null) {
            return "producto '" + report.getItem().getTitle() + "'";
        }
        if (report.getReportedUser() != null) {
            return "usuario " + report.getReportedUser().getName();
        }
        return "contenido";
    }
}
