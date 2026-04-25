package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Slot;
import com.hospital.service.SlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class SlotController {
    
    @Autowired
    private SlotService slotService;
    
    @GetMapping
    public Result<List<Slot>> getAllSlots() {
        return Result.success(slotService.getAllSlots());
    }
    
    @GetMapping("/{id}")
    public Result<Slot> getSlotById(@PathVariable Long id) {
        Slot slot = slotService.getSlotById(id);
        if (slot == null) {
            return Result.error("号源不存在");
        }
        return Result.success(slot);
    }
    
    @GetMapping("/doctor/{doctorId}")
    public Result<List<Slot>> getSlotsByDoctorAndDate(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(slotService.getSlotsByDoctorAndDate(doctorId, date));
    }
    
    @GetMapping("/doctor/{doctorId}/available")
    public Result<List<Slot>> getAvailableSlotsByDoctorAndDate(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(slotService.getAvailableSlotsByDoctorAndDate(doctorId, date));
    }
    
    @GetMapping("/date/{date}")
    public Result<List<Slot>> getSlotsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(slotService.getSlotsByDate(date));
    }
}
