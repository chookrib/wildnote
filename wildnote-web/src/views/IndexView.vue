<script setup>
import { onMounted, ref } from 'vue'
import { FolderFilled, FileTextOutlined } from '@ant-design/icons-vue'
import { getAllPinned, unpin, movePinned } from '@/utils/pinnedPath'
import { RouterLink } from 'vue-router'
import { confirm } from '../utils/confirm'
import axios from '@/utils/axios.js'

const pinnedPaths = ref([])
let remindLog = ref('')
let dragIndex = null

onMounted(() => {
  pinnedPaths.value = getAllPinned()
  axios.get('/api/note/remind').then(response => {
    remindLog.value = response.data.data
  })
})

const onDragStart = (index) => {
  dragIndex = index
}

const onDrop = (dropIndex) => {
  movePinned(dragIndex, dropIndex)
  dragIndex = null
  pinnedPaths.value = getAllPinned()
}

const unpinPath = function(path) {
  confirm(`确定取消在首页固定 ${path} 吗？`, function() {
    unpin(path)
    pinnedPaths.value = getAllPinned()
  })
}
</script>

<template>
  <a-card>
    <a-empty description="没有固定笔记" v-if="pinnedPaths.length === 0" />
    <a-flex wrap="wrap" gap="small" v-if="pinnedPaths.length > 0">
      <a-tag v-for="(path, index) in pinnedPaths"
             :key="index"
             draggable="true"
             @dragstart="onDragStart(index)"
             @dragover.prevent
             @drop="onDrop(index)"
             closable @close.prevent="unpinPath(path)">
        <template #icon>
          <template v-if="path.endsWith('\\')">
            <FolderFilled :style="{ color: '#F7C427'}" />
          </template>
          <template v-if="!path.endsWith('\\')">
            <FileTextOutlined />
          </template>
        </template>
        <template v-if="path.endsWith('\\')">
          <RouterLink :to="{ path: '/explore', query: { path: path } }">
            {{ path }}&nbsp;&nbsp;
          </RouterLink>
        </template>
        <template v-if="!path.endsWith('\\')">
          <RouterLink :to="{ path: '/note', query: { path: path } }">
            {{ path }}&nbsp;&nbsp;
          </RouterLink>
        </template>
      </a-tag>
    </a-flex>
  </a-card>
  <a-card>
    <template #title>
      最新提醒
    </template>
    <div class="log">
      {{ remindLog }}
    </div>
  </a-card>
</template>

<style scoped>
.log{
  white-space: pre-wrap;
  word-wrap: anywhere;
  font-size: 12px;
}
</style>
