package com.hospital.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitingQueue {
    private Long id;
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private String patientName;
    private Integer queueNumber;
    private Integer status;
    private LocalDateTime joinTime;
}
