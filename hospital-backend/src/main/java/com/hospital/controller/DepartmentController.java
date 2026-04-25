package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Department;
import com.hospital.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    
    @Autowired
    private DepartmentService departmentService;
    
    @GetMapping
    public Result<List<Department>> getAllDepartments() {
        return Result.success(departmentService.getAllDepartments());
    }
    
    @GetMapping("/{id}")
    public Result<Department> getDepartmentById(@PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id);
        if (department == null) {
            return Result.error("科室不存在");
        }
        return Result.success(department);
    }
    
    @PostMapping
    public Result<Department> createDepartment(@RequestParam String name,
                                                 @RequestParam BigDecimal defaultFee) {
        try {
            Department department = departmentService.createDepartment(name, defaultFee);
            return Result.success(department);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public Result<Department> updateDepartment(@PathVariable Long id,
                                                 @RequestParam(required = false) String name,
                                                 @RequestParam(required = false) BigDecimal defaultFee,
                                                 @RequestParam(required = false) Integer status) {
        Department department = departmentService.updateDepartment(id, name, defaultFee, status);
        if (department == null) {
            return Result.error("科室不存在");
        }
        return Result.success(department);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        if (departmentService.deleteDepartment(id)) {
            return Result.success();
        }
        return Result.error("科室不存在");
    }
}
