/**
 * Custom palette provider that adds Trigger, Logic Controller, and Executor
 * elements to the bpmn.js palette. These map to Flowable ServiceTask with
 * custom extension properties.
 */
export default function CustomPaletteProvider(palette, create, elementFactory, translate) {
  this._create = create
  this._elementFactory = elementFactory
  this._translate = translate

  palette.registerProvider(this)
}

CustomPaletteProvider.$inject = [
  'palette',
  'create',
  'elementFactory',
  'translate'
]

CustomPaletteProvider.prototype.getPaletteEntries = function () {
  const { _create: create, _elementFactory: elementFactory, _translate: translate } = this

  function createAction(type, group, className, title, options = {}) {
    function createListener(event) {
      const shape = elementFactory.createShape({
        type: 'bpmn:ServiceTask',
        ...options
      })

      // Attach custom type info via businessObject
      if (shape.businessObject) {
        shape.businessObject.$attrs = shape.businessObject.$attrs || {}
        shape.businessObject.$attrs['orchestration:elementType'] = type
        if (options.executorType) {
          shape.businessObject.$attrs['orchestration:executorType'] = options.executorType
        }
        if (options.logicType) {
          shape.businessObject.$attrs['orchestration:logicType'] = options.logicType
        }
      }

      create.start(event, shape)
    }

    return {
      group,
      className,
      title: translate(title),
      action: { dragstart: createListener, click: createListener }
    }
  }

  return {
    'create.kafka-trigger': createAction(
      'trigger', 'orchestration', 'bpmn-icon-start-event-message',
      'Kafka Trigger (Message Start Event)',
      {}
    ),
    'create.http-executor': createAction(
      'executor', 'orchestration', 'bpmn-icon-service-task',
      'HTTP Executor',
      { executorType: 'HTTP' }
    ),
    'create.script-executor': createAction(
      'executor', 'orchestration', 'bpmn-icon-service-task',
      'Script Executor',
      { executorType: 'SCRIPT' }
    ),
    'create.condition-logic': createAction(
      'logic', 'orchestration', 'bpmn-icon-service-task',
      'Condition Logic Controller',
      { logicType: 'CONDITION' }
    ),
    'create.data-transform-logic': createAction(
      'logic', 'orchestration', 'bpmn-icon-service-task',
      'Data Transform Logic',
      { logicType: 'DATA_TRANSFORM' }
    )
  }
}
