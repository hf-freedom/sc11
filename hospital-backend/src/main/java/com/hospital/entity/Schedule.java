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
public class Schedule {
    private Long id;
    private Long doctorId;
    private LocalDate scheduleDate;
    private Boolean morning;
    private Boolean afternoon;
    private Boolean evening;
    private Integer status;
}
