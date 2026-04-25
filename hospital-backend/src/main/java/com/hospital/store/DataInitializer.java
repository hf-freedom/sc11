package com.hospital.store;

import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.enums.StatusEnum;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;

//@Component
public class DataInitializer {
    
    @PostConstruct
    public void init() {
        DataStore store = DataStore.getInstance();
        
        Department internalMedicine = Department.builder()
                .id(store.nextDepartmentId())
                .name("内科")
                .status(StatusEnum.ENABLED.getCode())
                .defaultFee(new BigDecimal("20.00"))
                .build();
        store.getDepartments().put(internalMedicine.getId(), internalMedicine);
        
        Department surgery = Department.builder()
                .id(store.nextDepartmentId())
                .name("外科")
                .status(StatusEnum.ENABLED.getCode())
                .defaultFee(new BigDecimal("25.00"))
                .build();
        store.getDepartments().put(surgery.getId(), surgery);
        
        Department pediatrics = Department.builder()
                .id(store.nextDepartmentId())
                .name("儿科")
                .status(StatusEnum.ENABLED.getCode())
                .defaultFee(new BigDecimal("15.00"))
                .build();
        store.getDepartments().put(pediatrics.getId(), pediatrics);
        
        Doctor doctor1 = Doctor.builder()
                .id(store.nextDoctorId())
                .departmentId(internalMedicine.getId())
                .name("张医生")
                .title("主任医师")
                .maxDailyPatients(30)
                .status(StatusEnum.ENABLED.getCode())
                .build();
        store.getDoctors().put(doctor1.getId(), doctor1);
        
        Doctor doctor2 = Doctor.builder()
                .id(store.nextDoctorId())
                .departmentId(internalMedicine.getId())
                .name("李医生")
                .title("副主任医师")
                .maxDailyPatients(25)
                .status(StatusEnum.ENABLED.getCode())
                .build();
        store.getDoctors().put(doctor2.getId(), doctor2);
        
        Doctor doctor3 = Doctor.builder()
                .id(store.nextDoctorId())
                .departmentId(surgery.getId())
                .name("王医生")
                .title("主治医师")
                .maxDailyPatients(20)
                .status(StatusEnum.ENABLED.getCode())
                .build();
        store.getDoctors().put(doctor3.getId(), doctor3);
        
        Doctor doctor4 = Doctor.builder()
                .id(store.nextDoctorId())
                .departmentId(pediatrics.getId())
                .name("刘医生")
                .title("主任医师")
                .maxDailyPatients(35)
                .status(StatusEnum.ENABLED.getCode())
                .build();
        store.getDoctors().put(doctor4.getId(), doctor4);
        
        Patient patient1 = Patient.builder()
                .id(store.nextPatientId())
                .name("测试患者1")
                .phone("13800138001")
                .insuranceType("医保")
                .isBlacklisted(false)
                .build();
        store.getPatients().put(patient1.getId(), patient1);
        
        Patient patient2 = Patient.builder()
                .id(store.nextPatientId())
                .name("测试患者2")
                .phone("13800138002")
                .insuranceType("自费")
                .isBlacklisted(false)
                .build();
        store.getPatients().put(patient2.getId(), patient2);
    }
}
