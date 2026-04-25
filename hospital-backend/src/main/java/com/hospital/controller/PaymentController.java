package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Payment;
import com.hospital.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @GetMapping
    public Result<List<Payment>> getAllPayments() {
        return Result.success(paymentService.getAllPayments());
    }
    
    @GetMapping("/{id}")
    public Result<Payment> getPaymentById(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);
        if (payment == null) {
            return Result.error("支付记录不存在");
        }
        return Result.success(payment);
    }
    
    @GetMapping("/appointment/{appointmentId}")
    public Result<Payment> getPaymentByAppointmentId(@PathVariable Long appointmentId) {
        Payment payment = paymentService.getPaymentByAppointmentId(appointmentId);
        if (payment == null) {
            return Result.error("支付记录不存在");
        }
        return Result.success(payment);
    }
    
    @PostMapping
    public Result<Payment> createPayment(@RequestParam Long appointmentId) {
        try {
            Payment payment = paymentService.createPayment(appointmentId);
            return Result.success(payment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/pay")
    public Result<Payment> processPayment(@PathVariable Long id, @RequestParam(required = false) String paymentMethod) {
        try {
            if (paymentMethod == null) {
                paymentMethod = "微信支付";
            }
            Payment payment = paymentService.processPayment(id, paymentMethod);
            return Result.success(payment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/appointment/{appointmentId}/pay")
    public Result<Payment> payAppointment(@PathVariable Long appointmentId, 
                                            @RequestParam(required = false) String paymentMethod) {
        try {
            if (paymentMethod == null) {
                paymentMethod = "微信支付";
            }
            Payment payment = paymentService.payAppointment(appointmentId, paymentMethod);
            return Result.success(payment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
