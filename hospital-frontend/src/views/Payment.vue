<template>
  <div class="payment-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的预约</span>
          <el-input
            v-model="searchPhone"
            placeholder="请输入患者手机号查询"
            style="width: 300px;"
            clearable
            @clear="loadAppointments"
          >
            <el-button slot="append" icon="el-icon-search" @click="searchAppointments"></el-button>
          </el-input>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待支付" name="pending">
          <el-table :data="pendingAppointments" border v-loading="loading">
            <el-table-column prop="id" label="预约号" width="100" />
            <el-table-column label="患者" width="100">
              <template #default="scope">
                {{ getPatientName(scope.row.patientId) }}
              </template>
            </el-table-column>
            <el-table-column label="医生" width="120">
              <template #default="scope">
                {{ getDoctorName(scope.row.doctorId) }}
              </template>
            </el-table-column>
            <el-table-column label="科室" width="100">
              <template #default="scope">
                {{ getDepartmentNameByDoctor(scope.row.doctorId) }}
              </template>
            </el-table-column>
            <el-table-column prop="appointmentDate" label="就诊日期" width="120" />
            <el-table-column label="时间段" width="150">
              <template #default="scope">
                {{ getTimePeriodName(scope.row.timePeriod) }}
              </template>
            </el-table-column>
            <el-table-column label="应付金额" width="120">
              <template #default="scope">
                <span style="color: #f56c6c; font-weight: bold;">¥{{ scope.row.payableAmount }}</span>
              </template>
            </el-table-column>
            <el-table-column label="支付有效期" width="200">
              <template #default="scope">
                <el-tag :type="isExpiringSoon(scope.row.expireTime) ? 'warning' : 'info'">
                  {{ scope.row.expireTime }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="primary" size="mini" @click="handlePay(scope.row)">立即支付</el-button>
                <el-button type="warning" size="mini" @click="handleCancel(scope.row)">取消预约</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="pendingAppointments.length === 0 && !loading" description="暂无待支付预约" />
        </el-tab-pane>
        
        <el-tab-pane label="已支付" name="paid">
          <el-table :data="paidAppointments" border v-loading="loading">
            <el-table-column prop="id" label="预约号" width="100" />
            <el-table-column label="患者" width="100">
              <template #default="scope">
                {{ getPatientName(scope.row.patientId) }}
              </template>
            </el-table-column>
            <el-table-column label="医生" width="120">
              <template #default="scope">
                {{ getDoctorName(scope.row.doctorId) }}
              </template>
            </el-table-column>
            <el-table-column label="科室" width="100">
              <template #default="scope">
                {{ getDepartmentNameByDoctor(scope.row.doctorId) }}
              </template>
            </el-table-column>
            <el-table-column prop="appointmentDate" label="就诊日期" width="120" />
            <el-table-column label="时间段" width="150">
              <template #default="scope">
                {{ getTimePeriodName(scope.row.timePeriod) }}
              </template>
            </el-table-column>
            <el-table-column label="支付金额" width="120">
              <template #default="scope">
                <span style="color: #67c23a; font-weight: bold;">¥{{ scope.row.payableAmount }}</span>
              </template>
            </el-table-column>
            <el-table-column label="支付时间" width="180">
              <template #default="scope">
                {{ scope.row.payTime }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag type="success">已支付</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button 
                  type="danger" 
                  size="mini" 
                  @click="handleRefund(scope.row)"
                  :disabled="isPastAppointment(scope.row.appointmentDate, scope.row.timePeriod)"
                >
                  退号
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="paidAppointments.length === 0 && !loading" description="暂无已支付预约" />
        </el-tab-pane>
        
        <el-tab-pane label="已取消/退款" name="cancelled">
          <el-table :data="cancelledAppointments" border v-loading="loading">
            <el-table-column prop="id" label="预约号" width="100" />
            <el-table-column label="患者" width="100">
              <template #default="scope">
                {{ getPatientName(scope.row.patientId) }}
              </template>
            </el-table-column>
            <el-table-column label="医生" width="120">
              <template #default="scope">
                {{ getDoctorName(scope.row.doctorId) }}
              </template>
            </el-table-column>
            <el-table-column label="科室" width="100">
              <template #default="scope">
                {{ getDepartmentNameByDoctor(scope.row.doctorId) }}
              </template>
            </el-table-column>
            <el-table-column prop="appointmentDate" label="就诊日期" width="120" />
            <el-table-column label="状态" width="120">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusName(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" width="180">
              <template #default="scope">
                {{ scope.row.createTime }}
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="cancelledAppointments.length === 0 && !loading" description="暂无已取消预约" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <el-dialog title="支付确认" :visible.sync="payDialogVisible" width="500px">
      <div v-if="currentAppointment">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="预约号">{{ currentAppointment.id }}</el-descriptions-item>
          <el-descriptions-item label="就诊日期">{{ currentAppointment.appointmentDate }}</el-descriptions-item>
          <el-descriptions-item label="就诊时段">{{ getTimePeriodName(currentAppointment.timePeriod) }}</el-descriptions-item>
          <el-descriptions-item label="接诊医生">{{ getDoctorName(currentAppointment.doctorId) }}</el-descriptions-item>
          <el-descriptions-item label="支付金额">
            <span style="color: #f56c6c; font-size: 28px; font-weight: bold;">
              ¥{{ currentAppointment.payableAmount }}
            </span>
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider />
        
        <el-form label-width="100px">
          <el-form-item label="支付方式">
            <el-radio-group v-model="paymentMethod">
              <el-radio label="微信支付">微信支付</el-radio>
              <el-radio label="支付宝">支付宝</el-radio>
              <el-radio label="医保支付">医保支付</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="payDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmPay" :loading="paying">确认支付</el-button>
        </span>
      </template>
    </el-dialog>
    
    <el-dialog title="退号确认" :visible.sync="refundDialogVisible" width="500px">
      <div v-if="currentAppointment">
        <el-alert title="退号说明" type="info" show-icon>
          <div>退号退费比例：</div>
          <ul style="margin: 10px 0 0 20px;">
            <li>就诊前24小时以上：全额退款 (100%)</li>
            <li>就诊前12-24小时：退款80%</li>
            <li>就诊前4-12小时：退款50%</li>
            <li>就诊前4小时以内：不予退款</li>
          </ul>
        </el-alert>
        
        <el-divider />
        
        <el-descriptions :column="1" border>
          <el-descriptions-item label="预约号">{{ currentAppointment.id }}</el-descriptions-item>
          <el-descriptions-item label="就诊日期">{{ currentAppointment.appointmentDate }}</el-descriptions-item>
          <el-descriptions-item label="预计退款金额">
            <span style="color: #e6a23c; font-size: 20px; font-weight: bold;">
              ¥{{ getEstimatedRefund() }}
            </span>
          </el-descriptions-item>
        </el-descriptions>
        
        <el-form label-width="100px" style="margin-top: 20px;">
          <el-form-item label="退号原因">
            <el-input v-model="refundReason" type="textarea" :rows="3" placeholder="请输入退号原因（选填）" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmRefund" :loading="refunding">确认退号</el-button>
        </span>
      </template>
    </el-dialog>
    
    <el-dialog title="取消预约" :visible.sync="cancelDialogVisible" width="400px">
      <div>
        <p>确定要取消该预约吗？</p>
        <p>取消后号源将被释放。</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialogVisible = false">取消</el-button>
          <el-button type="warning" @click="confirmCancel">确定取消</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { appointmentApi, paymentApi, refundApi, patientApi, doctorApi, departmentApi } from '../api'

export default {
  name: 'Payment',
  data() {
    return {
      activeTab: 'pending',
      appointments: [],
      loading: false,
      searchPhone: '',
      patients: {},
      doctors: {},
      departments: {},
      payDialogVisible: false,
      refundDialogVisible: false,
      cancelDialogVisible: false,
      currentAppointment: null,
      paymentMethod: '微信支付',
      refundReason: '',
      paying: false,
      refunding: false
    }
  },
  computed: {
    pendingAppointments() {
      return this.appointments.filter(a => a.status === 0)
    },
    paidAppointments() {
      return this.appointments.filter(a => a.status === 1)
    },
    cancelledAppointments() {
      return this.appointments.filter(a => a.status === 2 || a.status === 3 || a.status === 4 || a.status === 5)
    }
  },
  created() {
    this.loadAllData()
  },
  methods: {
    async loadAllData() {
      try {
        const [patientsRes, doctorsRes, departmentsRes] = await Promise.all([
          patientApi.getAll(),
          doctorApi.getAll(),
          departmentApi.getAll()
        ])
        
        patientsRes.data?.forEach(p => {
          this.patients[p.id] = p
        })
        doctorsRes.data?.forEach(d => {
          this.doctors[d.id] = d
        })
        departmentsRes.data?.forEach(d => {
          this.departments[d.id] = d
        })
        
        this.loadAppointments()
      } catch (error) {
        console.error('加载数据失败:', error)
      }
    },
    
    async loadAppointments() {
      this.loading = true
      try {
        const res = await appointmentApi.getAll()
        this.appointments = (res.data || []).sort((a, b) => {
          return new Date(b.createTime) - new Date(a.createTime)
        })
      } catch (error) {
        this.$message.error('加载预约列表失败')
      } finally {
        this.loading = false
      }
    },
    
    async searchAppointments() {
      if (!this.searchPhone) {
        this.loadAppointments()
        return
      }
      
      try {
        const patientRes = await patientApi.getByPhone(this.searchPhone)
        if (patientRes.data) {
          const res = await appointmentApi.getByPatient(patientRes.data.id)
          this.appointments = (res.data || []).sort((a, b) => {
            return new Date(b.createTime) - new Date(a.createTime)
          })
        } else {
          this.appointments = []
        }
      } catch (error) {
        this.$message.error('查询失败')
      }
    },
    
    getPatientName(patientId) {
      return this.patients[patientId]?.name || '未知患者'
    },
    
    getDoctorName(doctorId) {
      return this.doctors[doctorId]?.name || '未知医生'
    },
    
    getDepartmentNameByDoctor(doctorId) {
      const doctor = this.doctors[doctorId]
      if (doctor) {
        return this.departments[doctor.departmentId]?.name || '未知科室'
      }
      return '未知科室'
    },
    
    getTimePeriodName(period) {
      const map = {
        morning: '上午 (08:00-12:00)',
        afternoon: '下午 (14:00-17:30)',
        evening: '晚上 (18:00-21:00)'
      }
      return map[period] || period
    },
    
    getStatusName(status) {
      const map = {
        0: '待支付',
        1: '已支付',
        2: '已取消',
        3: '已退款',
        4: '已过期',
        5: '已完成'
      }
      return map[status] || '未知'
    },
    
    getStatusType(status) {
      const map = {
        2: 'warning',
        3: 'info',
        4: 'danger',
        5: 'success'
      }
      return map[status] || 'info'
    },
    
    isExpiringSoon(expireTime) {
      if (!expireTime) return false
      const expire = new Date(expireTime)
      const now = new Date()
      const diff = expire.getTime() - now.getTime()
      return diff < 10 * 60 * 1000
    },
    
    isPastAppointment(date, period) {
      if (!date) return true
      const appointmentDate = new Date(date)
      const now = new Date()
      
      let hour = 0
      if (period === 'morning') hour = 12
      else if (period === 'afternoon') hour = 17
      else if (period === 'evening') hour = 21
      
      appointmentDate.setHours(hour, 0, 0, 0)
      return now > appointmentDate
    },
    
    handlePay(appointment) {
      this.currentAppointment = appointment
      this.paymentMethod = '微信支付'
      this.payDialogVisible = true
    },
    
    async confirmPay() {
      this.paying = true
      try {
        const res = await paymentApi.payAppointment(this.currentAppointment.id, this.paymentMethod)
        this.$message.success('支付成功！已加入候诊队列')
        this.payDialogVisible = false
        this.loadAppointments()
      } catch (error) {
        this.$message.error('支付失败: ' + (error.message || error))
      } finally {
        this.paying = false
      }
    },
    
    handleRefund(appointment) {
      this.currentAppointment = appointment
      this.refundReason = ''
      this.refundDialogVisible = true
    },
    
    getEstimatedRefund() {
      if (!this.currentAppointment) return '0.00'
      
      const appointmentDate = new Date(this.currentAppointment.appointmentDate)
      const period = this.currentAppointment.timePeriod
      
      let hour = 0
      if (period === 'morning') hour = 8
      else if (period === 'afternoon') hour = 14
      else if (period === 'evening') hour = 18
      
      appointmentDate.setHours(hour, 0, 0, 0)
      const now = new Date()
      const hoursUntilAppointment = (appointmentDate.getTime() - now.getTime()) / (1000 * 60 * 60)
      
      let ratio = 0
      if (hoursUntilAppointment >= 24) ratio = 1
      else if (hoursUntilAppointment >= 12) ratio = 0.8
      else if (hoursUntilAppointment >= 4) ratio = 0.5
      
      const amount = parseFloat(this.currentAppointment.payableAmount)
      return (amount * ratio).toFixed(2)
    },
    
    async confirmRefund() {
      this.refunding = true
      try {
        const res = await refundApi.processRefund(
          this.currentAppointment.id,
          this.refundReason || '患者主动退号'
        )
        this.$message.success(`退号成功！退款金额: ¥${res.data?.refundAmount}`)
        this.refundDialogVisible = false
        this.loadAppointments()
      } catch (error) {
        this.$message.error('退号失败: ' + (error.message || error))
      } finally {
        this.refunding = false
      }
    },
    
    handleCancel(appointment) {
      this.currentAppointment = appointment
      this.cancelDialogVisible = true
    },
    
    async confirmCancel() {
      try {
        const res = await appointmentApi.getById(this.currentAppointment.id)
        if (res.data) {
          res.data.status = 2
          await appointmentApi.checkExpired()
        }
        this.$message.success('预约已取消')
        this.cancelDialogVisible = false
        this.loadAppointments()
      } catch (error) {
        this.$message.error('取消失败: ' + (error.message || error))
      }
    }
  }
}
</script>

<style scoped>
.payment-page {
  padding: 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
