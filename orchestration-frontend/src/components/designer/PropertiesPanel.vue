<template>
  <div class="properties-panel" v-if="element">
    <h3 style="margin-bottom: 16px; color: #303133;">属性面板</h3>

    <!-- Common Properties -->
    <el-form label-position="top" size="small">
      <el-form-item label="ID">
        <el-input :model-value="elementId" @change="updateId" />
      </el-form-item>
      <el-form-item label="名称">
        <el-input :model-value="elementName" @change="updateName" />
      </el-form-item>

      <!-- Element Type Display -->
      <el-form-item label="BPMN类型">
        <el-tag>{{ element.type }}</el-tag>
      </el-form-item>

      <!-- Orchestration Element Type -->
      <el-form-item label="编排类型" v-if="isServiceTask">
        <el-select v-model="orchType" @change="updateOrchType" placeholder="选择编排类型">
          <el-option label="执行器 (Executor)" value="executor" />
          <el-option label="逻辑控制器 (Logic)" value="logic" />
          <el-option label="普通ServiceTask" value="" />
        </el-select>
      </el-form-item>

      <!-- Executor Properties -->
      <template v-if="orchType === 'executor'">
        <el-divider>执行器配置</el-divider>
        <el-form-item label="执行器类型">
          <el-select v-model="executorType" @change="updateExecutorType">
            <el-option label="HTTP 执行器" value="HTTP" />
            <el-option label="Script 执行器" value="SCRIPT" />
          </el-select>
        </el-form-item>
        <el-form-item label="Delegate Expression">
          <el-input :model-value="delegateExpr" disabled />
        </el-form-item>
        <el-form-item label="执行器配置 (JSON)">
          <el-input
            v-model="executorConfig"
            type="textarea"
            :rows="5"
            @change="updateExecutorConfig"
            placeholder='{"url":"http://...","method":"POST"}'
          />
        </el-form-item>
      </template>

      <!-- Logic Controller Properties -->
      <template v-if="orchType === 'logic'">
        <el-divider>逻辑控制器配置</el-divider>
        <el-form-item label="逻辑类型">
          <el-select v-model="logicType" @change="updateLogicType">
            <el-option label="条件判断" value="CONDITION" />
            <el-option label="数据转换" value="DATA_TRANSFORM" />
          </el-select>
        </el-form-item>
        <el-form-item label="Delegate Expression">
          <el-input :model-value="logicDelegateExpr" disabled />
        </el-form-item>
        <el-form-item label="逻辑配置 (JSON)">
          <el-input
            v-model="logicConfig"
            type="textarea"
            :rows="5"
            @change="updateLogicConfig"
            placeholder='{"expression":"${amount > 1000}","resultVariable":"result"}'
          />
        </el-form-item>
      </template>

      <!-- Start Event with Message/Signal -->
      <template v-if="isStartEvent">
        <el-divider>触发器配置</el-divider>
        <el-form-item label="触发方式">
          <el-select v-model="startEventType" @change="updateStartEventType">
            <el-option label="普通启动" value="none" />
            <el-option label="消息触发" value="message" />
            <el-option label="信号触发" value="signal" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="startEventType === 'message'" label="消息名称">
          <el-input v-model="messageName" @change="updateMessageName" placeholder="kafka-topic-name" />
        </el-form-item>
        <el-form-item v-if="startEventType === 'signal'" label="信号名称">
          <el-input v-model="signalName" @change="updateSignalName" placeholder="signal-name" />
        </el-form-item>
      </template>

      <!-- Sequence Flow Condition -->
      <template v-if="isSequenceFlow">
        <el-divider>条件表达式</el-divider>
        <el-form-item label="条件">
          <el-input
            v-model="conditionExpression"
            @change="updateConditionExpression"
            placeholder="${conditionResult == true}"
          />
        </el-form-item>
      </template>
    </el-form>
  </div>
  <div v-else class="properties-panel empty">
    <el-empty description="选择一个元素查看属性" :image-size="80" />
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

const props = defineProps({
  element: Object,
  modeler: Object
})

const emit = defineEmits(['update'])

const orchType = ref('')
const executorType = ref('HTTP')
const executorConfig = ref('')
const logicType = ref('CONDITION')
const logicConfig = ref('')
const startEventType = ref('none')
const messageName = ref('')
const signalName = ref('')
const conditionExpression = ref('')

