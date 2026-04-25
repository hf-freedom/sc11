package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.WaitingQueue;
import com.hospital.service.WaitingQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/waiting-queue")
public class WaitingQueueController {
    
    @Autowired
    private WaitingQueueService waitingQueueService;
    
    @GetMapping
    public Result<List<WaitingQueue>> getAllWaitingQueues() {
        return Result.success(waitingQueueService.getAllWaitingQueues());
    }
    
    @GetMapping("/doctor/{doctorId}")
    public Result<List<WaitingQueue>> getWaitingQueueByDoctor(@PathVariable Long doctorId) {
        return Result.success(waitingQueueService.getWaitingQueueByDoctor(doctorId));
    }
    
    @GetMapping("/appointment/{appointmentId}")
    public Result<WaitingQueue> getWaitingQueueByAppointment(@PathVariable Long appointmentId) {
        WaitingQueue waitingQueue = waitingQueueService.getWaitingQueueByAppointment(appointmentId);
        if (waitingQueue == null) {
            return Result.error("候诊记录不存在");
        }
        return Result.success(waitingQueue);
    }
    
    @PostMapping("/add")
    public Result<WaitingQueue> addToQueue(@RequestParam Long appointmentId) {
        try {
            WaitingQueue waitingQueue = waitingQueueService.addToQueue(appointmentId);
            return Result.success(waitingQueue);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/remove")
    public Result<Void> removeFromQueue(@RequestParam Long appointmentId) {
        waitingQueueService.removeFromQueue(appointmentId);
        return Result.success();
    }
}
