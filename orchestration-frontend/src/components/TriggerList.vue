<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px;">
      <h2>触发器管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新建触发器
      </el-button>
    </div>

    <el-table :data="triggers" v-loading="loading" stripe>
      <el-table-column prop="name" label="名称" width="200" />
      <el-table-column prop="triggerType" label="类型" width="150">
        <template #default="{ row }">
          <el-tag :type="typeColor(row.triggerType)">{{ row.triggerType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="triggerSource" label="触发源" width="250" />
      <el-table-column prop="processKey" label="目标流程" width="200" />
      <el-table-column prop="filterExpression" label="过滤表达式" />
      <el-table-column prop="enabled" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch :model-value="row.enabled" @change="handleToggle(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑触发器' : '新建触发器'" width="550px">
      <el-form :model="form" label-position="top">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="触发器名称" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.triggerType" style="width: 100%;">
            <el-option label="Kafka消息" value="KAFKA_MESSAGE" />
            <el-option label="信号" value="SIGNAL" />
            <el-option label="消息" value="MESSAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发源" required>
          <el-input v-model="form.triggerSource"
            :placeholder="form.triggerType === 'KAFKA_MESSAGE' ? 'Kafka Topic名称' : '信号/消息名称'" />
        </el-form-item>
        <el-form-item label="目标流程Key" required>
          <el-input v-model="form.processKey" placeholder="流程定义Key" />
        </el-form-item>
        <el-form-item label="过滤表达式 (可选)">
          <el-input v-model="form.filterExpression" placeholder="SpEL表达式" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { triggerApi } from '../api/index.js'

const triggers = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const form = ref({ name: '', triggerType: 'KAFKA_MESSAGE', triggerSource: '', processKey: '', filterExpression: '' })

onMounted(() => loadTriggers())

async function loadTriggers() {
  loading.value = true
  try {
    const { data } = await triggerApi.list()
    triggers.value = data
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function typeColor(type) {
  return { KAFKA_MESSAGE: 'warning', SIGNAL: 'success', MESSAGE: 'primary' }[type] || 'info'
}

function openDialog(row) {
  if (row) {
    editingId.value = row.id
    form.value = { ...row }
  } else {
    editingId.value = null
    form.value = { name: '', triggerType: 'KAFKA_MESSAGE', triggerSource: '', processKey: '', filterExpression: '' }
  }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    await triggerApi.save({ ...form.value, id: editingId.value, enabled: true })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadTriggers()
  } catch (e) {
    ElMessage.error('保存失败: ' + (e.response?.data?.error || e.message))
  }
}

async function handleToggle(row) {
  try {
    await triggerApi.toggle(row.id)
    await loadTriggers()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除触发器 "${row.name}" ?`, '确认', { type: 'warning' })
    await triggerApi.delete(row.id)
    ElMessage.success('已删除')
    await loadTriggers()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}
</script>
