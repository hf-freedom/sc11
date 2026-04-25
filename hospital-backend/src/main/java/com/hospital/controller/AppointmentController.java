package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Appointment;
import com.hospital.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @GetMapping
    public Result<List<Appointment>> getAllAppointments() {
        return Result.success(appointmentService.getAllAppointments());
    }
    
    @GetMapping("/{id}")
    public Result<Appointment> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        if (appointment == null) {
            return Result.error("预约不存在");
        }
        return Result.success(appointment);
    }
    
    @GetMapping("/patient/{patientId}")
    public Result<List<Appointment>> getAppointmentsByPatientId(@PathVariable Long patientId) {
        return Result.success(appointmentService.getAppointmentsByPatientId(patientId));
    }
    
    @GetMapping("/doctor/{doctorId}")
    public Result<List<Appointment>> getAppointmentsByDoctorId(@PathVariable Long doctorId) {
        return Result.success(appointmentService.getAppointmentsByDoctorId(doctorId));
    }
    
    @PostMapping
    public Result<Appointment> createAppointment(
            @RequestParam Long patientId,
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate,
            @RequestParam String timePeriod) {
        try {
            Appointment appointment = appointmentService.createAppointment(patientId, doctorId, appointmentDate, timePeriod);
            return Result.success(appointment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/check-expired")
    public Result<Void> checkExpiredAppointments() {
        appointmentService.checkExpiredAppointments();
        return Result.success();
    }
}
