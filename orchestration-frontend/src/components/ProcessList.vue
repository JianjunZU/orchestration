<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px;">
      <h2>流程定义管理</h2>
      <el-button type="primary" @click="$router.push('/designer')">
        <el-icon><Plus /></el-icon>新建流程
      </el-button>
    </div>

    <el-table :data="processes" v-loading="loading" stripe>
      <el-table-column prop="processKey" label="流程Key" width="200" />
      <el-table-column prop="name" label="名称" width="200" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="version" label="版本" width="80" align="center" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'DEPLOYED' ? 'success' : 'info'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/designer/${row.id}`)">编辑</el-button>
          <el-button size="small" type="success" @click="handleDeploy(row)" :disabled="row.status === 'DEPLOYED'">
            部署
          </el-button>
          <el-button size="small" type="primary" @click="handleStart(row)" :disabled="row.status !== 'DEPLOYED'">
            启动
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Start Process Dialog -->
    <el-dialog v-model="startDialogVisible" title="启动流程实例" width="500px">
      <el-form label-position="top">
        <el-form-item label="Business Key (可选)">
          <el-input v-model="startForm.businessKey" placeholder="业务标识" />
        </el-form-item>
        <el-form-item label="流程变量 (JSON)">
          <el-input
            v-model="startForm.variablesJson"
            type="textarea"
            :rows="5"
            placeholder='{"key": "value"}'
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="startDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doStart">启动</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { processApi, instanceApi } from '../api/index.js'

const processes = ref([])
const loading = ref(false)
const startDialogVisible = ref(false)
const startForm = ref({ processKey: '', businessKey: '', variablesJson: '{}' })

onMounted(() => loadProcesses())

async function loadProcesses() {
  loading.value = true
  try {
    const { data } = await processApi.list()
    processes.value = data
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function handleDeploy(row) {
  try {
    await processApi.deploy(row.id)
    ElMessage.success('部署成功')
    await loadProcesses()
  } catch (e) {
    ElMessage.error('部署失败: ' + (e.response?.data?.error || e.message))
  }
}

function handleStart(row) {
  startForm.value = { processKey: row.processKey, businessKey: '', variablesJson: '{}' }
  startDialogVisible.value = true
}

async function doStart() {
  try {
    let variables = {}
    if (startForm.value.variablesJson) {
      variables = JSON.parse(startForm.value.variablesJson)
    }
    await instanceApi.start({
      processKey: startForm.value.processKey,
      businessKey: startForm.value.businessKey || undefined,
      variables
    })
    ElMessage.success('流程已启动')
    startDialogVisible.value = false
  } catch (e) {
    ElMessage.error('启动失败: ' + (e.response?.data?.error || e.message))
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除流程 "${row.name}" ?`, '确认', { type: 'warning' })
    await processApi.delete(row.id)
    ElMessage.success('已删除')
    await loadProcesses()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>
