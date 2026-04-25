<template>
  <div class="schedule-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>医生排班管理</span>
          <el-button type="primary" @click="handleAddDoctor" size="small">添加医生</el-button>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="医生列表" name="doctors">
          <el-table :data="doctors" border style="width: 100%;">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="医生姓名" width="120" />
            <el-table-column prop="title" label="职称" width="120" />
            <el-table-column label="科室" width="120">
              <template #default="scope">
                {{ getDepartmentName(scope.row.departmentId) }}
              </template>
            </el-table-column>
            <el-table-column prop="maxDailyPatients" label="每日最大接诊数" width="140" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                  {{ scope.row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250">
              <template #default="scope">
                <el-button size="mini" type="primary" @click="handleEditDoctor(scope.row)">编辑</el-button>
                <el-button size="mini" type="success" @click="handleSetSchedule(scope.row)">排班</el-button>
                <el-button size="mini" type="danger" @click="handleDeleteDoctor(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="排班设置" name="schedule">
          <el-form :inline="true" class="demo-form-inline">
            <el-form-item label="选择医生">
              <el-select v-model="selectedDoctorId" placeholder="请选择医生" style="width: 200px;">
                <el-option
                  v-for="doctor in doctors"
                  :key="doctor.id"
                  :label="doctor.name + ' - ' + doctor.title"
                  :value="doctor.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="选择日期">
              <el-date-picker
                v-model="selectedDate"
                type="date"
                placeholder="选择日期"
                :clearable="false"
                value-format="yyyy-MM-dd"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadSchedule">查询</el-button>
            </el-form-item>
          </el-form>
          
          <el-divider />
          
          <el-form v-if="selectedDoctorId" label-width="100px">
            <el-form-item label="医生信息">
              <span>{{ selectedDoctor?.name }} - {{ selectedDoctor?.title }}</span>
            </el-form-item>
            <el-form-item label="日期">
              <span>{{ selectedDate }}</span>
            </el-form-item>
            <el-form-item label="上午">
              <el-switch v-model="scheduleForm.morning" active-text="出诊" inactive-text="休息" />
            </el-form-item>
            <el-form-item label="下午">
              <el-switch v-model="scheduleForm.afternoon" active-text="出诊" inactive-text="休息" />
            </el-form-item>
            <el-form-item label="晚上">
              <el-switch v-model="scheduleForm.evening" active-text="出诊" inactive-text="休息" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveSchedule">保存排班</el-button>
              <el-button type="warning" @click="handleAbsence">医生停诊</el-button>
            </el-form-item>
          </el-form>
          
          <el-alert v-else title="请先选择医生和日期" type="info" show-icon />
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <el-dialog title="医生信息" :visible.sync="doctorDialogVisible" width="500px">
      <el-form :model="doctorForm" label-width="100px">
        <el-form-item label="医生姓名">
          <el-input v-model="doctorForm.name" placeholder="请输入医生姓名" />
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="doctorForm.departmentId" placeholder="请选择科室" style="width: 100%;">
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职称">
          <el-select v-model="doctorForm.title" placeholder="请选择职称" style="width: 100%;">
            <el-option label="主任医师" value="主任医师" />
            <el-option label="副主任医师" value="副主任医师" />
            <el-option label="主治医师" value="主治医师" />
            <el-option label="医师" value="医师" />
          </el-select>
        </el-form-item>
        <el-form-item label="每日最大接诊数">
          <el-input-number v-model="doctorForm.maxDailyPatients" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="doctorForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="doctorDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveDoctor">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <el-dialog title="医生停诊" :visible.sync="absenceDialogVisible" width="400px">
      <el-form label-width="80px">
        <el-form-item label="停诊原因">
          <el-input v-model="absenceReason" type="textarea" :rows="3" placeholder="请输入停诊原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="absenceDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmAbsence">确认停诊</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { doctorApi, departmentApi, scheduleApi } from '../api'

function formatDate(date) {
  if (!date) return ''
  if (typeof date === 'string') return date
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export default {
  name: 'Schedule',
  data() {
    return {
      activeTab: 'doctors',
      doctors: [],
      departments: [],
      selectedDoctorId: null,
      selectedDate: formatDate(new Date()),
      scheduleForm: {
        morning: false,
        afternoon: false,
        evening: false
      },
      doctorDialogVisible: false,
      doctorForm: {
        id: null,
        name: '',
        departmentId: null,
        title: '',
        maxDailyPatients: 20,
        status: 1
      },
      absenceDialogVisible: false,
      absenceReason: ''
    }
  },
  computed: {
    selectedDoctor() {
      return this.doctors.find(d => d.id === this.selectedDoctorId)
    }
  },
  watch: {
    selectedDoctorId(newVal) {
      if (newVal && this.activeTab === 'schedule') {
        this.loadSchedule()
      }
    },
    selectedDate(newVal) {
      if (this.selectedDoctorId && this.activeTab === 'schedule') {
        this.loadSchedule()
      }
    },
    activeTab(newVal) {
      if (newVal === 'schedule' && this.selectedDoctorId) {
        this.loadSchedule()
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        const deptsRes = await departmentApi.getAll()
        const doctorsRes = await doctorApi.getAll()
        this.departments = deptsRes.data || []
        this.doctors = doctorsRes.data || []
      } catch (error) {
        console.error('加载数据失败:', error)
      }
    },
    
    getDepartmentName(departmentId) {
      const dept = this.departments.find(d => d.id === departmentId)
      return dept ? dept.name : '未知科室'
    },
    
    handleAddDoctor() {
      this.doctorForm = {
        id: null,
        name: '',
        departmentId: this.departments[0]?.id,
        title: '',
        maxDailyPatients: 20,
        status: 1
      }
      this.doctorDialogVisible = true
    },
    
    handleEditDoctor(doctor) {
      this.doctorForm = { ...doctor }
      this.doctorDialogVisible = true
    },
    
    async handleDeleteDoctor(doctor) {
      try {
        await this.$confirm('确定要删除该医生吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await doctorApi.delete(doctor.id)
        this.$message.success('删除成功')
        this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败: ' + (error.message || error))
        }
      }
    },
    
    async saveDoctor() {
      try {
        if (!this.doctorForm.name) {
          this.$message.warning('请输入医生姓名')
          return
        }
        if (!this.doctorForm.departmentId) {
          this.$message.warning('请选择科室')
          return
        }
        if (!this.doctorForm.title) {
          this.$message.warning('请选择职称')
          return
        }
        
        if (this.doctorForm.id) {
          await doctorApi.update(this.doctorForm.id, this.doctorForm)
        } else {
          await doctorApi.create(this.doctorForm)
        }
        this.$message.success('保存成功')
        this.doctorDialogVisible = false
        this.loadData()
      } catch (error) {
        this.$message.error('保存失败: ' + (error.message || error))
      }
    },
    
    handleSetSchedule(doctor) {
      this.selectedDoctorId = doctor.id
      this.activeTab = 'schedule'
      this.loadSchedule()
    },
    
    async loadSchedule() {
      if (!this.selectedDoctorId) return
      
      try {
        const res = await scheduleApi.getByDoctorAndDate(this.selectedDoctorId, this.selectedDate)
        const schedules = res.data || []
        if (schedules.length > 0) {
          const schedule = schedules[0]
          this.scheduleForm = {
            morning: schedule.morning || false,
            afternoon: schedule.afternoon || false,
            evening: schedule.evening || false
          }
        } else {
          this.scheduleForm = {
            morning: false,
            afternoon: false,
            evening: false
          }
        }
      } catch (error) {
        console.error('加载排班失败:', error)
      }
    },
    
    async saveSchedule() {
      try {
        const scheduleData = {
          doctorId: this.selectedDoctorId,
          scheduleDate: this.selectedDate,
          morning: this.scheduleForm.morning,
          afternoon: this.scheduleForm.afternoon,
          evening: this.scheduleForm.evening
        }
        await scheduleApi.createOrUpdate(scheduleData)
        this.$message.success('排班保存成功')
      } catch (error) {
        this.$message.error('保存失败: ' + (error.message || error))
      }
    },
    
    handleAbsence() {
      this.absenceReason = ''
      this.absenceDialogVisible = true
    },
    
    async confirmAbsence() {
      try {
        if (!this.absenceReason) {
          this.$message.warning('请输入停诊原因')
          return
        }
        await scheduleApi.handleAbsence(this.selectedDoctorId, this.selectedDate, this.absenceReason)
        this.$message.success('停诊处理完成，已生成退费任务')
        this.absenceDialogVisible = false
        this.loadSchedule()
      } catch (error) {
        this.$message.error('操作失败: ' + (error.message || error))
      }
    }
  }
}
</script>

<style scoped>
.schedule-page {
  padding: 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
