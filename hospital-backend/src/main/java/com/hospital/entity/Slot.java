package com.hospital.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Slot {
    private Long id;
    private Long doctorId;
    private LocalDate slotDate;
    private String timePeriod;
    private Integer totalCount;
    private Integer availableCount;
    private Integer status;
}
