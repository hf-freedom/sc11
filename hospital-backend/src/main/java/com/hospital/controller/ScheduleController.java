package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Schedule;
import com.hospital.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {
    
    @Autowired
    private ScheduleService scheduleService;
    
    @GetMapping
    public Result<List<Schedule>> getAllSchedules() {
        return Result.success(scheduleService.getAllSchedules());
    }
    
    @GetMapping("/{id}")
    public Result<Schedule> getScheduleById(@PathVariable Long id) {
        Schedule schedule = scheduleService.getScheduleById(id);
        if (schedule == null) {
            return Result.error("排班不存在");
        }
        return Result.success(schedule);
    }
    
    @GetMapping("/doctor/{doctorId}")
    public Result<List<Schedule>> getSchedulesByDoctorAndDate(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(scheduleService.getSchedulesByDoctorAndDate(doctorId, date));
    }
    
    @GetMapping("/date/{date}")
    public Result<List<Schedule>> getSchedulesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(scheduleService.getSchedulesByDate(date));
    }
    
    @PostMapping
    public Result<Schedule> createOrUpdateSchedule(@RequestBody Schedule schedule) {
        try {
            Schedule created = scheduleService.createOrUpdateSchedule(schedule);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/absence")
    public Result<Void> handleDoctorAbsence(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String reason) {
        try {
            scheduleService.handleDoctorAbsence(doctorId, date, reason);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
