package com.hospital.service;

import com.hospital.entity.Patient;
import com.hospital.store.DataStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PatientService {
    
    private final DataStore dataStore = DataStore.getInstance();
    
    public List<Patient> getAllPatients() {
        initDataIfEmpty();
        Map<Long, Patient> patients = dataStore.getPatients();
        return new ArrayList<>(patients.values());
    }
    
    public Patient getPatientById(Long id) {
        return dataStore.getPatients().get(id);
    }
    
    public Patient getPatientByPhone(String phone) {
        return dataStore.getPatients().values().stream()
                .filter(p -> phone.equals(p.getPhone()))
                .findFirst()
                .orElse(null);
    }
    
    public Patient createPatient(Patient patient) {
        Patient existing = getPatientByPhone(patient.getPhone());
        if (existing != null) {
            return existing;
        }
        patient.setId(dataStore.nextPatientId());
        if (patient.getIsBlacklisted() == null) {
            patient.setIsBlacklisted(false);
        }
        dataStore.getPatients().put(patient.getId(), patient);
        return patient;
    }
    
    public Patient updatePatient(Long id, Patient patient) {
        Patient existing = dataStore.getPatients().get(id);
        if (existing == null) {
            return null;
        }
        if (patient.getName() != null) {
            existing.setName(patient.getName());
        }
        if (patient.getPhone() != null) {
            existing.setPhone(patient.getPhone());
        }
        if (patient.getInsuranceType() != null) {
            existing.setInsuranceType(patient.getInsuranceType());
        }
        if (patient.getIsBlacklisted() != null) {
            existing.setIsBlacklisted(patient.getIsBlacklisted());
        }
        return existing;
    }
    
    public boolean isBlacklisted(Long patientId) {
        Patient patient = getPatientById(patientId);
        return patient != null && Boolean.TRUE.equals(patient.getIsBlacklisted());
    }
    
    private void initDataIfEmpty() {
        if (dataStore.getPatients().isEmpty()) {
            Patient patient1 = Patient.builder()
                    .id(dataStore.nextPatientId())
                    .name("测试患者1")
                    .phone("13800138001")
                    .insuranceType("医保")
                    .isBlacklisted(false)
                    .build();
            dataStore.getPatients().put(patient1.getId(), patient1);
            
            Patient patient2 = Patient.builder()
                    .id(dataStore.nextPatientId())
                    .name("测试患者2")
                    .phone("13800138002")
                    .insuranceType("自费")
                    .isBlacklisted(false)
                    .build();
            dataStore.getPatients().put(patient2.getId(), patient2);
        }
    }
}
