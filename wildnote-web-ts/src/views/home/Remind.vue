<script setup lang="ts">
import { computed, onMounted, ref, useCssModule } from 'vue';
import { RouterLink } from 'vue-router';
import axios from '@/utility/axios-utility';
import { formatDuration } from '@/utility/datetime-utility';
import type { ColumnsType } from 'ant-design-vue/es/table';
import type { TablePaginationConfig } from 'ant-design-vue/lib';
import type { FilterValue, SorterResult } from 'ant-design-vue/es/table/interface';

// const scheduledCrons = ref([]);
// const unscheduledCrons = ref([]);
// const isolatedJobs = ref([]);
const scheduledCronList = ref<Array<{ path: string; delayTime?: number; cronDetail?: string; [key: string]: any }>>([]);
const unscheduledCronList = ref<Array<{ path: string; [key: string]: any }>>([]);
const isolatedJobList = ref<Array<{ jobId: string; nextTime?: string; [key: string]: any }>>([]);

onMounted(() => {
  axios.get('/api/remind/all').then((response) => {
    scheduledCronList.value = response.data.data.scheduledCronList;
    unscheduledCronList.value = response.data.data.unscheduledCronList;
    isolatedJobList.value = response.data.data.isolatedJobList;
  });
});

// const scheduledCronSorterParam = ref({});
const scheduledCronSorterParam = ref<SorterResult<any>>();

const scheduledCronListComputed = computed(() => {
  const sorter = scheduledCronSorterParam.value;
  if (sorter && sorter.field && sorter.order) {
    if (sorter.field === 'path') {
      return [...scheduledCronList.value].sort((a, b) => {
        if (sorter.order === 'ascend') {
          return a.path.localeCompare(b.path);
        } else {
          return b.path.localeCompare(a.path);
        }
      });
    } else if (sorter.field === 'delayTime' || sorter.field === 'cronDetail') {
      return [...scheduledCronList.value].sort((a, b) => {
        if (sorter.order === 'ascend') {
          return Number(a.delayTime) > Number(b.delayTime) ? 1 : -1;
        } else {
          return Number(a.delayTime) < Number(b.delayTime) ? 1 : -1;
        }
      });
    }
  }
  return [...scheduledCronList.value];
});

const handleScheduledCornTableChange = (
  pagination: TablePaginationConfig,
  filters: Record<string, FilterValue>,
  sorter: SorterResult<any> | SorterResult<any>[],
) => {
  // console.log('Table changed:', pagination, filters, sorter);
  if (Array.isArray(sorter)) {
    scheduledCronSorterParam.value = sorter[0];
  } else {
    scheduledCronSorterParam.value = sorter;
  }
};

const styles = useCssModule();
// console.log(styles);
const scheduledCronColumns: ColumnsType<any> = [
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
      return { class: styles['cell-hide-on-sm-up'] + ' ' + styles['col-cron-detail'] };
    },
    customHeaderCell: (column) => {
      return { class: styles['cell-hide-on-sm-up'] };
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

const unscheduledCronColumns: ColumnsType<any> = [
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
    title: '描述',
    dataIndex: 'description',
    //ellipsis: true
  },
];

const isolatedJobColumns: ColumnsType<any> = [
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
    <template #title>已调度笔记提醒作业</template>
    <a-table
      :columns="scheduledCronColumns"
      :row-key="(record) => record.path + record.lineNumber"
      :data-source="scheduledCronListComputed"
      :pagination="false"
      @change="handleScheduledCornTableChange"
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
          {{ record.nextTime??'已结束' }}
        </template>
        <template v-if="column.dataIndex === 'delayTime'">
          {{ formatDuration(record.delayTime)??'已结束' }}
        </template>
        <template v-if="column.dataIndex === 'cronDetail'">
          <a-tag>
            {{ record.cronExpression }}
          </a-tag>
          <br />{{ record.nextTime }} <br />{{ formatDuration(record.delayTime) }}
        </template>
      </template>
    </a-table>
  </a-card>

  <a-card>
    <template #title>未调度笔记提醒作业</template>
    <a-table
      :columns="unscheduledCronColumns"
      :row-key="(record) => record.path + record.lineNumber"
      :data-source="unscheduledCronList"
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

  <a-card v-if="isolatedJobList.length > 0">
    <template #title>残留笔记提醒作业</template>
    <a-table
      :columns="isolatedJobColumns"
      :row-key="(record) => record.jobId"
      :data-source="isolatedJobList"
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
