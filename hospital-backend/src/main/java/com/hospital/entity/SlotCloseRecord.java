package com.hospital.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotCloseRecord {
    private Long id;
    private Long doctorId;
    private Long slotId;
    private LocalDate slotDate;
    private String timePeriod;
    private String reason;
    private LocalDateTime createTime;
}
