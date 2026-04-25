package com.hospital.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    private Long id;
    private Long departmentId;
    private String name;
    private String title;
    private Integer maxDailyPatients;
    private Integer status;
}
