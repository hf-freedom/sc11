package com.hospital.service;

import com.hospital.entity.Appointment;
import com.hospital.entity.Patient;
import com.hospital.entity.WaitingQueue;
import com.hospital.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WaitingQueueService {
    
    private final DataStore dataStore = DataStore.getInstance();
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private PatientService patientService;
    
    public List<WaitingQueue> getAllWaitingQueues() {
        return dataStore.getWaitingQueueList().stream()
                .sorted(Comparator.comparing(WaitingQueue::getJoinTime))
                .collect(Collectors.toList());
    }
    
    public List<WaitingQueue> getWaitingQueueByDoctor(Long doctorId) {
        return dataStore.getWaitingQueueList().stream()
                .filter(wq -> doctorId.equals(wq.getDoctorId()))
                .sorted(Comparator.comparing(WaitingQueue::getJoinTime))
                .collect(Collectors.toList());
    }
    
    public WaitingQueue getWaitingQueueByAppointment(Long appointmentId) {
        return dataStore.getWaitingQueueList().stream()
                .filter(wq -> appointmentId.equals(wq.getAppointmentId()))
                .findFirst()
                .orElse(null);
    }
    
    public synchronized WaitingQueue addToQueue(Long appointmentId) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        if (appointment == null) {
            throw new IllegalArgumentException("预约不存在");
        }
        
        WaitingQueue existing = getWaitingQueueByAppointment(appointmentId);
        if (existing != null) {
            return existing;
        }
        
        Patient patient = patientService.getPatientById(appointment.getPatientId());
        String patientName = patient != null ? patient.getName() : "未知患者";
        
        int queueNumber = getNextQueueNumber(appointment.getDoctorId());
        
        WaitingQueue waitingQueue = WaitingQueue.builder()
                .id(dataStore.nextWaitingQueueId())
                .appointmentId(appointmentId)
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .patientName(patientName)
                .queueNumber(queueNumber)
                .status(1)
                .joinTime(LocalDateTime.now())
                .build();
        
        dataStore.getWaitingQueues().put(waitingQueue.getId(), waitingQueue);
        dataStore.getWaitingQueueList().add(waitingQueue);
        
        return waitingQueue;
    }
    
    public synchronized void removeFromQueue(Long appointmentId) {
        WaitingQueue waitingQueue = getWaitingQueueByAppointment(appointmentId);
        if (waitingQueue != null) {
            waitingQueue.setStatus(0);
            dataStore.getWaitingQueueList().remove(waitingQueue);
            dataStore.getWaitingQueues().remove(waitingQueue.getId());
        }
    }
    
    private int getNextQueueNumber(Long doctorId) {
        List<WaitingQueue> doctorQueue = getWaitingQueueByDoctor(doctorId);
        if (doctorQueue.isEmpty()) {
            return 1;
        }
        return doctorQueue.stream()
                .mapToInt(WaitingQueue::getQueueNumber)
                .max()
                .orElse(0) + 1;
    }
}
