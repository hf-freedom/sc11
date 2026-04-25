package com.hospital.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private Long slotId;
    private LocalDate appointmentDate;
    private String timePeriod;
    private Integer status;
    private BigDecimal totalAmount;
    private BigDecimal insuranceDiscount;
    private BigDecimal payableAmount;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime expireTime;
}
