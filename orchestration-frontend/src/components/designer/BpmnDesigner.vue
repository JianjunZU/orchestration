<template>
  <div>
    <div class="toolbar">
      <el-input
        v-model="processName"
        placeholder="流程名称"
        style="width: 200px"
      />
      <el-input
        v-model="processKey"
        placeholder="流程Key (唯一)"
        style="width: 200px"
        :disabled="!!processId"
      />
      <el-button type="primary" @click="handleSave">
        <el-icon><DocumentChecked /></el-icon>保存
      </el-button>
      <el-button type="success" @click="handleDeploy" :disabled="!processId">
        <el-icon><Upload /></el-icon>部署
      </el-button>
      <el-button @click="handleExportXml">
        <el-icon><Download /></el-icon>导出XML
      </el-button>
      <el-button @click="handleImportXml">
        <el-icon><FolderOpened /></el-icon>导入XML
      </el-button>
      <el-button @click="handleNewProcess">
        <el-icon><Plus /></el-icon>新建
      </el-button>
    </div>

    <div class="bpmn-designer-container">
      <div class="bpmn-canvas" ref="canvasRef"></div>
      <div class="bpmn-properties">
        <PropertiesPanel
          :element="selectedElement"
          :modeler="modeler"
          @update="handlePropertiesUpdate"
        />
      </div>
    </div>

    <!-- Import dialog -->
    <el-dialog v-model="importDialogVisible" title="导入BPMN XML" width="600px">
      <el-input
        v-model="importXml"
        type="textarea"
        :rows="15"
        placeholder="粘贴BPMN XML..."
      />
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doImport">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'
import customModule from './custom/index.js'
import PropertiesPanel from './PropertiesPanel.vue'
import { processApi } from '../../api/index.js'

const route = useRoute()
const router = useRouter()
const canvasRef = ref(null)
const modeler = ref(null)
const selectedElement = ref(null)
const processId = ref(null)
const processName = ref('')
const processKey = ref('')
const importDialogVisible = ref(false)
const importXml = ref('')

const defaultXml = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:flowable="http://flowable.org/bpmn"
  targetNamespace="http://orchestration.com/process">
  <process id="process_1" name="新流程" isExecutable="true">
    <startEvent id="start_1" name="开始" />
  </process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="process_1">
      <bpmndi:BPMNShape id="BPMNShape_start_1" bpmnElement="start_1">
        <dc:Bounds x="180" y="240" width="36" height="36" />
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</definitions>`

onMounted(async () => {
  modeler.value = new BpmnModeler({
    container: canvasRef.value,
    additionalModules: [customModule],
    moddleExtensions: {}
  })

  // Listen for element selection
  modeler.value.on('selection.changed', (e) => {
    selectedElement.value = e.newSelection[0] || null
  })

  // Load existing process or create new
  if (route.params.id) {
    await loadProcess(route.params.id)
  } else {
    await modeler.value.importXML(defaultXml)
    modeler.value.get('canvas').zoom('fit-viewport')
  }
})

onBeforeUnmount(() => {
  if (modeler.value) {
    modeler.value.destroy()
  }
})

watch(() => route.params.id, async (newId) => {
  if (newId) {
    await loadProcess(newId)
  }
})

async function loadProcess(id) {
  try {
    const { data } = await processApi.getById(id)
    processId.value = data.id
    processName.value = data.name
    processKey.value = data.processKey
    await modeler.value.importXML(data.bpmnXml)
    modeler.value.get('canvas').zoom('fit-viewport')
    ElMessage.success('流程加载成功')
  } catch (e) {
    ElMessage.error('加载失败: ' + (e.response?.data?.error || e.message))
  }
}

async function handleSave() {
  if (!processName.value || !processKey.value) {
    ElMessage.warning('请输入流程名称和Key')
    return
  }

  try {
    const { xml } = await modeler.value.saveXML({ format: true })
    const { data } = await processApi.save({
      id: processId.value,
      processKey: processKey.value,
      name: processName.value,
      bpmnXml: xml
    })
    processId.value = data.id
    ElMessage.success('保存成功')
    if (!route.params.id) {
      router.replace(`/designer/${data.id}`)
    }
  } catch (e) {
    ElMessage.error('保存失败: ' + (e.response?.data?.error || e.message))
  }
}

async function handleDeploy() {
  try {
    await processApi.deploy(processId.value)
    ElMessage.success('部署成功')
  } catch (e) {
    ElMessage.error('部署失败: ' + (e.response?.data?.error || e.message))
  }
}

async function handleExportXml() {
  const { xml } = await modeler.value.saveXML({ format: true })
  const blob = new Blob([xml], { type: 'application/xml' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `${processKey.value || 'process'}.bpmn20.xml`
  link.click()
  URL.revokeObjectURL(link.href)
}

function handleImportXml() {
  importDialogVisible.value = true
  importXml.value = ''
}

async function doImport() {
  if (!importXml.value) return
  try {
    await modeler.value.importXML(importXml.value)
    modeler.value.get('canvas').zoom('fit-viewport')
    importDialogVisible.value = false
    ElMessage.success('导入成功')
  } catch (e) {
    ElMessage.error('导入失败: ' + e.message)
  }
}

function handleNewProcess() {
  processId.value = null
  processName.value = ''
  processKey.value = ''
  modeler.value.importXML(defaultXml)
  router.replace('/designer')
}

function handlePropertiesUpdate() {
  // Refresh selected element state after property change
  if (selectedElement.value) {
    const el = selectedElement.value
    selectedElement.value = null
    setTimeout(() => { selectedElement.value = el }, 0)
  }
}
</script>
