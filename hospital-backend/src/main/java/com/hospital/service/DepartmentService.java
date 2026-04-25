package com.hospital.service;

import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.enums.StatusEnum;
import com.hospital.store.DataStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentService {
    
    private final DataStore dataStore = DataStore.getInstance();
    
    public List<Department> getAllDepartments() {
        initDataIfEmpty();
        Map<Long, Department> departments = dataStore.getDepartments();
        return new ArrayList<>(departments.values());
    }
    
    public Department getDepartmentById(Long id) {
        return dataStore.getDepartments().get(id);
    }
    
    public Department createDepartment(String name, BigDecimal defaultFee) {
        Department department = Department.builder()
                .id(dataStore.nextDepartmentId())
                .name(name)
                .status(StatusEnum.ENABLED.getCode())
                .defaultFee(defaultFee)
                .build();
        dataStore.getDepartments().put(department.getId(), department);
        return department;
    }
    
    public Department updateDepartment(Long id, String name, BigDecimal defaultFee, Integer status) {
        Department department = dataStore.getDepartments().get(id);
        if (department == null) {
            return null;
        }
        if (name != null) {
            department.setName(name);
        }
        if (defaultFee != null) {
            department.setDefaultFee(defaultFee);
        }
        if (status != null) {
            department.setStatus(status);
        }
        return department;
    }
    
    public boolean deleteDepartment(Long id) {
        return dataStore.getDepartments().remove(id) != null;
    }
    
    private void initDataIfEmpty() {
        if (dataStore.getDepartments().isEmpty()) {
            Department internalMedicine = Department.builder()
                    .id(dataStore.nextDepartmentId())
                    .name("内科")
                    .status(StatusEnum.ENABLED.getCode())
                    .defaultFee(new BigDecimal("20.00"))
                    .build();
            dataStore.getDepartments().put(internalMedicine.getId(), internalMedicine);
            
            Department surgery = Department.builder()
                    .id(dataStore.nextDepartmentId())
                    .name("外科")
                    .status(StatusEnum.ENABLED.getCode())
                    .defaultFee(new BigDecimal("25.00"))
                    .build();
            dataStore.getDepartments().put(surgery.getId(), surgery);
            
            Department pediatrics = Department.builder()
                    .id(dataStore.nextDepartmentId())
                    .name("儿科")
                    .status(StatusEnum.ENABLED.getCode())
                    .defaultFee(new BigDecimal("15.00"))
                    .build();
            dataStore.getDepartments().put(pediatrics.getId(), pediatrics);
            
            Doctor doctor1 = Doctor.builder()
                    .id(dataStore.nextDoctorId())
                    .departmentId(internalMedicine.getId())
                    .name("张医生")
                    .title("主任医师")
                    .maxDailyPatients(30)
                    .status(StatusEnum.ENABLED.getCode())
                    .build();
            dataStore.getDoctors().put(doctor1.getId(), doctor1);
            
            Doctor doctor2 = Doctor.builder()
                    .id(dataStore.nextDoctorId())
                    .departmentId(internalMedicine.getId())
                    .name("李医生")
                    .title("副主任医师")
                    .maxDailyPatients(25)
                    .status(StatusEnum.ENABLED.getCode())
                    .build();
            dataStore.getDoctors().put(doctor2.getId(), doctor2);
            
            Doctor doctor3 = Doctor.builder()
                    .id(dataStore.nextDoctorId())
                    .departmentId(surgery.getId())
                    .name("王医生")
                    .title("主治医师")
                    .maxDailyPatients(20)
                    .status(StatusEnum.ENABLED.getCode())
                    .build();
            dataStore.getDoctors().put(doctor3.getId(), doctor3);
            
            Doctor doctor4 = Doctor.builder()
                    .id(dataStore.nextDoctorId())
                    .departmentId(pediatrics.getId())
                    .name("刘医生")
                    .title("主任医师")
                    .maxDailyPatients(35)
                    .status(StatusEnum.ENABLED.getCode())
                    .build();
            dataStore.getDoctors().put(doctor4.getId(), doctor4);
        }
    }
}
