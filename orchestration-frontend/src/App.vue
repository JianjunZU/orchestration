<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <div class="header-left">
        <h1 class="logo">Orchestration</h1>
        <span class="subtitle">流程编排平台</span>
      </div>
      <el-menu
        :default-active="currentRoute"
        mode="horizontal"
        router
        class="header-menu"
      >
        <el-menu-item index="/">
          <el-icon><List /></el-icon>流程管理
        </el-menu-item>
        <el-menu-item index="/designer">
          <el-icon><Edit /></el-icon>流程设计
        </el-menu-item>
        <el-menu-item index="/triggers">
          <el-icon><Bell /></el-icon>触发器
        </el-menu-item>
        <el-menu-item index="/instances">
          <el-icon><Monitor /></el-icon>运行实例
        </el-menu-item>
      </el-menu>
    </el-header>
    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const currentRoute = computed(() => route.path)
</script>

<style scoped>
.app-container {
  height: 100vh;
}
.app-header {
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: baseline;
  margin-right: 40px;
}
.logo {
  font-size: 20px;
  font-weight: 700;
  color: #409eff;
  margin: 0;
}
.subtitle {
  font-size: 13px;
  color: #909399;
  margin-left: 10px;
}
.header-menu {
  border-bottom: none;
}
.app-main {
  background: #f5f7fa;
  padding: 20px;
  overflow: auto;
}
</style>
