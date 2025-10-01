<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { message } from 'ant-design-vue'
import { FolderFilled, FileTextOutlined, DownloadOutlined, UploadOutlined } from '@ant-design/icons-vue'
import axios from '@/utility/axios-utility.js'
import * as localStorageUtility from '@/utility/local-storage-utility.js'
import { showConfirm } from '@/utility/confirm-utility.js'

const favoriteNotePaths = ref([])
// let remindLog = ref('')
let dragIndex = null

onMounted(() => {
  favoriteNotePaths.value = localStorageUtility.getFavoriteNotePaths()
  // axios.get('/api/log/get?type=remind').then(response => {
  //   //remindLog.value = response.data.data.result.log
  //   //remindLog.value = response.data.data.replace(/(\\[^|]+)/g, '<a href="#/note?path=$1">$1</a>')
  //   remindLog.value = response.data.data.replace(/(\s\\[^|]+)/g,
  //     (match, p1) => `<a href="#/note?path=${encodeURIComponent(p1.trim())}">${p1}</a>`)
  // })
})

const onDragStart = (index) => {
  dragIndex = index
}

const onDrop = (dropIndex) => {
  localStorageUtility.moveFavoritePath(dragIndex, dropIndex)
  dragIndex = null
  favoriteNotePaths.value = localStorageUtility.getFavoriteNotePaths()
}

const dropFavorite = function(path) {
  showConfirm(`确定取消在首页固定 ${path} 吗？`, function() {
    localStorageUtility.dropFavoriteNotePath(path)
    favoriteNotePaths.value = localStorageUtility.getFavoriteNotePaths()
  })
}

const downloadFavorite = function() {
  showConfirm(`确定下载收藏的笔记路径吗？（将覆盖当前收藏的笔记路径）`, function() {
    axios.get('/api/note/favorite/get').then(response => {
      favoriteNotePaths.value = response.data.data.paths
      localStorageUtility.setFavoriteNotePaths(favoriteNotePaths.value)
      message.success('下载收藏的笔记路径成功')
    })
  })
}

const uploadFavorite = function() {
  showConfirm(`确定上传收藏的笔记路径吗？（将覆盖服务端收藏的笔记路径）`, function() {
    axios.post('/api/note/favorite/set', {
      paths: favoriteNotePaths.value
    }).then(response => {
      message.success('上传收藏的笔记路径成功')
    })
  })
}
</script>

<template>
  <a-card>
    <a-empty description="没有固定笔记" v-if="favoriteNotePaths.length === 0" />
    <a-flex wrap="wrap" gap="small" v-if="favoriteNotePaths.length > 0">
      <a-tag v-for="(path, index) in favoriteNotePaths"
             :key="index"
             draggable="true"
             @dragstart="onDragStart(index)"
             @dragover.prevent
             @drop="onDrop(index)"
             closable
             @close.prevent="dropFavorite(path)">
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
  <!--<a-card>
    <template #title>
      最新提醒日志
    </template>
    <div class="log" v-html="remindLog">
    </div>
  </a-card>-->
  <a-float-button type="primary" @click="downloadFavorite" style="right: 80px;">
    <template #icon>
      <DownloadOutlined />
    </template>
  </a-float-button>
  <a-float-button type="primary" @click="uploadFavorite">
    <template #icon>
      <UploadOutlined />
    </template>
  </a-float-button>
</template>

<style scoped>
/*.log{
  white-space: pre-wrap;
  word-wrap: anywhere;
  font-size: 12px;
}*/
.ant-tag {
  font-size: 14px;
  padding: 4px;
}
</style>
