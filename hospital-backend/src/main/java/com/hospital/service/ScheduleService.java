package com.hospital.service;

import com.hospital.entity.Doctor;
import com.hospital.entity.Schedule;
import com.hospital.entity.Slot;
import com.hospital.enums.StatusEnum;
import com.hospital.enums.TimePeriodEnum;
import com.hospital.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScheduleService {
    
    private final DataStore dataStore = DataStore.getInstance();
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private SlotService slotService;
    
    public List<Schedule> getAllSchedules() {
        Map<Long, Schedule> schedules = dataStore.getSchedules();
        return new ArrayList<>(schedules.values());
    }
    
    public Schedule getScheduleById(Long id) {
        return dataStore.getSchedules().get(id);
    }
    
    public List<Schedule> getSchedulesByDoctorAndDate(Long doctorId, LocalDate date) {
        return dataStore.getSchedules().values().stream()
                .filter(s -> doctorId.equals(s.getDoctorId()) && date.equals(s.getScheduleDate()))
                .collect(Collectors.toList());
    }
    
    public List<Schedule> getSchedulesByDate(LocalDate date) {
        return dataStore.getSchedules().values().stream()
                .filter(s -> date.equals(s.getScheduleDate()))
                .collect(Collectors.toList());
    }
    
    public Schedule createOrUpdateSchedule(Schedule schedule) {
        if (schedule.getDoctorId() == null) {
            throw new IllegalArgumentException("医生ID不能为空");
        }
        Doctor doctor = doctorService.getDoctorById(schedule.getDoctorId());
        if (doctor == null) {
            throw new IllegalArgumentException("医生不存在");
        }
        if (!StatusEnum.ENABLED.getCode().equals(doctor.getStatus())) {
            throw new IllegalArgumentException("医生状态不可用");
        }
        
        List<Schedule> existing = getSchedulesByDoctorAndDate(schedule.getDoctorId(), schedule.getScheduleDate());
        
        Schedule targetSchedule;
        if (!existing.isEmpty()) {
            targetSchedule = existing.get(0);
            targetSchedule.setMorning(schedule.getMorning() != null ? schedule.getMorning() : false);
            targetSchedule.setAfternoon(schedule.getAfternoon() != null ? schedule.getAfternoon() : false);
            targetSchedule.setEvening(schedule.getEvening() != null ? schedule.getEvening() : false);
            if (schedule.getStatus() != null) {
                targetSchedule.setStatus(schedule.getStatus());
            }
        } else {
            targetSchedule = Schedule.builder()
                    .id(dataStore.nextScheduleId())
                    .doctorId(schedule.getDoctorId())
                    .scheduleDate(schedule.getScheduleDate())
                    .morning(schedule.getMorning() != null ? schedule.getMorning() : false)
                    .afternoon(schedule.getAfternoon() != null ? schedule.getAfternoon() : false)
                    .evening(schedule.getEvening() != null ? schedule.getEvening() : false)
                    .status(StatusEnum.ENABLED.getCode())
                    .build();
            dataStore.getSchedules().put(targetSchedule.getId(), targetSchedule);
        }
        
        generateSlotsFromSchedule(targetSchedule, doctor);
        
        return targetSchedule;
    }
    
    private void generateSlotsFromSchedule(Schedule schedule, Doctor doctor) {
        LocalDate date = schedule.getScheduleDate();
        Long doctorId = schedule.getDoctorId();
        int maxDailyPatients = doctor.getMaxDailyPatients();
        
        int activePeriods = 0;
        if (Boolean.TRUE.equals(schedule.getMorning())) activePeriods++;
        if (Boolean.TRUE.equals(schedule.getAfternoon())) activePeriods++;
        if (Boolean.TRUE.equals(schedule.getEvening())) activePeriods++;
        
        if (activePeriods == 0) {
            closeSlot(doctorId, date, TimePeriodEnum.MORNING.getCode());
            closeSlot(doctorId, date, TimePeriodEnum.AFTERNOON.getCode());
            closeSlot(doctorId, date, TimePeriodEnum.EVENING.getCode());
            return;
        }
        
        int baseSlots = maxDailyPatients / activePeriods;
        int extraSlots = maxDailyPatients % activePeriods;
        
        int morningSlots = 0, afternoonSlots = 0, eveningSlots = 0;
        int assigned = 0;
        
        if (Boolean.TRUE.equals(schedule.getMorning())) {
            morningSlots = baseSlots + (assigned < extraSlots ? 1 : 0);
            assigned++;
        }
        if (Boolean.TRUE.equals(schedule.getAfternoon())) {
            afternoonSlots = baseSlots + (assigned < extraSlots ? 1 : 0);
            assigned++;
        }
        if (Boolean.TRUE.equals(schedule.getEvening())) {
            eveningSlots = baseSlots + (assigned < extraSlots ? 1 : 0);
            assigned++;
        }
        
        if (Boolean.TRUE.equals(schedule.getMorning())) {
            createOrUpdateSlot(doctorId, date, TimePeriodEnum.MORNING.getCode(), morningSlots);
        } else {
            closeSlot(doctorId, date, TimePeriodEnum.MORNING.getCode());
        }
        
        if (Boolean.TRUE.equals(schedule.getAfternoon())) {
            createOrUpdateSlot(doctorId, date, TimePeriodEnum.AFTERNOON.getCode(), afternoonSlots);
        } else {
            closeSlot(doctorId, date, TimePeriodEnum.AFTERNOON.getCode());
        }
        
        if (Boolean.TRUE.equals(schedule.getEvening())) {
            createOrUpdateSlot(doctorId, date, TimePeriodEnum.EVENING.getCode(), eveningSlots);
        } else {
            closeSlot(doctorId, date, TimePeriodEnum.EVENING.getCode());
        }
    }
    
    private void createOrUpdateSlot(Long doctorId, LocalDate date, String timePeriod, int maxCount) {
        if (maxCount <= 0) {
            maxCount = 1;
        }
        
        Slot existingSlot = slotService.getSlotByDoctorDatePeriod(doctorId, date, timePeriod);
        if (existingSlot != null) {
            if (StatusEnum.DISABLED.getCode().equals(existingSlot.getStatus())) {
                existingSlot.setStatus(StatusEnum.ENABLED.getCode());
                existingSlot.setTotalCount(maxCount);
                existingSlot.setAvailableCount(maxCount);
            } else {
                int oldTotal = existingSlot.getTotalCount();
                int oldAvailable = existingSlot.getAvailableCount();
                int delta = maxCount - oldTotal;
                
                if (delta > 0) {
                    existingSlot.setTotalCount(maxCount);
                    existingSlot.setAvailableCount(oldAvailable + delta);
                } else if (delta < 0) {
                    existingSlot.setTotalCount(maxCount);
                    int newAvailable = oldAvailable + delta;
                    if (newAvailable < 0) {
                        newAvailable = 0;
                    }
                    existingSlot.setAvailableCount(newAvailable);
                }
            }
        } else {
            Slot newSlot = Slot.builder()
                    .id(dataStore.nextSlotId())
                    .doctorId(doctorId)
                    .slotDate(date)
                    .timePeriod(timePeriod)
                    .totalCount(maxCount)
                    .availableCount(maxCount)
                    .status(StatusEnum.ENABLED.getCode())
                    .build();
            dataStore.getSlots().put(newSlot.getId(), newSlot);
        }
    }
    
    private void closeSlot(Long doctorId, LocalDate date, String timePeriod) {
        Slot slot = slotService.getSlotByDoctorDatePeriod(doctorId, date, timePeriod);
        if (slot != null && StatusEnum.ENABLED.getCode().equals(slot.getStatus())) {
            slot.setStatus(StatusEnum.DISABLED.getCode());
        }
    }
    
    public void handleDoctorAbsence(Long doctorId, LocalDate date, String reason) {
        List<Schedule> schedules = getSchedulesByDoctorAndDate(doctorId, date);
        for (Schedule schedule : schedules) {
            schedule.setStatus(StatusEnum.DISABLED.getCode());
            schedule.setMorning(false);
            schedule.setAfternoon(false);
            schedule.setEvening(false);
            
            slotService.closeSlotsByDoctorDate(doctorId, date, reason);
        }
    }
}
