package com.tfg.rentalplatform.repository;

import com.tfg.rentalplatform.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReservationId(Long reservationId);
    List<Payment> findByPayerIdOrderByCreatedAtDesc(Long payerId);
    List<Payment> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
}
