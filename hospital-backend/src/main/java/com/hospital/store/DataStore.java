package com.hospital.store;

import com.hospital.entity.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class DataStore {
    
    private static final DataStore INSTANCE = new DataStore();
    
    private final AtomicLong departmentIdGenerator = new AtomicLong(1);
    private final AtomicLong doctorIdGenerator = new AtomicLong(1);
    private final AtomicLong scheduleIdGenerator = new AtomicLong(1);
    private final AtomicLong slotIdGenerator = new AtomicLong(1);
    private final AtomicLong patientIdGenerator = new AtomicLong(1);
    private final AtomicLong appointmentIdGenerator = new AtomicLong(1);
    private final AtomicLong paymentIdGenerator = new AtomicLong(1);
    private final AtomicLong refundIdGenerator = new AtomicLong(1);
    private final AtomicLong waitingQueueIdGenerator = new AtomicLong(1);
    private final AtomicLong slotCloseRecordIdGenerator = new AtomicLong(1);
    private final AtomicLong refundTaskIdGenerator = new AtomicLong(1);
    
    private final Map<Long, Department> departments = new ConcurrentHashMap<>();
    private final Map<Long, Doctor> doctors = new ConcurrentHashMap<>();
    private final Map<Long, Schedule> schedules = new ConcurrentHashMap<>();
    private final Map<Long, Slot> slots = new ConcurrentHashMap<>();
    private final Map<Long, Patient> patients = new ConcurrentHashMap<>();
    private final Map<Long, Appointment> appointments = new ConcurrentHashMap<>();
    private final Map<Long, Payment> payments = new ConcurrentHashMap<>();
    private final Map<Long, Refund> refunds = new ConcurrentHashMap<>();
    private final Map<Long, WaitingQueue> waitingQueues = new ConcurrentHashMap<>();
    private final Map<Long, SlotCloseRecord> slotCloseRecords = new ConcurrentHashMap<>();
    private final Map<Long, RefundTask> refundTasks = new ConcurrentHashMap<>();
    
    private final List<WaitingQueue> waitingQueueList = new CopyOnWriteArrayList<>();
    
    private DataStore() {
    }
    
    public static DataStore getInstance() {
        return INSTANCE;
    }
    
    public long nextDepartmentId() {
        return departmentIdGenerator.getAndIncrement();
    }
    
    public long nextDoctorId() {
        return doctorIdGenerator.getAndIncrement();
    }
    
    public long nextScheduleId() {
        return scheduleIdGenerator.getAndIncrement();
    }
    
    public long nextSlotId() {
        return slotIdGenerator.getAndIncrement();
    }
    
    public long nextPatientId() {
        return patientIdGenerator.getAndIncrement();
    }
    
    public long nextAppointmentId() {
        return appointmentIdGenerator.getAndIncrement();
    }
    
    public long nextPaymentId() {
        return paymentIdGenerator.getAndIncrement();
    }
    
    public long nextRefundId() {
        return refundIdGenerator.getAndIncrement();
    }
    
    public long nextWaitingQueueId() {
        return waitingQueueIdGenerator.getAndIncrement();
    }
    
    public long nextSlotCloseRecordId() {
        return slotCloseRecordIdGenerator.getAndIncrement();
    }
    
    public long nextRefundTaskId() {
        return refundTaskIdGenerator.getAndIncrement();
    }
    
    public Map<Long, Department> getDepartments() {
        return departments;
    }
    
    public Map<Long, Doctor> getDoctors() {
        return doctors;
    }
    
    public Map<Long, Schedule> getSchedules() {
        return schedules;
    }
    
    public Map<Long, Slot> getSlots() {
        return slots;
    }
    
    public Map<Long, Patient> getPatients() {
        return patients;
    }
    
    public Map<Long, Appointment> getAppointments() {
        return appointments;
    }
    
    public Map<Long, Payment> getPayments() {
        return payments;
    }
    
    public Map<Long, Refund> getRefunds() {
        return refunds;
    }
    
    public Map<Long, WaitingQueue> getWaitingQueues() {
        return waitingQueues;
    }
    
    public List<WaitingQueue> getWaitingQueueList() {
        return waitingQueueList;
    }
    
    public Map<Long, SlotCloseRecord> getSlotCloseRecords() {
        return slotCloseRecords;
    }
    
    public Map<Long, RefundTask> getRefundTasks() {
        return refundTasks;
    }
}
