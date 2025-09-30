<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { FolderFilled, FileTextOutlined, StarFilled, StarOutlined } from '@ant-design/icons-vue'
import axios from '@/utility/axios-utility.js'
import * as localStorageUtility from '@/utility/local-storage-utility.js'

const route = useRoute()
const path = route.query.path || '\\'
const isFavorite = ref(false)
const dataSource = ref([])

onMounted(() => {
  axios.get('/api/note/all').then(response => {
    dataSource.value = response.data.data.list
  })
  isFavorite.value = localStorageUtility.isFavoriteNotePath(path)
})

const sorterParam = ref({})

const dataSourceComputed = computed(() => {
  let ds = []
  if (path) {
    const level = path.split('\\').length - 2
    ds = dataSource.value.filter(node => node.relPath.startsWith(path) && node.level === level)
  } else {
    ds = dataSource.value.filter(node => node.level === 0)
  }
  const sorter = sorterParam.value
  if (sorter && sorter.field && sorter.order) {
    return ds.sort((a, b) => {
      if (sorter.order === 'descend') {
        if (a.directory === b.directory) {
          return b.relPath.localeCompare(a.relPath, 'en')
        }
        return a.directory - b.directory
      }
    })
  }
  return ds.sort((a, b) => {
    // 目录在前，文件在后，按名称排序
    if (a.directory === b.directory) {
      return a.relPath.localeCompare(b.relPath, 'en')
    }
    return b.directory - a.directory
  })
})

const handleTableChange = function(pagination, filters, sorter) {
  sorterParam.value = sorter
}

const columns = [
  {
    title: '路径',
    dataIndex: 'relPath',
    sorter: true,
    sortDirections: ['descend']
  },
  {
    title: '修改时间',
    dataIndex: 'lastModifiedTime',
    width: '160px',
    align: 'center',
    responsive: ['sm']
  }
]

const addFavorite = function() {
  localStorageUtility.addFavoritePath(path)
  isFavorite.value = true
}

const dropFavorite = function() {
  localStorageUtility.dropFavoriteNotePath(path)
  isFavorite.value = false
}
</script>

<template>
  <div class="fixed-title">
    <!--<RouterLink :to="{ path: '/explore' }">根</RouterLink>-->
    \<span v-if="path">
          <template v-for="(segment, index) in path.split('\\').filter(s => s)" :key="index">
          <RouterLink
            :to="{ path: '/explore', query: { path: path.split('\\').slice(0, index + 2).join('\\') + '\\' } }">
            {{ segment }}
          </RouterLink>\</template>
      </span>
  </div>
  <a-card style="margin-top: 40px;">
    <a-table
      :columns="columns"
      :row-key="record => record.relPath"
      :data-source="dataSourceComputed"
      :pagination="false"
      @change="handleTableChange"
      size="small"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'relPath'">
          <RouterLink v-if="record.directory" :to="{path:'/explore', query: {path: record.relPath + '\\'}}">
            <FolderFilled :style="{ color: '#f7c427'}" />
            {{ record.name }}
          </RouterLink>
          <RouterLink v-if="!record.directory" :to="{path:'/note', query: {path: record.relPath}}">
            <FileTextOutlined />
            {{ record.name }}
          </RouterLink>
        </template>
        <template v-if="column.dataIndex === 'lastModifiedTime'">
          {{ record.lastModifiedTime }}
        </template>
      </template>
    </a-table>
  </a-card>
  <a-float-button type="default" @click="addFavorite" v-if="!isFavorite">
    <template #icon>
      <StarOutlined />
    </template>
  </a-float-button>
  <a-float-button type="primary" @click="dropFavorite" v-if="isFavorite">
    <template #icon>
      <StarFilled />
    </template>
  </a-float-button>
</template>

<style scoped>
.fixed-title {
  background-color: #fffbe6;
  /*font-weight: bold;*/
  padding-left: 24px;
  padding-right: 24px;
  height: 40px;
  line-height: 40px;
  position: fixed;
  top: 40px;
  left: 0;
  right: 0;
  z-index: 1000;
}

.fixed-title * {
  /*font-weight: bold;*/
}
</style>
