<template>
  <div class="waiting-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>候诊队列</span>
          <el-select
            v-model="selectedDoctorId"
            placeholder="选择医生筛选"
            clearable
            style="width: 200px;"
            @change="loadWaitingQueue"
          >
            <el-option
              v-for="doctor in doctors"
              :key="doctor.id"
              :label="doctor.name + ' - ' + doctor.title"
              :value="doctor.id"
            />
          </el-select>
          <el-button type="primary" @click="loadWaitingQueue">刷新</el-button>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="24" v-for="(queues, doctorId) in groupedQueues" :key="doctorId">
          <div class="doctor-header">
            <el-tag type="primary" size="large">
              {{ getDoctorName(parseInt(doctorId)) }} - {{ getDoctorTitle(parseInt(doctorId)) }}
            </el-tag>
            <span class="queue-count">当前候诊: {{ queues.length }} 人</span>
          </div>
          
          <el-table :data="queues" border style="margin-bottom: 20px;">
            <el-table-column prop="queueNumber" label="排队号" width="100">
              <template #default="scope">
                <span style="font-size: 24px; font-weight: bold; color: #409EFF;">
                  {{ scope.row.queueNumber }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="patientName" label="患者姓名" width="150">
              <template #default="scope">
                <span style="font-size: 18px;">{{ scope.row.patientName }}</span>
              </template>
            </el-table-column>
            <el-table-column label="预约信息" width="300">
              <template #default="scope">
                <div>预约号: {{ scope.row.appointmentId }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="joinTime" label="加入时间" width="200" />
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                  {{ scope.row.status === 1 ? '候诊中' : '已离开' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="位置" width="100">
              <template #default="scope">
                <el-tag v-if="getPosition(scope.row, queues) === 1" type="danger">
                  当前就诊
                </el-tag>
                <el-tag v-else-if="getPosition(scope.row, queues) === 2" type="warning">
                  下一位
                </el-tag>
                <span v-else>
                  第 {{ getPosition(scope.row, queues) }} 位
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
      
      <el-empty v-if="Object.keys(groupedQueues).length === 0 && !loading" description="当前没有候诊患者" />
      
      <el-card v-if="allQueues.length > 0" style="margin-top: 20px;">
        <template #header>
          <span>大屏显示模式</span>
        </template>
        <div class="big-screen">
          <div class="screen-header">
            <h2>医院门诊候诊叫号系统</h2>
          </div>
          <div class="screen-content">
            <div class="current-calling" v-if="currentCalling">
              <div class="calling-label">当前就诊</div>
              <div class="calling-number">{{ currentCalling.queueNumber }}</div>
              <div class="calling-patient">{{ currentCalling.patientName }}</div>
              <div class="calling-doctor">{{ getDoctorName(currentCalling.doctorId) }}</div>
            </div>
            <div class="waiting-list" v-else>
              <div class="calling-label">暂无患者就诊</div>
            </div>
          </div>
          <div class="screen-footer">
            <div class="next-patients">
              <span>下一位: </span>
              <span v-if="nextPatients.length > 0" class="next-patient">
                {{ nextPatients.slice(0, 3).map(p => `${p.queueNumber}号 ${p.patientName}`).join(', ') }}
              </span>
              <span v-else>暂无</span>
            </div>
          </div>
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script>
import { waitingQueueApi, doctorApi } from '../api'

export default {
  name: 'Waiting',
  data() {
    return {
      doctors: [],
      allQueues: [],
      selectedDoctorId: null,
      loading: false
    }
  },
  computed: {
    groupedQueues() {
      const groups = {}
      let queues = this.allQueues
      
      if (this.selectedDoctorId) {
        queues = queues.filter(q => q.doctorId === this.selectedDoctorId)
      }
      
      queues.forEach(q => {
        const doctorId = q.doctorId
        if (!groups[doctorId]) {
          groups[doctorId] = []
        }
        groups[doctorId].push(q)
      })
      
      return groups
    },
    currentCalling() {
      const firstQueues = []
      for (const doctorId in this.groupedQueues) {
        if (this.groupedQueues[doctorId].length > 0) {
          firstQueues.push(this.groupedQueues[doctorId][0])
        }
      }
      return firstQueues[0] || null
    },
    nextPatients() {
      const nexts = []
      for (const doctorId in this.groupedQueues) {
        if (this.groupedQueues[doctorId].length > 1) {
          nexts.push(this.groupedQueues[doctorId][1])
        }
      }
      return nexts
    }
  },
  created() {
    this.loadDoctors()
    this.loadWaitingQueue()
  },
  methods: {
    async loadDoctors() {
      try {
        const res = await doctorApi.getAll()
        this.doctors = res.data || []
      } catch (error) {
        console.error('加载医生列表失败:', error)
      }
    },
    
    async loadWaitingQueue() {
      this.loading = true
      try {
        const res = await waitingQueueApi.getAll()
        this.allQueues = (res.data || []).filter(q => q.status === 1)
      } catch (error) {
        this.$message.error('加载候诊队列失败')
      } finally {
        this.loading = false
      }
    },
    
    getDoctorName(doctorId) {
      const doctor = this.doctors.find(d => d.id === doctorId)
      return doctor ? doctor.name : '未知医生'
    },
    
    getDoctorTitle(doctorId) {
      const doctor = this.doctors.find(d => d.id === doctorId)
      return doctor ? doctor.title : ''
    },
    
    getPosition(item, list) {
      return list.findIndex(q => q.id === item.id) + 1
    }
  }
}
</script>

<style scoped>
.waiting-page {
  padding: 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}
.doctor-header {
  display: flex;
  align-items: center;
  margin: 20px 0 10px 0;
}
.queue-count {
  margin-left: 20px;
  color: #909399;
}
.big-screen {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: white;
  border-radius: 8px;
  padding: 20px;
}
.screen-header {
  text-align: center;
  border-bottom: 2px solid rgba(255, 255, 255, 0.3);
  padding-bottom: 20px;
  margin-bottom: 20px;
}
.screen-header h2 {
  margin: 0;
  font-size: 28px;
  letter-spacing: 5px;
}
.screen-content {
  text-align: center;
  padding: 40px 0;
}
.calling-label {
  font-size: 24px;
  margin-bottom: 20px;
  color: #ffd700;
}
.calling-number {
  font-size: 120px;
  font-weight: bold;
  color: #ffd700;
  text-shadow: 0 0 20px rgba(255, 215, 0, 0.5);
}
.calling-patient {
  font-size: 32px;
  margin-top: 20px;
}
.calling-doctor {
  font-size: 20px;
  margin-top: 10px;
  color: #90caf9;
}
.screen-footer {
  border-top: 2px solid rgba(255, 255, 255, 0.3);
  padding-top: 20px;
  margin-top: 20px;
}
.next-patients {
  font-size: 20px;
  text-align: center;
}
.next-patient {
  color: #ffd700;
  font-weight: bold;
}
</style>
