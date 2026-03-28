import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('./components/ProcessList.vue')
  },
  {
    path: '/designer/:id?',
    name: 'Designer',
    component: () => import('./components/designer/BpmnDesigner.vue')
  },
  {
    path: '/triggers',
    name: 'Triggers',
    component: () => import('./components/TriggerList.vue')
  },
  {
    path: '/instances',
    name: 'Instances',
    component: () => import('./components/InstanceList.vue')
  }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
