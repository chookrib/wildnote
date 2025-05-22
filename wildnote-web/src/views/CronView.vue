<script setup>
import { computed, onMounted, ref } from 'vue'
import axios from '../utils/axios'
import { RouterLink } from 'vue-router'
import { showDateTime, showTime } from '@/utils/dateTime.js'

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
    if (sorter.field === 'path') {
      return [...dataSource.value].sort((a, b) => {
        if (sorter.order === 'ascend') {
          return a.path.localeCompare(b.path)
        } else {
          return b.path.localeCompare(a.path)
        }
      })
    } else if (sorter.field === 'delayTime') {
      return [...dataSource.value].sort((a, b) => {
        if (sorter.order === 'ascend') {
          return Number(a.delayTime) > Number(b.delayTime) ? 1 : -1
        } else {
          return Number(a.delayTime) < Number(b.delayTime) ? 1 : -1
        }
      })
    }
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
    title: '行',
    dataIndex: 'lineNumber',
    align: 'center'
  },
  {
    title: 'Cron表达式',
    dataIndex: 'cron'
  },
  {
    title: '提醒消息',
    dataIndex: 'message',
    ellipsis: true
  },
  {
    title: '下次执行时间',
    dataIndex: 'nextTime',
    align: 'center',
    ellipsis: true
  },
  {
    title: '等待时间',
    dataIndex: 'delayTime',
    align: 'right',
    ellipsis: true,
    sorter: true
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
        <template v-if="column.dataIndex === 'cron'">
          <a-tag>
            {{ record.cron }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'nextTime'">
          {{ showDateTime(record.nextTime) }}
        </template>
        <template v-if="column.dataIndex === 'delayTime'">
          {{ showTime(record.delayTime) }}
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<style scoped>
</style>