const isServiceTask = computed(() => props.element?.type === 'bpmn:ServiceTask')
const isStartEvent = computed(() => props.element?.type === 'bpmn:StartEvent')
const isSequenceFlow = computed(() => props.element?.type === 'bpmn:SequenceFlow')

const elementId = computed(() => props.element?.businessObject?.id || '')
const elementName = computed(() => props.element?.businessObject?.name || '')

const delegateExpr = computed(() => '${executorDelegate}')
const logicDelegateExpr = computed(() => '${logicControllerDelegate}')

watch(() => props.element, (el) => {
  if (!el) return
  const bo = el.businessObject
  const attrs = bo.$attrs || {}

  orchType.value = attrs['orchestration:elementType'] || ''
  executorType.value = attrs['orchestration:executorType'] || 'HTTP'
  executorConfig.value = attrs['orchestration:executorConfig'] || ''
  logicType.value = attrs['orchestration:logicType'] || 'CONDITION'
  logicConfig.value = attrs['orchestration:logicConfig'] || ''

  // Detect start event type
  if (isStartEvent.value) {
    const eventDefs = bo.eventDefinitions || []
    if (eventDefs.length > 0) {
      const def = eventDefs[0]
      if (def.$type === 'bpmn:MessageEventDefinition') {
        startEventType.value = 'message'
        messageName.value = def.messageRef?.name || ''
      } else if (def.$type === 'bpmn:SignalEventDefinition') {
        startEventType.value = 'signal'
        signalName.value = def.signalRef?.name || ''
      }
    } else {
      startEventType.value = 'none'
    }
  }

  // Sequence flow condition
  if (isSequenceFlow.value) {
    conditionExpression.value = bo.conditionExpression?.body || ''
  }
}, { immediate: true })

function getModeling() {
  return props.modeler?.get('modeling')
}

function updateId(val) {
  getModeling()?.updateProperties(props.element, { id: val })
}

function updateName(val) {
  getModeling()?.updateProperties(props.element, { name: val })
}

function updateOrchType(val) {
  const bo = props.element.businessObject
  bo.$attrs = bo.$attrs || {}
  bo.$attrs['orchestration:elementType'] = val

  if (val === 'executor') {
    getModeling()?.updateProperties(props.element, {
      'flowable:delegateExpression': '${executorDelegate}'
    })
  } else if (val === 'logic') {
    getModeling()?.updateProperties(props.element, {
      'flowable:delegateExpression': '${logicControllerDelegate}'
    })
  }
  emit('update')
}

function updateExecutorType(val) {
  const bo = props.element.businessObject
  bo.$attrs['orchestration:executorType'] = val
  emit('update')
}

function updateExecutorConfig(val) {
  const bo = props.element.businessObject
  bo.$attrs['orchestration:executorConfig'] = val
  emit('update')
}

function updateLogicType(val) {
  const bo = props.element.businessObject
  bo.$attrs['orchestration:logicType'] = val
  emit('update')
}

function updateLogicConfig(val) {
  const bo = props.element.businessObject
  bo.$attrs['orchestration:logicConfig'] = val
  emit('update')
}

function updateStartEventType(type) {
  // Note: changing start event types requires moddle manipulation
  // For simplicity we store as custom attributes
  const bo = props.element.businessObject
  bo.$attrs = bo.$attrs || {}
  bo.$attrs['orchestration:triggerType'] = type
  emit('update')
}

function updateMessageName(val) {
  const bo = props.element.businessObject
  bo.$attrs['orchestration:messageName'] = val
  emit('update')
}

function updateSignalName(val) {
  const bo = props.element.businessObject
  bo.$attrs['orchestration:signalName'] = val
  emit('update')
}

function updateConditionExpression(val) {
  const moddle = props.modeler?.get('moddle')
  const modeling = getModeling()
  if (val) {
    const condExpr = moddle.create('bpmn:FormalExpression', { body: val })
    modeling?.updateProperties(props.element, { conditionExpression: condExpr })
  } else {
    modeling?.updateProperties(props.element, { conditionExpression: undefined })
  }
}
</script>

<style scoped>
.properties-panel {
  min-height: 400px;
}
.properties-panel.empty {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
