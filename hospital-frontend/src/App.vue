<template>
  <div id="app">
    <el-container style="height: 100vh;">
      <el-header style="background-color: #409EFF; color: white; padding: 0 20px; display: flex; align-items: center;">
        <h1 style="margin: 0; font-size: 20px;">医院门诊预约和收费系统</h1>
        <el-menu
          :default-active="activeMenu"
          class="el-menu-demo"
          mode="horizontal"
          background-color="#409EFF"
          text-color="#fff"
          active-text-color="#ffd04b"
          router
          style="margin-left: 30px; border: none;"
        >
          <el-menu-item index="/schedule">医生排班</el-menu-item>
          <el-menu-item index="/appointment">预约挂号</el-menu-item>
          <el-menu-item index="/payment">我的预约</el-menu-item>
          <el-menu-item index="/waiting">候诊队列</el-menu-item>
        </el-menu>
      </el-header>
      <el-main style="background-color: #f5f7fa; padding: 20px;">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'App',
  computed: {
    activeMenu() {
      return this.$route.path
    }
  }
}
</script>

<style>
#app {
  margin: 0;
  padding: 0;
}
</style>
