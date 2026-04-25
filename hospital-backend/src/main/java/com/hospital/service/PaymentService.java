package com.hospital.service;

import com.hospital.entity.Appointment;
import com.hospital.entity.Payment;
import com.hospital.entity.WaitingQueue;
import com.hospital.enums.AppointmentStatusEnum;
import com.hospital.enums.PaymentStatusEnum;
import com.hospital.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {
    
    private final DataStore dataStore = DataStore.getInstance();
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private WaitingQueueService waitingQueueService;
    
    public List<Payment> getAllPayments() {
        Map<Long, Payment> payments = dataStore.getPayments();
        return new ArrayList<>(payments.values());
    }
    
    public Payment getPaymentById(Long id) {
        return dataStore.getPayments().get(id);
    }
    
    public Payment getPaymentByAppointmentId(Long appointmentId) {
        return dataStore.getPayments().values().stream()
                .filter(p -> appointmentId.equals(p.getAppointmentId()))
                .findFirst()
                .orElse(null);
    }
    
    public Payment createPayment(Long appointmentId) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        if (appointment == null) {
            throw new IllegalArgumentException("预约不存在");
        }
        
        if (!AppointmentStatusEnum.PENDING_PAYMENT.getCode().equals(appointment.getStatus())) {
            throw new IllegalArgumentException("预约状态不是待支付");
        }
        
        Payment existingPayment = getPaymentByAppointmentId(appointmentId);
        if (existingPayment != null && PaymentStatusEnum.PENDING.getCode().equals(existingPayment.getStatus())) {
            return existingPayment;
        }
        
        Payment payment = Payment.builder()
                .id(dataStore.nextPaymentId())
                .appointmentId(appointmentId)
                .amount(appointment.getPayableAmount())
                .status(PaymentStatusEnum.PENDING.getCode())
                .createTime(LocalDateTime.now())
                .build();
        
        dataStore.getPayments().put(payment.getId(), payment);
        return payment;
    }
    
    public Payment processPayment(Long paymentId, String paymentMethod) {
        Payment payment = dataStore.getPayments().get(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("支付记录不存在");
        }
        
        if (!PaymentStatusEnum.PENDING.getCode().equals(payment.getStatus())) {
            throw new IllegalArgumentException("支付状态不是待支付");
        }
        
        Appointment appointment = appointmentService.getAppointmentById(payment.getAppointmentId());
        if (appointment == null) {
            throw new IllegalArgumentException("预约不存在");
        }
        
        if (!AppointmentStatusEnum.PENDING_PAYMENT.getCode().equals(appointment.getStatus())) {
            throw new IllegalArgumentException("预约状态已变更");
        }
        
        payment.setStatus(PaymentStatusEnum.PAID.getCode());
        payment.setPaymentMethod(paymentMethod);
        payment.setPayTime(LocalDateTime.now());
        
        appointment.setStatus(AppointmentStatusEnum.PAID.getCode());
        appointment.setPayTime(LocalDateTime.now());
        
        waitingQueueService.addToQueue(appointment.getId());
        
        return payment;
    }
    
    public Payment payAppointment(Long appointmentId, String paymentMethod) {
        Payment payment = createPayment(appointmentId);
        return processPayment(payment.getId(), paymentMethod);
    }
}
