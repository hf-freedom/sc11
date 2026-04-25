package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.enums.AppointmentStatusEnum;
import com.hospital.enums.PaymentStatusEnum;
import com.hospital.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RefundService {
    
    private final DataStore dataStore = DataStore.getInstance();
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private SlotService slotService;
    
    @Autowired
    private WaitingQueueService waitingQueueService;
    
    @Autowired
    private PaymentService paymentService;
    
    public List<Refund> getAllRefunds() {
        Map<Long, Refund> refunds = dataStore.getRefunds();
        return new ArrayList<>(refunds.values());
    }
    
    public Refund getRefundById(Long id) {
        return dataStore.getRefunds().get(id);
    }
    
    public Refund getRefundByAppointmentId(Long appointmentId) {
        return dataStore.getRefunds().values().stream()
                .filter(r -> appointmentId.equals(r.getAppointmentId()))
                .findFirst()
                .orElse(null);
    }
    
    public Refund processRefund(Long appointmentId, String reason) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        if (appointment == null) {
            throw new IllegalArgumentException("预约不存在");
        }
        
        if (!AppointmentStatusEnum.PAID.getCode().equals(appointment.getStatus())) {
            throw new IllegalArgumentException("预约状态不是已支付，无法退款");
        }
        
        Refund existingRefund = getRefundByAppointmentId(appointmentId);
        if (existingRefund != null) {
            return existingRefund;
        }
        
        BigDecimal refundRatio = calculateRefundRatio(appointment);
        BigDecimal refundAmount = appointment.getPayableAmount().multiply(refundRatio)
                .setScale(2, RoundingMode.HALF_UP);
        
        Refund refund = Refund.builder()
                .id(dataStore.nextRefundId())
                .appointmentId(appointmentId)
                .refundAmount(refundAmount)
                .refundRatio(refundRatio)
                .reason(reason)
                .status(1)
                .createTime(LocalDateTime.now())
                .completeTime(LocalDateTime.now())
                .build();
        
        dataStore.getRefunds().put(refund.getId(), refund);
        
        appointment.setStatus(AppointmentStatusEnum.REFUNDED.getCode());
        
        Payment payment = paymentService.getPaymentByAppointmentId(appointmentId);
        if (payment != null) {
            payment.setStatus(PaymentStatusEnum.REFUNDED.getCode());
        }
        
        slotService.increaseAvailableCount(appointment.getSlotId());
        
        waitingQueueService.removeFromQueue(appointmentId);
        
        return refund;
    }
    
    private BigDecimal calculateRefundRatio(Appointment appointment) {
        LocalDate appointmentDate = appointment.getAppointmentDate();
        String timePeriod = appointment.getTimePeriod();
        
        LocalTime appointmentTime;
        if ("morning".equals(timePeriod)) {
            appointmentTime = LocalTime.of(8, 0);
        } else if ("afternoon".equals(timePeriod)) {
            appointmentTime = LocalTime.of(14, 0);
        } else {
            appointmentTime = LocalTime.of(18, 0);
        }
        
        LocalDateTime appointmentDateTime = LocalDateTime.of(appointmentDate, appointmentTime);
        LocalDateTime now = LocalDateTime.now();
        
        long hoursUntilAppointment = java.time.Duration.between(now, appointmentDateTime).toHours();
        
        if (hoursUntilAppointment >= 24) {
            return new BigDecimal("1.00");
        } else if (hoursUntilAppointment >= 12) {
            return new BigDecimal("0.80");
        } else if (hoursUntilAppointment >= 4) {
            return new BigDecimal("0.50");
        } else {
            return BigDecimal.ZERO;
        }
    }
}
