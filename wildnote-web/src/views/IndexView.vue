<script setup>
import { onMounted, ref } from 'vue'
import { FolderFilled, FileTextOutlined } from '@ant-design/icons-vue'
import { getLocalPinnedPaths, localUnpinPath, localMovePinnedPath } from '@/utils/localStorageUtil'
import { RouterLink } from 'vue-router'
import { showConfirm } from '@/utils/confirmUtil'
import axios from '@/utils/axiosUtil'

const pinnedPaths = ref([])
let remindLog = ref('')
let dragIndex = null

onMounted(() => {
  pinnedPaths.value = getLocalPinnedPaths()
  axios.get('/api/note/remind').then(response => {
    //remindLog.value = response.data.data
    //remindLog.value = response.data.data.replace(/(\\[^|]+)/g, '<a href="#/note?path=$1">$1</a>')
    remindLog.value = response.data.data.replace(/\s(\\[^|]+)/g, (match, p1) => `<a href="#/note?path=${encodeURIComponent(p1.trim())}">${p1}</a>`)
  })
})

const onDragStart = (index) => {
  dragIndex = index
}

const onDrop = (dropIndex) => {
  localMovePinnedPath(dragIndex, dropIndex)
  dragIndex = null
  pinnedPaths.value = getLocalPinnedPaths()
}

const unpinPath = function(path) {
  showConfirm(`确定取消在首页固定 ${path} 吗？`, function() {
    localUnpinPath(path)
    pinnedPaths.value = getLocalPinnedPaths()
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
             closable
             @close.prevent="unpinPath(path)">
        <template #icon>
          <template v-if="path.endsWith('\\')">
            <FolderFilled :style="{ color: '#f7c427'}" />
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
    <div class="log" v-html="remindLog">
    </div>
  </a-card>
</template>

<style scoped>
.log{
  white-space: pre-wrap;
  word-wrap: anywhere;
  font-size: 12px;
}
.ant-tag {
  font-size: 14px;
  padding: 4px;
}
</style>
