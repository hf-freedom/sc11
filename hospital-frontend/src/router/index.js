import Vue from 'vue'
import VueRouter from 'vue-router'
import Schedule from '../views/Schedule.vue'
import Appointment from '../views/Appointment.vue'
import Payment from '../views/Payment.vue'
import Waiting from '../views/Waiting.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/appointment'
  },
  {
    path: '/schedule',
    name: 'Schedule',
    component: Schedule
  },
  {
    path: '/appointment',
    name: 'Appointment',
    component: Appointment
  },
  {
    path: '/payment',
    name: 'Payment',
    component: Payment
  },
  {
    path: '/waiting',
    name: 'Waiting',
    component: Waiting
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
