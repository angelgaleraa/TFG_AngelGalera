package com.tfg.rentalplatform.controller;

import com.tfg.rentalplatform.dto.PaymentDtos;
import com.tfg.rentalplatform.security.SecurityUtils;
import com.tfg.rentalplatform.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    public List<PaymentDtos.PaymentResponse> myPayments() {
        return paymentService.myPayments(securityUtils.currentUser());
    }

    @GetMapping("/owner")
    public List<PaymentDtos.PaymentResponse> ownerPayments() {
        return paymentService.ownerPayments(securityUtils.currentUser());
    }
}
