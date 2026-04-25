import request from './request'

function formatDate(date) {
  if (!date) return ''
  if (typeof date === 'string') {
    if (date.includes('T')) {
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }
    return date
  }
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatScheduleData(data) {
  if (!data) return data
  const result = { ...data }
  if (result.scheduleDate) {
    result.scheduleDate = formatDate(result.scheduleDate)
  }
  return result
}

function formatAppointmentData(data) {
  if (!data) return data
  const result = { ...data }
  if (result.appointmentDate) {
    result.appointmentDate = formatDate(result.appointmentDate)
  }
  return result
}

export const departmentApi = {
  getAll: () => request.get('/departments'),
  getById: (id) => request.get(`/departments/${id}`)
}

export const doctorApi = {
  getAll: () => request.get('/doctors'),
  getById: (id) => request.get(`/doctors/${id}`),
  getByDepartment: (departmentId) => request.get(`/doctors/department/${departmentId}`),
  create: (data) => request.post('/doctors', data),
  update: (id, data) => request.put(`/doctors/${id}`, data),
  delete: (id) => request.delete(`/doctors/${id}`)
}

export const scheduleApi = {
  getAll: () => request.get('/schedules'),
  getById: (id) => request.get(`/schedules/${id}`),
  getByDoctorAndDate: (doctorId, date) => request.get(`/schedules/doctor/${doctorId}`, { params: { date: formatDate(date) } }),
  getByDate: (date) => request.get(`/schedules/date/${formatDate(date)}`),
  createOrUpdate: (data) => request.post('/schedules', formatScheduleData(data)),
  handleAbsence: (doctorId, date, reason) => request.post('/schedules/absence', null, { params: { doctorId, date: formatDate(date), reason } })
}

export const slotApi = {
  getAll: () => request.get('/slots'),
  getById: (id) => request.get(`/slots/${id}`),
  getByDoctorAndDate: (doctorId, date) => request.get(`/slots/doctor/${doctorId}`, { params: { date: formatDate(date) } }),
  getAvailableByDoctorAndDate: (doctorId, date) => request.get(`/slots/doctor/${doctorId}/available`, { params: { date: formatDate(date) } }),
  getByDate: (date) => request.get(`/slots/date/${formatDate(date)}`)
}

export const patientApi = {
  getAll: () => request.get('/patients'),
  getById: (id) => request.get(`/patients/${id}`),
  getByPhone: (phone) => request.get(`/patients/phone/${phone}`),
  create: (data) => request.post('/patients', data),
  update: (id, data) => request.put(`/patients/${id}`, data),
  isBlacklisted: (id) => request.get(`/patients/${id}/blacklisted`)
}

export const appointmentApi = {
  getAll: () => request.get('/appointments'),
  getById: (id) => request.get(`/appointments/${id}`),
  getByPatient: (patientId) => request.get(`/appointments/patient/${patientId}`),
  getByDoctor: (doctorId) => request.get(`/appointments/doctor/${doctorId}`),
  create: (data) => request.post('/appointments', null, { params: formatAppointmentData(data) }),
  checkExpired: () => request.post('/appointments/check-expired')
}

export const paymentApi = {
  getAll: () => request.get('/payments'),
  getById: (id) => request.get(`/payments/${id}`),
  getByAppointment: (appointmentId) => request.get(`/payments/appointment/${appointmentId}`),
  create: (appointmentId) => request.post('/payments', null, { params: { appointmentId } }),
  process: (id, paymentMethod) => request.post(`/payments/${id}/pay`, null, { params: { paymentMethod } }),
  payAppointment: (appointmentId, paymentMethod) => request.post(`/payments/appointment/${appointmentId}/pay`, null, { params: { paymentMethod } })
}

export const refundApi = {
  getAll: () => request.get('/refunds'),
  getById: (id) => request.get(`/refunds/${id}`),
  getByAppointment: (appointmentId) => request.get(`/refunds/appointment/${appointmentId}`),
  process: (appointmentId, reason) => request.post('/refunds', null, { params: { appointmentId, reason } })
}

export const waitingQueueApi = {
  getAll: () => request.get('/waiting-queue'),
  getByDoctor: (doctorId) => request.get(`/waiting-queue/doctor/${doctorId}`),
  getByAppointment: (appointmentId) => request.get(`/waiting-queue/appointment/${appointmentId}`),
  addToQueue: (appointmentId) => request.post('/waiting-queue/add', null, { params: { appointmentId } }),
  removeFromQueue: (appointmentId) => request.post('/waiting-queue/remove', null, { params: { appointmentId } })
}
