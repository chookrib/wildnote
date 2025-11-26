<script setup lang="ts">
import { computed, onMounted, ref, useCssModule } from 'vue';
import { RouterLink } from 'vue-router';
import axios from '@/utility/axios-utility';
import { showDateTime, showTime } from '@/utility/datetime-utility';
import type { ColumnsType } from 'ant-design-vue/es/table';
import type { TablePaginationConfig } from 'ant-design-vue/lib';
import type { FilterValue, SorterResult } from 'ant-design-vue/es/table/interface';

// const scheduledCrons = ref([]);
// const unscheduledCrons = ref([]);
// const remainJobs = ref([]);
const scheduledCrons = ref<Array<{ path: string; delayTime?: number; cronDetail?: string; [key: string]: any }>>([]);
const unscheduledCrons = ref<Array<{ path: string; [key: string]: any }>>([]);
const remainJobs = ref<Array<{ jobId: string; nextTime?: string; [key: string]: any }>>([]);

onMounted(() => {
  axios.get('/api/remind/all').then((response) => {
    scheduledCrons.value = response.data.data.scheduledCrons;
    unscheduledCrons.value = response.data.data.unscheduledCrons;
    remainJobs.value = response.data.data.remainJobs;
  });
});

// const sorterParam = ref({});
const sorterParam = ref<SorterResult<any>>();

const scheduledCronsComputed = computed(() => {
  const sorter = sorterParam.value;
  if (sorter && sorter.field && sorter.order) {
    if (sorter.field === 'path') {
      return [...scheduledCrons.value].sort((a, b) => {
        if (sorter.order === 'ascend') {
          return a.path.localeCompare(b.path);
        } else {
          return b.path.localeCompare(a.path);
        }
      });
    } else if (sorter.field === 'delayTime' || sorter.field === 'cronDetail') {
      return [...scheduledCrons.value].sort((a, b) => {
        if (sorter.order === 'ascend') {
          return Number(a.delayTime) > Number(b.delayTime) ? 1 : -1;
        } else {
          return Number(a.delayTime) < Number(b.delayTime) ? 1 : -1;
        }
      });
    }
  }
  return [...scheduledCrons.value];
});

const handleTableChange = (
  pagination: TablePaginationConfig,
  filters: Record<string, FilterValue>,
  sorter: SorterResult<any> | SorterResult<any>[],
) => {
  // console.log('Table changed:', pagination, filters, sorter);
  if (Array.isArray(sorter)) {
    sorterParam.value = sorter[0];
  } else {
    sorterParam.value = sorter;
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
const remainJobColumns: ColumnsType<any> = [
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
    <template #title>已调度提醒计划任务</template>
    <a-table
      :columns="scheduledCronColumns"
      :row-key="(record) => record.path + record.lineNumber"
      :data-source="scheduledCronsComputed"
      :pagination="false"
      @change="handleTableChange"
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
          {{ showTime(record.delayTime) }}
        </template>
        <template v-if="column.dataIndex === 'cronDetail'">
          <a-tag>
            {{ record.cronExpression }}
          </a-tag>
          <br />{{ record.nextTime }} <br />{{ showTime(record.delayTime) }}
        </template>
      </template>
    </a-table>
  </a-card>
  <a-card>
    <template #title>未调度提醒计划任务</template>
    <a-table
      :columns="unscheduledCronColumns"
      :row-key="(record) => record.path + record.lineNumber"
      :data-source="unscheduledCrons"
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
  <a-card v-if="remainJobs.length > 0">
    <template #title>残留提醒计划任务作业</template>
    <a-table
      :columns="remainJobColumns"
      :row-key="(record) => record.jobId"
      :data-source="remainJobs"
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
