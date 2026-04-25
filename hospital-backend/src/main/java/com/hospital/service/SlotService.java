package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.enums.AppointmentStatusEnum;
import com.hospital.enums.StatusEnum;
import com.hospital.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SlotService {
    
    private final DataStore dataStore = DataStore.getInstance();
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private RefundService refundService;
    
    public List<Slot> getAllSlots() {
        Map<Long, Slot> slots = dataStore.getSlots();
        return new ArrayList<>(slots.values());
    }
    
    public Slot getSlotById(Long id) {
        return dataStore.getSlots().get(id);
    }
    
    public Slot getSlotByDoctorDatePeriod(Long doctorId, LocalDate date, String timePeriod) {
        return dataStore.getSlots().values().stream()
                .filter(s -> doctorId.equals(s.getDoctorId()) 
                        && date.equals(s.getSlotDate()) 
                        && timePeriod.equals(s.getTimePeriod()))
                .findFirst()
                .orElse(null);
    }
    
    public List<Slot> getSlotsByDoctorAndDate(Long doctorId, LocalDate date) {
        return dataStore.getSlots().values().stream()
                .filter(s -> doctorId.equals(s.getDoctorId()) && date.equals(s.getSlotDate()))
                .collect(Collectors.toList());
    }
    
    public List<Slot> getSlotsByDate(LocalDate date) {
        return dataStore.getSlots().values().stream()
                .filter(s -> date.equals(s.getSlotDate()))
                .collect(Collectors.toList());
    }
    
    public List<Slot> getAvailableSlotsByDoctorAndDate(Long doctorId, LocalDate date) {
        return dataStore.getSlots().values().stream()
                .filter(s -> doctorId.equals(s.getDoctorId()) 
                        && date.equals(s.getSlotDate())
                        && StatusEnum.ENABLED.getCode().equals(s.getStatus())
                        && s.getAvailableCount() > 0)
                .collect(Collectors.toList());
    }
    
    public synchronized boolean decreaseAvailableCount(Long slotId) {
        Slot slot = dataStore.getSlots().get(slotId);
        if (slot == null || slot.getAvailableCount() <= 0) {
            return false;
        }
        slot.setAvailableCount(slot.getAvailableCount() - 1);
        return true;
    }
    
    public synchronized void increaseAvailableCount(Long slotId) {
        Slot slot = dataStore.getSlots().get(slotId);
        if (slot != null) {
            slot.setAvailableCount(slot.getAvailableCount() + 1);
        }
    }
    
    public void closeSlotsByDoctorDate(Long doctorId, LocalDate date, String reason) {
        List<Slot> slots = getSlotsByDoctorAndDate(doctorId, date);
        for (Slot slot : slots) {
            if (StatusEnum.ENABLED.getCode().equals(slot.getStatus())) {
                slot.setStatus(StatusEnum.DISABLED.getCode());
                
                SlotCloseRecord record = SlotCloseRecord.builder()
                        .id(dataStore.nextSlotCloseRecordId())
                        .doctorId(doctorId)
                        .slotId(slot.getId())
                        .slotDate(date)
                        .timePeriod(slot.getTimePeriod())
                        .reason(reason)
                        .createTime(LocalDateTime.now())
                        .build();
                dataStore.getSlotCloseRecords().put(record.getId(), record);
                
                List<Appointment> affectedAppointments = appointmentService.getAppointmentsBySlotId(slot.getId());
                for (Appointment appointment : affectedAppointments) {
                    if (AppointmentStatusEnum.PAID.getCode().equals(appointment.getStatus())) {
                        RefundTask refundTask = RefundTask.builder()
                                .id(dataStore.nextRefundTaskId())
                                .appointmentId(appointment.getId())
                                .patientId(appointment.getPatientId())
                                .refundAmount(appointment.getPayableAmount())
                                .reason("医生停诊: " + reason)
                                .status(0)
                                .createTime(LocalDateTime.now())
                                .build();
                        dataStore.getRefundTasks().put(refundTask.getId(), refundTask);
                        
                        refundService.processRefund(appointment.getId(), "医生停诊");
                    }
                }
            }
        }
    }
}
