/**
 * Adds custom context pad entries for orchestration elements.
 */
export default function CustomContextPadProvider(contextPad, modeling, elementFactory, create, translate) {
  this._modeling = modeling
  this._elementFactory = elementFactory
  this._create = create
  this._translate = translate

  contextPad.registerProvider(this)
}

CustomContextPadProvider.$inject = [
  'contextPad',
  'modeling',
  'elementFactory',
  'create',
  'translate'
]

CustomContextPadProvider.prototype.getContextPadEntries = function (element) {
  const { _modeling: modeling, _elementFactory: elementFactory, _create: create, _translate: translate } = this

  function appendExecutor(event, element) {
    const shape = elementFactory.createShape({ type: 'bpmn:ServiceTask' })
    if (shape.businessObject) {
      shape.businessObject.$attrs = shape.businessObject.$attrs || {}
      shape.businessObject.$attrs['orchestration:elementType'] = 'executor'
    }
    create.start(event, shape, { source: element })
  }

  function appendLogic(event, element) {
    const shape = elementFactory.createShape({ type: 'bpmn:ServiceTask' })
    if (shape.businessObject) {
      shape.businessObject.$attrs = shape.businessObject.$attrs || {}
      shape.businessObject.$attrs['orchestration:elementType'] = 'logic'
    }
    create.start(event, shape, { source: element })
  }

  return {
    'append.executor': {
      group: 'model',
      className: 'bpmn-icon-service-task',
      title: translate('Append Executor'),
      action: { click: appendExecutor, dragstart: appendExecutor }
    },
    'append.logic': {
      group: 'model',
      className: 'bpmn-icon-service-task',
      title: translate('Append Logic Controller'),
      action: { click: appendLogic, dragstart: appendLogic }
    }
  }
}
