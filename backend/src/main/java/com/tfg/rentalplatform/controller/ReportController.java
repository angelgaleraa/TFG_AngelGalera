package com.tfg.rentalplatform.controller;

import com.tfg.rentalplatform.dto.ReportDtos;
import com.tfg.rentalplatform.security.SecurityUtils;
import com.tfg.rentalplatform.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ReportDtos.ReportResponse create(@Valid @RequestBody ReportDtos.CreateReportRequest request) {
        return reportService.create(securityUtils.currentUser(), request);
    }
}
