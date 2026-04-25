package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Patient;
import com.hospital.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    
    @Autowired
    private PatientService patientService;
    
    @GetMapping
    public Result<List<Patient>> getAllPatients() {
        return Result.success(patientService.getAllPatients());
    }
    
    @GetMapping("/{id}")
    public Result<Patient> getPatientById(@PathVariable Long id) {
        Patient patient = patientService.getPatientById(id);
        if (patient == null) {
            return Result.error("患者不存在");
        }
        return Result.success(patient);
    }
    
    @GetMapping("/phone/{phone}")
    public Result<Patient> getPatientByPhone(@PathVariable String phone) {
        Patient patient = patientService.getPatientByPhone(phone);
        if (patient == null) {
            return Result.error("患者不存在");
        }
        return Result.success(patient);
    }
    
    @PostMapping
    public Result<Patient> createPatient(@RequestBody Patient patient) {
        try {
            Patient created = patientService.createPatient(patient);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public Result<Patient> updatePatient(@PathVariable Long id, @RequestBody Patient patient) {
        Patient updated = patientService.updatePatient(id, patient);
        if (updated == null) {
            return Result.error("患者不存在");
        }
        return Result.success(updated);
    }
    
    @GetMapping("/{id}/blacklisted")
    public Result<Boolean> isBlacklisted(@PathVariable Long id) {
        return Result.success(patientService.isBlacklisted(id));
    }
}
