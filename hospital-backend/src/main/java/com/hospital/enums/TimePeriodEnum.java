package com.hospital.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TimePeriodEnum {
    
    MORNING("morning", "上午"),
    AFTERNOON("afternoon", "下午"),
    EVENING("evening", "晚上");
    
    private final String code;
    private final String desc;
}
