<script setup>
import { computed, onMounted, ref } from 'vue'
import axios from '../utils/axios'
import { FileTextOutlined, FolderFilled, SearchOutlined } from '@ant-design/icons-vue'
import { showDateTime } from '@/utils/dateTime'
import { RouterLink } from 'vue-router'

const dataSource = ref([])
const searchKey = ref('')

onMounted(() => {
  axios.get('/api/note/all').then(response => {
    dataSource.value = response.data.data
  })
})

const dataSourceComputed = computed(() => {
  if (searchKey.value.length === 0) {
    return []
  }
  let ds = dataSource.value.filter(node => node.relPath.toLowerCase().includes(searchKey.value.toLowerCase()))
  return ds.sort((a, b) => {
    return a.relPath.localeCompare(b.relPath)
  })
})

const columns = [
  {
    title: '路径',
    dataIndex: 'relPath'
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
  <div style="position: fixed; top: 40px; left: 0; right: 0; z-index: 1000;
       height: 40px; line-height: 40px; padding-left: 24px; padding-right: 24px;
        background-color: #FFFBE6; font-weight: bold;">
    <a-input v-model:value="searchKey" placeholder="最输入关键字搜索">
      <template #prefix>
        <SearchOutlined />
      </template>
    </a-input>
  </div>
  <a-card title=" ">
    <a-table
      :columns="columns"
      :row-key="record => record.relPath"
      :data-source="dataSourceComputed"
      :pagination="false"
      size="small"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'relPath'">
          <RouterLink v-if="record.directory" :to="{path:'/explore', query: {path: record.relPath + '\\'}}">
            <FolderFilled :style="{ color: '#F7C427'}" />
            {{ record.relPath }}
          </RouterLink>
          <RouterLink v-if="!record.directory" :to="{path:'/note', query: {path: record.relPath}}">
            <FileTextOutlined :style="{ color: '#000000'}" />
            {{ record.relPath }}
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
