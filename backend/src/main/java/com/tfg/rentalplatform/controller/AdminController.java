package com.tfg.rentalplatform.controller;

import com.tfg.rentalplatform.dto.AdminDtos;
import com.tfg.rentalplatform.entity.UserRole;
import com.tfg.rentalplatform.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/overview")
    public AdminDtos.OverviewResponse overview() {
        return adminService.overview();
    }

    @PutMapping("/users/{userId}/role")
    public void updateRole(@PathVariable Long userId, @RequestParam UserRole role) {
        adminService.updateUserRole(userId, role);
    }

    @PutMapping("/users/{userId}/active")
    public void setUserActive(@PathVariable Long userId, @RequestParam Boolean active) {
        adminService.setUserActive(userId, active);
    }

    @PutMapping("/items/{itemId}/active")
    public void setItemActive(@PathVariable Long itemId, @RequestParam Boolean active) {
        adminService.setItemActive(itemId, active);
    }

    @PutMapping("/reports/{reportId}/close")
    public void closeReport(@PathVariable Long reportId) {
        adminService.closeReport(reportId);
    }

    @PutMapping("/reports/{reportId}/pause-item")
    public void pauseReportedItem(@PathVariable Long reportId) {
        adminService.pauseReportedItem(reportId);
    }

    @PutMapping("/reports/{reportId}/reactivate-item")
    public void reactivateReportedItem(@PathVariable Long reportId) {
        adminService.reactivateReportedItem(reportId);
    }

    @PutMapping("/reports/{reportId}/block-user")
    public void blockReportedUser(@PathVariable Long reportId) {
        adminService.blockReportedUser(reportId);
    }
}
