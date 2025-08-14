<script setup>
import { computed, onMounted, ref, useCssModule } from 'vue'
import axios from '@/utility/axios-utility.js'
import { RouterLink } from 'vue-router'
import { showDateTime, showTime } from '@/utility/datetime-utility.js'

const dataSource = ref([])
const dataSourceFailed = ref([])

onMounted(() => {
  axios.get('/api/note/cron').then(response => {
    dataSource.value = response.data.data
  })
  axios.get('/api/note/cron/failed').then(response => {
    dataSourceFailed.value = response.data.data
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
    } else if (sorter.field === 'delayTime' || sorter.field === 'cronDetail') {
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

const styles = useCssModule()
//console.log(styles)
const columns = [
  {
    title: '笔记文件路径',
    dataIndex: 'path',
    sorter: true
  },
  {
    title: '行',
    dataIndex: 'lineNumber',
    align: 'center',
    responsive: ['sm']
  },
  {
    title: 'Cron表达式',
    dataIndex: 'cronDetail',
    sorter: true,
    customCell: (record, rowIndex, column) => { return { class: styles['cell-hide-on-sm-up'] + ' ' + styles['col-cron-detail']  } },
    customHeaderCell: (column) => { return { class: styles['cell-hide-on-sm-up'] } },
  },
  {
    title: 'Cron表达式',
    dataIndex: 'cron',
    responsive: ['sm'],
    //customCell: (record, rowIndex, column) => { return { class: styles['cell-hide-on-xs'] } },
    //customHeaderCell: (column) => { return { class: styles['cell-hide-on-xs'] } }
  },
  {
    title: '下次执行时间',
    dataIndex: 'nextTime',
    align: 'center',
    //ellipsis: true,
    responsive: ['sm'],
    //customCell: (record, rowIndex, column) => { return { class: styles['cell-hide-on-xs'] } },
    //customHeaderCell: (column) => { return { class: styles['cell-hide-on-xs'] } }
  },
  {
    title: '等待时间',
    dataIndex: 'delayTime',
    align: 'right',
    ellipsis: true,
    sorter: true,
    responsive: ['sm'],
    //customCell: (record, rowIndex, column) => { return { class: styles['cell-hide-on-xs'] } },
    //customHeaderCell: (column) => { return { class: styles['cell-hide-on-xs'] } }
  },
  {
    title: '提醒消息',
    dataIndex: 'message',
    //ellipsis: true
  }
]
const columnsFailed = [
  {
    title: '笔记文件路径',
    dataIndex: 'path',
    // sorter: true
  },
  {
    title: '行',
    dataIndex: 'lineNumber',
    align: 'center',
  },
  {
    title: 'Cron表达式',
    dataIndex: 'cron',
  },
  {
    title: '提醒消息',
    dataIndex: 'message',
    //ellipsis: true
  }
]
</script>

<template>
  <a-card>
    <template #title>运行中提醒</template>
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
        <template v-if="column.dataIndex === 'cronDetail'">
          <a-tag>
            {{ record.cron }}
          </a-tag>
          <br>{{ showDateTime(record.nextTime) }}
          <br>{{ showTime(record.delayTime) }}
        </template>
      </template>
    </a-table>
  </a-card>
  <a-card>
    <template #title>失败的提醒</template>
    <a-table
      :columns="columnsFailed"
      :row-key="record => record.path + record.lineNumber"
      :data-source="dataSourceFailed"
      :pagination="false"
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
      </template>
    </a-table>
  </a-card>
</template>

<style scoped>
</style>

<style module>
  .cell-hide-on-xs {
    @media (max-width: 575.98px) {
        display: none;
    }
  }
  .cell-hide-on-sm-up {
    @media (min-width: 576px) {
      display: none;
    }
  }
  .col-cron-detail {
    white-space: nowrap;
    overflow: hidden;
    font-size: 12px;
  }
</style>
