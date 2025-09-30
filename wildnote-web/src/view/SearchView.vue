<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { FileTextOutlined, FolderFilled, SearchOutlined } from '@ant-design/icons-vue'
import axios from '@/utility/axios-utility.js'
import { showDateTime } from '@/utility/datetime-utility.js'

const dataSource = ref([])
const searchKey = ref('')

onMounted(() => {
  axios.get('/api/note/all').then(response => {
    dataSource.value = response.data.data.list
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
    width: '160px',
    align: 'center',
    responsive: ['sm']
  }
]
</script>

<template>
  <div class="fixed-title">
    <a-input v-model:value="searchKey" placeholder="最输入关键字搜索" size="small" :allow-clear="true">
      <template #prefix>
        <SearchOutlined />
      </template>
    </a-input>
  </div>
  <a-card style="margin-top: 40px;">
    <a-table
      :columns="columns"
      :row-key="record => record.relPath"
      :data-source="dataSourceComputed"
      :pagination="false"
      size="small"
    >
      <template #emptyText>
        <a-empty description="没有搜索结果" />
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'relPath'">
          <RouterLink v-if="record.directory" :to="{path:'/explore', query: {path: record.relPath + '\\'}}">
            <FolderFilled :style="{ color: '#f7c427'}" />
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
