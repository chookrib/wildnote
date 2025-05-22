<script setup>
import { computed, onMounted, ref } from 'vue'
import axios from '../utils/axios'
import { showDateTime } from '@/utils/dateTime'
import { RouterLink, useRoute } from 'vue-router'
import { FileTextOutlined, FolderFilled } from '@ant-design/icons-vue'

const route = useRoute()
const path = route.query.path || ''

const dataSource = ref([])

onMounted(() => {
  axios.get('/api/note/all').then(response => {
    dataSource.value = response.data.data
  })
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
          return b.relPath.localeCompare(a.relPath)
        }
        return a.directory - b.directory
      }
    })
  }
  return ds.sort((a, b) => {
      // 目录在前，文件在后，按名称排序
      if (a.directory === b.directory) {
        return a.relPath.localeCompare(b.relPath)
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
    width: '150px',
    align: 'center'
  }
]
</script>

<template>
  <a-card>
    <template #title>
      <div style="position: fixed; top: 40px; left: 0; right: 0; z-index: 1000;
       height: 40px; line-height: 40px; padding-left: 24px; padding-right: 24px;
        background-color: #FFFBE6; font-weight: bold;">
        <!--<RouterLink :to="{ path: '/explore' }">根</RouterLink>-->
        \<span v-if="path">
          <template v-for="(segment, idx) in path.split('\\').filter(s => s)" :key="idx">
          <RouterLink
            :to="{ path: '/explore', query: { path: path.split('\\').slice(0, idx + 2).join('\\') + '\\' } }">
            {{ segment }}
          </RouterLink>\
        </template>
      </span>
      </div>
    </template>
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
            <FolderFilled :style="{ color: '#F7C427'}" />
            {{ record.name }}
          </RouterLink>
          <RouterLink v-if="!record.directory" :to="{path:'/note', query: {path: record.relPath}}">
            <FileTextOutlined :style="{ color: '#000000'}" />
            {{ record.name }}
          </RouterLink>
        </template>
        <template v-if="column.dataIndex === 'lastModifiedTime'">
          {{ showDateTime(record.lastModifiedTime) }}
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<style scoped>
</style>
