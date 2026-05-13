package com.tfg.rentalplatform.dto;

import com.tfg.rentalplatform.entity.Report;
import com.tfg.rentalplatform.entity.ReportStatus;
import com.tfg.rentalplatform.entity.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ReportDtos {

    public record CreateReportRequest(
            @NotNull ReportType type,
            Long itemId,
            Long userId,
            @NotBlank @Size(max = 120) String reason,
            @Size(max = 500) String details
    ) {}

    public record ReportResponse(
            Long id,
            ReportType type,
            ReportStatus status,
            Long reporterId,
            String reporterName,
            Long itemId,
            String itemTitle,
            Long itemOwnerId,
            String itemOwnerName,
            Boolean itemActive,
            Boolean itemModerationBlocked,
            Long userId,
            String userName,
            Boolean userActive,
            String reason,
            String details,
            LocalDateTime createdAt
    ) {
        public static ReportResponse from(Report report) {
            return new ReportResponse(
                    report.getId(),
                    report.getType(),
                    report.getStatus(),
                    report.getReporter().getId(),
                    report.getReporter().getName(),
                    report.getItem() == null ? null : report.getItem().getId(),
                    report.getItem() == null ? null : report.getItem().getTitle(),
                    report.getItem() == null ? null : report.getItem().getOwner().getId(),
                    report.getItem() == null ? null : report.getItem().getOwner().getName(),
                    report.getItem() == null ? null : report.getItem().getActive(),
                    report.getItem() == null ? null : report.getItem().getModerationBlocked(),
                    report.getReportedUser() == null ? null : report.getReportedUser().getId(),
                    report.getReportedUser() == null ? null : report.getReportedUser().getName(),
                    report.getReportedUser() == null ? null : report.getReportedUser().getActive(),
                    report.getReason(),
                    report.getDetails(),
                    report.getCreatedAt()
            );
        }
    }
}
