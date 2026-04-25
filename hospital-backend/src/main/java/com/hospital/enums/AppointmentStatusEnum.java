package com.hospital.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AppointmentStatusEnum {
    
    PENDING_PAYMENT(0, "待支付"),
    PAID(1, "已支付"),
    CANCELLED(2, "已取消"),
    REFUNDED(3, "已退款"),
    EXPIRED(4, "已过期"),
    COMPLETED(5, "已完成");
    
    private final Integer code;
    private final String desc;
}
