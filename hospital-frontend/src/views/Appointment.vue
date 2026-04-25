<template>
  <div class="appointment-page">
    <el-card>
      <template #header>
        <span>预约挂号</span>
      </template>
      
      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step title="选择患者" />
        <el-step title="选择科室医生" />
        <el-step title="选择号源" />
        <el-step title="确认预约" />
      </el-steps>
      
      <el-divider />
      
      <div v-if="activeStep === 0">
        <h4>请选择患者</h4>
        <el-form :model="patientForm" label-width="100px" style="max-width: 500px;">
          <el-form-item label="患者姓名">
            <el-input v-model="patientForm.name" placeholder="请输入患者姓名" />
          </el-form-item>
          <el-form-item label="手机号码">
            <el-input v-model="patientForm.phone" placeholder="请输入手机号码" maxlength="11" />
          </el-form-item>
          <el-form-item label="医保类型">
            <el-select v-model="patientForm.insuranceType" placeholder="请选择医保类型" style="width: 100%;">
              <el-option label="医保" value="医保" />
              <el-option label="自费" value="自费" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="nextStep(1)">下一步</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div v-if="activeStep === 1">
        <h4>请选择科室和医生</h4>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="dept-title">科室列表</div>
            <el-menu
              :default-active="selectedDeptId?.toString()"
              background-color="#f5f7fa"
              text-color="#606266"
              active-text-color="#409EFF"
              @select="handleDeptSelect"
            >
              <el-menu-item
                v-for="dept in departments"
                :key="dept.id"
                :index="dept.id.toString()"
              >
                <span>{{ dept.name }}</span>
                <span class="dept-fee">挂号费: ¥{{ dept.defaultFee }}</span>
              </el-menu-item>
            </el-menu>
          </el-col>
          <el-col :span="16">
            <div class="dept-title">医生列表</div>
            <el-table :data="doctorsByDept" border v-loading="loadingDoctors">
              <el-table-column prop="name" label="医生姓名" width="120" />
              <el-table-column prop="title" label="职称" width="120" />
              <el-table-column label="职称加价" width="120">
                <template #default="scope">
                  <span style="color: #f56c6c;">+¥{{ getTitleSurcharge(scope.row.title) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="maxDailyPatients" label="每日接诊上限" width="120" />
              <el-table-column label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                    {{ scope.row.status === 1 ? '可预约' : '暂停' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button
                    type="primary"
                    size="mini"
                    :disabled="scope.row.status !== 1"
                    @click="selectDoctor(scope.row)"
                  >
                    选择
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-alert v-if="!selectedDeptId" title="请先选择左侧科室" type="info" show-icon style="margin-top: 20px;" />
          </el-col>
        </el-row>
        <div style="margin-top: 20px;">
          <el-button @click="prevStep">上一步</el-button>
        </div>
      </div>
      
      <div v-if="activeStep === 2">
        <h4>请选择就诊日期和时段</h4>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-date-picker
              v-model="appointmentDate"
              type="date"
              placeholder="选择就诊日期"
              :clearable="false"
              value-format="yyyy-MM-dd"
              :disabled-date="disabledDate"
              style="width: 100%;"
              @change="loadSlots"
            />
          </el-col>
        </el-row>
        
        <el-divider />
        
        <div v-if="slots.length > 0">
          <h4>可预约号源</h4>
          <el-row :gutter="20">
            <el-col :span="8" v-for="slot in slots" :key="slot.id">
              <el-card :class="{ 'slot-card': true, 'slot-disabled': slot.status !== 1 || slot.availableCount <= 0 }">
                <div class="slot-time">{{ getTimePeriodName(slot.timePeriod) }}</div>
                <div class="slot-count">
                  剩余号源: <span :class="{ 'count-warning': slot.availableCount <= 5 }">{{ slot.availableCount }} / {{ slot.totalCount }}</span>
                </div>
                <div class="slot-status">
                  <el-tag :type="slot.status === 1 && slot.availableCount > 0 ? 'success' : 'danger'">
                    {{ slot.status === 1 && slot.availableCount > 0 ? '可预约' : '已约满' }}
                  </el-tag>
                </div>
                <el-button
                  type="primary"
                  :disabled="slot.status !== 1 || slot.availableCount <= 0"
                  style="width: 100%; margin-top: 10px;"
                  @click="selectSlot(slot)"
                >
                  选择此号源
                </el-button>
              </el-card>
            </el-col>
          </el-row>
        </div>
        <el-alert v-else-if="appointmentDate && slotsLoaded" title="该日期无可用号源" type="warning" show-icon />
        <el-alert v-else title="请选择就诊日期查看号源" type="info" show-icon />
        
        <div style="margin-top: 20px;">
          <el-button @click="prevStep">上一步</el-button>
        </div>
      </div>
      
      <div v-if="activeStep === 3">
        <h4>确认预约信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="患者姓名">{{ patientForm.name }}</el-descriptions-item>
          <el-descriptions-item label="手机号码">{{ patientForm.phone }}</el-descriptions-item>
          <el-descriptions-item label="医保类型">{{ patientForm.insuranceType }}</el-descriptions-item>
          <el-descriptions-item label="就诊科室">{{ selectedDept?.name }}</el-descriptions-item>
          <el-descriptions-item label="接诊医生">{{ selectedDoctor?.name }} ({{ selectedDoctor?.title }})</el-descriptions-item>
          <el-descriptions-item label="就诊日期">{{ appointmentDate }}</el-descriptions-item>
          <el-descriptions-item label="就诊时段">{{ getTimePeriodName(selectedSlot?.timePeriod) }}</el-descriptions-item>
          <el-descriptions-item label="医保类型">{{ patientForm.insuranceType }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider />
        
        <h4>费用明细</h4>
        <el-table :data="feeDetails" show-summary :summary-method="getSummaries">
          <el-table-column prop="name" label="费用项目" />
          <el-table-column prop="amount" label="金额">
            <template #default="scope">
              ¥{{ scope.row.amount }}
            </template>
          </el-table-column>
        </el-table>
        
        <div style="margin-top: 20px;">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="confirmAppointment" :loading="submitting">确认预约</el-button>
        </div>
      </div>
      
      <el-dialog title="预约结果" :visible.sync="resultDialogVisible" width="500px">
        <div v-if="appointmentResult">
          <el-alert :title="appointmentResult.message" :type="appointmentResult.success ? 'success' : 'error'" show-icon />
          <el-descriptions v-if="appointmentResult.success" :column="1" border style="margin-top: 20px;">
            <el-descriptions-item label="预约编号">{{ appointmentResult.data?.id }}</el-descriptions-item>
            <el-descriptions-item label="待支付金额">
              <span style="color: #f56c6c; font-size: 24px; font-weight: bold;">
                ¥{{ appointmentResult.data?.payableAmount }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="支付有效期">
              <span style="color: #e6a23c;">请在30分钟内完成支付</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="resultDialogVisible = false">关闭</el-button>
            <el-button v-if="appointmentResult?.success" type="primary" @click="goToPayment">去支付</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { departmentApi, doctorApi, slotApi, patientApi, appointmentApi } from '../api'

export default {
  name: 'Appointment',
  data() {
    return {
      activeStep: 0,
      patientForm: {
        name: '',
        phone: '',
        insuranceType: '医保'
      },
      departments: [],
      doctorsByDept: [],
      selectedDeptId: null,
      selectedDoctor: null,
      appointmentDate: null,
      slots: [],
      slotsLoaded: false,
      selectedSlot: null,
      submitting: false,
      resultDialogVisible: false,
      appointmentResult: null,
      loadingDoctors: false,
      currentPatientId: null
    }
  },
  computed: {
    selectedDept() {
      return this.departments.find(d => d.id === this.selectedDeptId)
    },
    feeDetails() {
      const details = []
      if (this.selectedDept) {
        details.push({ name: '科室挂号费', amount: this.selectedDept.defaultFee })
      }
      if (this.selectedDoctor) {
        const surcharge = this.getTitleSurcharge(this.selectedDoctor.title)
        if (surcharge > 0) {
          details.push({ name: `职称加价(${this.selectedDoctor.title})`, amount: surcharge })
        }
      }
      if (this.patientForm.insuranceType === '医保') {
        const total = (this.selectedDept?.defaultFee || 0) + this.getTitleSurcharge(this.selectedDoctor?.title)
        const discount = total * 0.5
        details.push({ name: '医保减免', amount: -discount.toFixed(2) })
      }
      return details
    }
  },
  created() {
    this.loadDepartments()
  },
  methods: {
    async loadDepartments() {
      try {
        const res = await departmentApi.getAll()
        this.departments = res.data || []
      } catch (error) {
        this.$message.error('加载科室失败')
      }
    },
    
    handleDeptSelect(index) {
      this.selectedDeptId = parseInt(index)
      this.selectedDoctor = null
      this.loadDoctorsByDept()
    },
    
    async loadDoctorsByDept() {
      if (!this.selectedDeptId) return
      this.loadingDoctors = true
      try {
        const res = await doctorApi.getByDepartment(this.selectedDeptId)
        this.doctorsByDept = res.data || []
      } catch (error) {
        this.$message.error('加载医生列表失败')
      } finally {
        this.loadingDoctors = false
      }
    },
    
    getTitleSurcharge(title) {
      switch (title) {
        case '主任医师': return 50
        case '副主任医师': return 30
        case '主治医师': return 10
        default: return 0
      }
    },
    
    selectDoctor(doctor) {
      this.selectedDoctor = doctor
      this.nextStep(2)
    },
    
    disabledDate(time) {
      return time.getTime() < Date.now() - 8.64e7
    },
    
    async loadSlots() {
      if (!this.selectedDoctor || !this.appointmentDate) return
      this.slotsLoaded = false
      try {
        const res = await slotApi.getAvailableByDoctorAndDate(this.selectedDoctor.id, this.appointmentDate)
        this.slots = res.data || []
        this.slotsLoaded = true
      } catch (error) {
        this.$message.error('加载号源失败')
        this.slotsLoaded = true
      }
    },
    
    getTimePeriodName(period) {
      const map = {
        morning: '上午 (08:00-12:00)',
        afternoon: '下午 (14:00-17:30)',
        evening: '晚上 (18:00-21:00)'
      }
      return map[period] || period
    },
    
    selectSlot(slot) {
      this.selectedSlot = slot
      this.nextStep(3)
    },
    
    getSummaries(param) {
      const { columns, data } = param
      const sums = []
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '应付金额'
          return
        }
        const total = data.reduce((sum, item) => sum + parseFloat(item.amount), 0)
        sums[index] = `¥${total.toFixed(2)}`
      })
      return sums
    },
    
    prevStep() {
      if (this.activeStep > 0) {
        this.activeStep--
      }
    },
    
    nextStep(step) {
      if (step === 1) {
        if (!this.patientForm.name) {
          this.$message.warning('请输入患者姓名')
          return
        }
        if (!this.patientForm.phone || !/^1[3-9]\d{9}$/.test(this.patientForm.phone)) {
          this.$message.warning('请输入正确的手机号码')
          return
        }
        if (!this.patientForm.insuranceType) {
          this.$message.warning('请选择医保类型')
          return
        }
      }
      if (step === 2 && !this.selectedDoctor) {
        this.$message.warning('请选择医生')
        return
      }
      if (step === 3 && !this.selectedSlot) {
        this.$message.warning('请选择号源')
        return
      }
      this.activeStep = step
    },
    
    async confirmAppointment() {
      this.submitting = true
      try {
        let patient = await patientApi.getByPhone(this.patientForm.phone).catch(() => null)
        
        if (!patient) {
          patient = await patientApi.create({
            name: this.patientForm.name,
            phone: this.patientForm.phone,
            insuranceType: this.patientForm.insuranceType,
            isBlacklisted: false
          })
        }
        
        const patientId = patient.data?.id || patient.id
        
        const res = await appointmentApi.create({
          patientId: patientId,
          doctorId: this.selectedDoctor.id,
          appointmentDate: this.appointmentDate,
          timePeriod: this.selectedSlot.timePeriod
        })
        
        this.appointmentResult = {
          success: true,
          message: '预约成功！请在30分钟内完成支付',
          data: res.data
        }
        this.resultDialogVisible = true
      } catch (error) {
        this.appointmentResult = {
          success: false,
          message: error.message || '预约失败'
        }
        this.resultDialogVisible = true
      } finally {
        this.submitting = false
      }
    },
    
    goToPayment() {
      this.resultDialogVisible = false
      this.$router.push('/payment')
    }
  }
}
</script>

<style scoped>
.appointment-page {
  padding: 0;
}
.dept-title {
  font-weight: bold;
  margin-bottom: 10px;
}
.dept-fee {
  float: right;
  color: #909399;
  font-size: 12px;
}
.slot-card {
  cursor: pointer;
  transition: all 0.3s;
}
.slot-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.slot-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.slot-time {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}
.slot-count {
  margin-bottom: 10px;
}
.count-warning {
  color: #e6a23c;
  font-weight: bold;
}
</style>
