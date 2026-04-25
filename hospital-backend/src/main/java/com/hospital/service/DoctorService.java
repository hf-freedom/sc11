package com.hospital.service;

import com.hospital.entity.Doctor;
import com.hospital.enums.StatusEnum;
import com.hospital.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    
    private final DataStore dataStore = DataStore.getInstance();
    
    @Autowired
    private DepartmentService departmentService;
    
    public List<Doctor> getAllDoctors() {
        departmentService.getAllDepartments();
        Map<Long, Doctor> doctors = dataStore.getDoctors();
        return new ArrayList<>(doctors.values());
    }
    
    public List<Doctor> getDoctorsByDepartment(Long departmentId) {
        departmentService.getAllDepartments();
        return dataStore.getDoctors().values().stream()
                .filter(d -> departmentId.equals(d.getDepartmentId()))
                .collect(Collectors.toList());
    }
    
    public Doctor getDoctorById(Long id) {
        departmentService.getAllDepartments();
        return dataStore.getDoctors().get(id);
    }
    
    public Doctor createDoctor(Doctor doctor) {
        doctor.setId(dataStore.nextDoctorId());
        if (doctor.getStatus() == null) {
            doctor.setStatus(StatusEnum.ENABLED.getCode());
        }
        dataStore.getDoctors().put(doctor.getId(), doctor);
        return doctor;
    }
    
    public Doctor updateDoctor(Long id, Doctor doctor) {
        Doctor existing = dataStore.getDoctors().get(id);
        if (existing == null) {
            return null;
        }
        if (doctor.getName() != null) {
            existing.setName(doctor.getName());
        }
        if (doctor.getDepartmentId() != null) {
            existing.setDepartmentId(doctor.getDepartmentId());
        }
        if (doctor.getTitle() != null) {
            existing.setTitle(doctor.getTitle());
        }
        if (doctor.getMaxDailyPatients() != null) {
            existing.setMaxDailyPatients(doctor.getMaxDailyPatients());
        }
        if (doctor.getStatus() != null) {
            existing.setStatus(doctor.getStatus());
        }
        return existing;
    }
    
    public boolean deleteDoctor(Long id) {
        return dataStore.getDoctors().remove(id) != null;
    }
    
    public BigDecimal getTitleSurcharge(String title) {
        switch (title) {
            case "主任医师":
                return new BigDecimal("50.00");
            case "副主任医师":
                return new BigDecimal("30.00");
            case "主治医师":
                return new BigDecimal("10.00");
            default:
                return BigDecimal.ZERO;
        }
    }
}
