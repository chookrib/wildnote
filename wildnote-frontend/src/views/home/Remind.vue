<script setup lang="ts">
import {computed, onMounted, ref, useCssModule} from 'vue';
import {RouterLink} from 'vue-router';
import axios from '@/utility/axios-utility';
import {formatDuration} from '@/utility/datetime-utility';
import type {ColumnsType} from 'ant-design-vue/es/table';
import type {TablePaginationConfig} from 'ant-design-vue/lib';
import type {FilterValue, SorterResult} from 'ant-design-vue/es/table/interface';

// const activeCronList = ref([]);
// const inactiveCronList = ref([]);
// const dirtyJobList = ref([]);
const activeCronList = ref<Array<{ path: string; delayTime?: number; cronDetail?: string; [key: string]: any }>>([]);
const inactiveCronList = ref<Array<{ path: string; [key: string]: any }>>([]);
const dirtyJobList = ref<Array<{ jobId: string; nextTime?: string; [key: string]: any }>>([]);

onMounted(() => {
  axios.get('/api/remind/all').then((response) => {
    activeCronList.value = response.data.data.activeCronList;
    inactiveCronList.value = response.data.data.inactiveCronList;
    dirtyJobList.value = response.data.data.dirtyJobList;
  });
});

// const activeCronSorterParam = ref({});
const activeCronSorterParam = ref<SorterResult<any>>();

const activeCronListComputed = computed(() => {
  const sorter = activeCronSorterParam.value;
  if (sorter && sorter.field && sorter.order) {
    if (sorter.field === 'path') {
      return [...activeCronList.value].sort((a, b) => {
        if (sorter.order === 'ascend') {
          return a.path.localeCompare(b.path);
        } else {
          return b.path.localeCompare(a.path);
        }
      });
    } else if (sorter.field === 'delayTime' || sorter.field === 'cronDetail') {
      return [...activeCronList.value].sort((a, b) => {
        if (sorter.order === 'ascend') {
          return Number(a.delayTime) > Number(b.delayTime) ? 1 : -1;
        } else {
          return Number(a.delayTime) < Number(b.delayTime) ? 1 : -1;
        }
      });
    }
  }
  return [...activeCronList.value];
});

const handleActiveCornTableChange = (
  pagination: TablePaginationConfig,
  filters: Record<string, FilterValue>,
  sorter: SorterResult<any> | SorterResult<any>[],
) => {
  // console.log('Table changed:', pagination, filters, sorter);
  if (Array.isArray(sorter)) {
    activeCronSorterParam.value = sorter[0];
  } else {
    activeCronSorterParam.value = sorter;
  }
};

const styles = useCssModule();
// console.log(styles);
const activeCronColumns: ColumnsType<any> = [
  {
    title: '笔记文件路径',
    dataIndex: 'path',
    sorter: true,
  },
  {
    title: '行',
    dataIndex: 'lineNumber',
    align: 'center',
    responsive: ['sm'],
  },
  {
    title: 'Cron表达式',
    dataIndex: 'cronDetail',
    sorter: true,
    customCell: (record, rowIndex, column) => {
      return {class: styles['cell-hide-on-sm-up'] + ' ' + styles['col-cron-detail']};
    },
    customHeaderCell: (column) => {
      return {class: styles['cell-hide-on-sm-up']};
    },
  },
  {
    title: 'Cron表达式',
    dataIndex: 'cronExpression',
    responsive: ['sm'],
    //customCell: (record, rowIndex, column) => { return { class: styles['cell-hide-on-xs'] } },
    //customHeaderCell: (column) => { return { class: styles['cell-hide-on-xs'] } },
  },
  {
    title: '下次执行时间',
    dataIndex: 'nextTime',
    align: 'center',
    //ellipsis: true,
    responsive: ['sm'],
    //customCell: (record, rowIndex, column) => { return { class: styles['cell-hide-on-xs'] } },
    //customHeaderCell: (column) => { return { class: styles['cell-hide-on-xs'] } },
  },
  {
    title: '等待时间',
    dataIndex: 'delayTime',
    align: 'right',
    ellipsis: true,
    sorter: true,
    responsive: ['sm'],
    //customCell: (record, rowIndex, column) => { return { class: styles['cell-hide-on-xs'] } },
    //customHeaderCell: (column) => { return { class: styles['cell-hide-on-xs'] } },
  },
  {
    title: '描述',
    dataIndex: 'description',
    //ellipsis: true,
  },
];

const inactiveCronColumns: ColumnsType<any> = [
  {
    title: '笔记文件路径',
    dataIndex: 'path',
    // sorter: true,
  },
  {
    title: '行',
    dataIndex: 'lineNumber',
    align: 'center',
  },
  {
    title: 'Cron表达式',
    dataIndex: 'cronExpression',
  },
  {
    title: '状态',
    dataIndex: 'status',
  },
  {
    title: '描述',
    dataIndex: 'description',
    //ellipsis: true
  },
];

const dirtyJobColumns: ColumnsType<any> = [
  {
    title: '作业Id',
    dataIndex: 'jobId',
    ellipsis: true,
  },
  {
    title: '下次执行时间',
    dataIndex: 'nextTime',
    align: 'center',
    ellipsis: true,
  },
];
</script>

<template>
  <a-card>
    <template #title>运行中笔记提醒作业</template>
    <a-table
      :columns="activeCronColumns"
      :row-key="(record) => record.path + record.lineNumber"
      :data-source="activeCronListComputed"
      :pagination="false"
      @change="handleActiveCornTableChange"
      size="small"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'path'">
          <RouterLink :to="{ path: '/note', query: { path: record.path } }">{{ record.path }}</RouterLink>
        </template>
        <template v-if="column.dataIndex === 'cron'">
          <a-tag>
            {{ record.cronExpression }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'nextTime'">
          {{ record.nextTime }}
        </template>
        <template v-if="column.dataIndex === 'delayTime'">
          {{ formatDuration(record.delayTime) }}
        </template>
        <template v-if="column.dataIndex === 'cronDetail'">
          <a-tag>
            {{ record.cronExpression }}
          </a-tag>
          <br/>{{ record.nextTime }} <br/>{{ formatDuration(record.delayTime) }}
        </template>
      </template>
    </a-table>
  </a-card>

  <a-card>
    <template #title>未运行笔记提醒作业</template>
    <a-table
      :columns="inactiveCronColumns"
      :row-key="(record) => record.path + record.lineNumber"
      :data-source="inactiveCronList"
      :pagination="false"
      size="small"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'path'">
          <RouterLink :to="{ path: '/note', query: { path: record.path } }">{{ record.path }}</RouterLink>
        </template>
        <template v-if="column.dataIndex === 'cron'">
          <a-tag>
            {{ record.cronExpression }}
          </a-tag>
        </template>
      </template>
    </a-table>
  </a-card>

  <a-card v-if="dirtyJobList.length > 0">
    <template #title>笔记提醒作业脏数据</template>
    <a-table
      :columns="dirtyJobColumns"
      :row-key="(record) => record.jobId"
      :data-source="dirtyJobList"
      :pagination="false"
      size="small"
    >
    </a-table>
  </a-card>
</template>

<style scoped></style>

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
