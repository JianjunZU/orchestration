<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px;">
      <h2>运行实例</h2>
      <div>
        <el-radio-group v-model="viewMode" @change="loadInstances" style="margin-right: 16px;">
          <el-radio-button value="running">运行中</el-radio-button>
          <el-radio-button value="history">历史记录</el-radio-button>
        </el-radio-group>
        <el-input
          v-model="filterKey"
          placeholder="按流程Key筛选"
          style="width: 200px"
          clearable
          @change="loadInstances"
        />
      </div>
    </div>

    <el-table :data="instances" v-loading="loading" stripe>
      <el-table-column prop="processInstanceId" label="实例ID" width="280" />
      <el-table-column prop="processDefinitionKey" label="流程Key" width="200" />
      <el-table-column prop="businessKey" label="Business Key" width="200" />
      <el-table-column prop="status" label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'RUNNING' ? 'success' : 'info'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="200" />
      <el-table-column prop="endTime" label="结束时间" width="200" />
      <el-table-column label="操作" width="150" align="center">
        <template #default="{ row }">
          <el-button
            size="small"
            type="danger"
            @click="handleStop(row)"
            :disabled="row.status !== 'RUNNING'"
          >
            停止
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { instanceApi } from '../api/index.js'

const instances = ref([])
const loading = ref(false)
const viewMode = ref('running')
const filterKey = ref('')

onMounted(() => loadInstances())

async function loadInstances() {
  loading.value = true
  try {
    const key = filterKey.value || undefined
    const { data } = viewMode.value === 'running'
      ? await instanceApi.listRunning(key)
      : await instanceApi.listHistory(key)
    instances.value = data
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function handleStop(row) {
  try {
    await ElMessageBox.confirm('确定停止此流程实例?', '确认', { type: 'warning' })
    await instanceApi.stop(row.processInstanceId, '手动停止')
    ElMessage.success('已停止')
    await loadInstances()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}
</script>
