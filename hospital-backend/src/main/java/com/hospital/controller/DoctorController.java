package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Doctor;
import com.hospital.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    
    @Autowired
    private DoctorService doctorService;
    
    @GetMapping
    public Result<List<Doctor>> getAllDoctors() {
        return Result.success(doctorService.getAllDoctors());
    }
    
    @GetMapping("/department/{departmentId}")
    public Result<List<Doctor>> getDoctorsByDepartment(@PathVariable Long departmentId) {
        return Result.success(doctorService.getDoctorsByDepartment(departmentId));
    }
    
    @GetMapping("/{id}")
    public Result<Doctor> getDoctorById(@PathVariable Long id) {
        Doctor doctor = doctorService.getDoctorById(id);
        if (doctor == null) {
            return Result.error("医生不存在");
        }
        return Result.success(doctor);
    }
    
    @PostMapping
    public Result<Doctor> createDoctor(@RequestBody Doctor doctor) {
        try {
            Doctor created = doctorService.createDoctor(doctor);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public Result<Doctor> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctor) {
        Doctor updated = doctorService.updateDoctor(id, doctor);
        if (updated == null) {
            return Result.error("医生不存在");
        }
        return Result.success(updated);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteDoctor(@PathVariable Long id) {
        if (doctorService.deleteDoctor(id)) {
            return Result.success();
        }
        return Result.error("医生不存在");
    }
}
