package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Refund;
import com.hospital.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {
    
    @Autowired
    private RefundService refundService;
    
    @GetMapping
    public Result<List<Refund>> getAllRefunds() {
        return Result.success(refundService.getAllRefunds());
    }
    
    @GetMapping("/{id}")
    public Result<Refund> getRefundById(@PathVariable Long id) {
        Refund refund = refundService.getRefundById(id);
        if (refund == null) {
            return Result.error("退费记录不存在");
        }
        return Result.success(refund);
    }
    
    @GetMapping("/appointment/{appointmentId}")
    public Result<Refund> getRefundByAppointmentId(@PathVariable Long appointmentId) {
        Refund refund = refundService.getRefundByAppointmentId(appointmentId);
        if (refund == null) {
            return Result.error("退费记录不存在");
        }
        return Result.success(refund);
    }
    
    @PostMapping
    public Result<Refund> processRefund(@RequestParam Long appointmentId, 
                                          @RequestParam(required = false, defaultValue = "患者主动退号") String reason) {
        try {
            Refund refund = refundService.processRefund(appointmentId, reason);
            return Result.success(refund);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
