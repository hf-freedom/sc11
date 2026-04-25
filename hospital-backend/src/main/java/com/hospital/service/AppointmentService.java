package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.enums.AppointmentStatusEnum;
import com.hospital.enums.StatusEnum;
import com.hospital.enums.TimePeriodEnum;
import com.hospital.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppointmentService {
    
    private final DataStore dataStore = DataStore.getInstance();
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private SlotService slotService;
    
    @Autowired
    private PatientService patientService;
    
    @Autowired
    private ScheduleService scheduleService;
    
    public List<Appointment> getAllAppointments() {
        Map<Long, Appointment> appointments = dataStore.getAppointments();
        return new ArrayList<>(appointments.values());
    }
    
    public Appointment getAppointmentById(Long id) {
        return dataStore.getAppointments().get(id);
    }
    
    public List<Appointment> getAppointmentsByPatientId(Long patientId) {
        return dataStore.getAppointments().values().stream()
                .filter(a -> patientId.equals(a.getPatientId()))
                .collect(Collectors.toList());
    }
    
    public List<Appointment> getAppointmentsByDoctorId(Long doctorId) {
        return dataStore.getAppointments().values().stream()
                .filter(a -> doctorId.equals(a.getDoctorId()))
                .collect(Collectors.toList());
    }
    
    public List<Appointment> getAppointmentsBySlotId(Long slotId) {
        return dataStore.getAppointments().values().stream()
                .filter(a -> slotId.equals(a.getSlotId()))
                .collect(Collectors.toList());
    }
    
    public List<Appointment> getAppointmentsByPatientAndDate(Long patientId, LocalDate date) {
        return dataStore.getAppointments().values().stream()
                .filter(a -> patientId.equals(a.getPatientId()) && date.equals(a.getAppointmentDate()))
                .collect(Collectors.toList());
    }
    
    public Appointment createAppointment(Long patientId, Long doctorId, LocalDate appointmentDate, String timePeriod) {
        if (patientService.isBlacklisted(patientId)) {
            throw new IllegalArgumentException("患者在黑名单中，无法预约");
        }
        
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor == null) {
            throw new IllegalArgumentException("医生不存在");
        }
        if (!StatusEnum.ENABLED.getCode().equals(doctor.getStatus())) {
            throw new IllegalArgumentException("医生状态不可用");
        }
        
        List<Schedule> schedules = scheduleService.getSchedulesByDoctorAndDate(doctorId, appointmentDate);
        if (schedules.isEmpty()) {
            throw new IllegalArgumentException("医生当天没有排班");
        }
        
        Schedule schedule = schedules.get(0);
        boolean isAvailable = false;
        if (TimePeriodEnum.MORNING.getCode().equals(timePeriod) && Boolean.TRUE.equals(schedule.getMorning())) {
            isAvailable = true;
        } else if (TimePeriodEnum.AFTERNOON.getCode().equals(timePeriod) && Boolean.TRUE.equals(schedule.getAfternoon())) {
            isAvailable = true;
        } else if (TimePeriodEnum.EVENING.getCode().equals(timePeriod) && Boolean.TRUE.equals(schedule.getEvening())) {
            isAvailable = true;
        }
        
        if (!isAvailable) {
            throw new IllegalArgumentException("医生该时段不出诊");
        }
        
        Slot slot = slotService.getSlotByDoctorDatePeriod(doctorId, appointmentDate, timePeriod);
        if (slot == null || slot.getAvailableCount() <= 0) {
            throw new IllegalArgumentException("该时段号源不足");
        }
        
        List<Appointment> existingAppointments = getAppointmentsByPatientAndDate(patientId, appointmentDate);
        for (Appointment existing : existingAppointments) {
            if (!AppointmentStatusEnum.CANCELLED.getCode().equals(existing.getStatus())
                    && !AppointmentStatusEnum.REFUNDED.getCode().equals(existing.getStatus())
                    && !AppointmentStatusEnum.EXPIRED.getCode().equals(existing.getStatus())) {
                Doctor existingDoctor = doctorService.getDoctorById(existing.getDoctorId());
                if (existingDoctor != null && doctor.getDepartmentId().equals(existingDoctor.getDepartmentId())) {
                    throw new IllegalArgumentException("同一个患者同一天同一科室只能预约一次");
                }
            }
        }
        
        if (!slotService.decreaseAvailableCount(slot.getId())) {
            throw new IllegalArgumentException("号源已被占用，请重试");
        }
        
        try {
            Department department = departmentService.getDepartmentById(doctor.getDepartmentId());
            BigDecimal baseFee = department != null ? department.getDefaultFee() : BigDecimal.ZERO;
            BigDecimal titleSurcharge = doctorService.getTitleSurcharge(doctor.getTitle());
            
            Patient patient = patientService.getPatientById(patientId);
            BigDecimal insuranceDiscount = BigDecimal.ZERO;
            if (patient != null && "医保".equals(patient.getInsuranceType())) {
                insuranceDiscount = baseFee.add(titleSurcharge).multiply(new BigDecimal("0.5"));
            }
            
            BigDecimal totalAmount = baseFee.add(titleSurcharge);
            BigDecimal payableAmount = totalAmount.subtract(insuranceDiscount);
            
            Appointment appointment = Appointment.builder()
                    .id(dataStore.nextAppointmentId())
                    .patientId(patientId)
                    .doctorId(doctorId)
                    .slotId(slot.getId())
                    .appointmentDate(appointmentDate)
                    .timePeriod(timePeriod)
                    .status(AppointmentStatusEnum.PENDING_PAYMENT.getCode())
                    .totalAmount(totalAmount)
                    .insuranceDiscount(insuranceDiscount)
                    .payableAmount(payableAmount)
                    .createTime(LocalDateTime.now())
                    .expireTime(LocalDateTime.now().plusMinutes(30))
                    .build();
            
            dataStore.getAppointments().put(appointment.getId(), appointment);
            return appointment;
        } catch (Exception e) {
            slotService.increaseAvailableCount(slot.getId());
            throw e;
        }
    }
    
    public void updateAppointmentStatus(Long appointmentId, Integer status) {
        Appointment appointment = dataStore.getAppointments().get(appointmentId);
        if (appointment != null) {
            appointment.setStatus(status);
        }
    }
    
    public void checkExpiredAppointments() {
        LocalDateTime now = LocalDateTime.now();
        for (Appointment appointment : dataStore.getAppointments().values()) {
            if (AppointmentStatusEnum.PENDING_PAYMENT.getCode().equals(appointment.getStatus())
                    && appointment.getExpireTime() != null
                    && now.isAfter(appointment.getExpireTime())) {
                appointment.setStatus(AppointmentStatusEnum.EXPIRED.getCode());
                slotService.increaseAvailableCount(appointment.getSlotId());
            }
        }
    }
}
