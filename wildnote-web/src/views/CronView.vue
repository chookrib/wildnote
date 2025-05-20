<script setup>
import { computed, onMounted, ref } from 'vue'
import axios from '../utils/axios'
import { RouterLink } from 'vue-router'

const dataSource = ref([])

onMounted(() => {
  axios.get('/api/note/cron').then(response => {
    dataSource.value = response.data.data
  })
})

const sorterParam = ref({})

const dataSourceComputed = computed(() => {
  const sorter = sorterParam.value
  if (sorter && sorter.field && sorter.order) {
    return [...dataSource.value].sort((a, b) => {
      if (sorter.order === 'ascend') {
        return a[sorter.field] > b[sorter.field] ? 1 : -1
      } else {
        return a[sorter.field] < b[sorter.field] ? 1 : -1
      }
    })
  }
  return [...dataSource.value]
})

const handleTableChange = function(pagination, filters, sorter) {
  //console.log('Table changed:', pagination, filters, sorter)
  sorterParam.value = sorter
}

const columns = [
  {
    title: '笔记文件路径',
    dataIndex: 'path',
    sorter: true
  },
  {
    title: '行号',
    dataIndex: 'lineNumber',
    align: 'center'
  },
  {
    title: 'Cron表达式',
    dataIndex: 'cron'
  },
  {
    title: '提醒消息',
    dataIndex: 'message'
  },
  {
    title: '下次执行时间',
    dataIndex: 'nextTime',
    align: 'center'
  }
]
</script>

<template>
  <a-card>
    <a-table
      :columns="columns"
      :row-key="record => record.path + record.lineNumber"
      :data-source="dataSourceComputed"
      :pagination="false"
      @change="handleTableChange"
      size="small"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'path'">
          <RouterLink :to="{path:'/note', query: {path: record.path}}">{{ record.path }}</RouterLink>
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<style scoped>
</style>
