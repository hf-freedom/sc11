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
public class Payment {
    private Long id;
    private Long appointmentId;
    private BigDecimal amount;
    private String paymentMethod;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
}
