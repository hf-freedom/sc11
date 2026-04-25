package com.hospital.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundTask {
    private Long id;
    private Long appointmentId;
    private Long patientId;
    private BigDecimal refundAmount;
    private String reason;
    private Integer status;
    private LocalDateTime createTime;
}
