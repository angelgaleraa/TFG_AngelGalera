package com.tfg.rentalplatform.repository;

import com.tfg.rentalplatform.entity.Report;
import com.tfg.rentalplatform.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findTop20ByOrderByCreatedAtDesc();
    long countByStatus(ReportStatus status);
}
