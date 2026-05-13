package com.tfg.rentalplatform.dto;

import com.tfg.rentalplatform.entity.UserRole;

import java.util.List;

public class AdminDtos {

    public record OverviewResponse(
            long users,
            long activeItems,
            long reservations,
            long capturedPayments,
            long unreadNotifications,
            long openReports,
            List<ReportDtos.ReportResponse> recentReports
    ) {}

    public record UpdateRoleRequest(UserRole role) {}
}
