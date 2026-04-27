<script setup lang="ts" name="Search">
import {computed, onMounted, ref} from 'vue';

// defineOptions({
//   name: 'Search'
// })

import {RouterLink} from 'vue-router';
import {FileTextOutlined, FolderFilled, SearchOutlined} from '@ant-design/icons-vue';
import axios from '@/utility/axios-utility';
import type {ColumnsType} from 'ant-design-vue/es/table';

// const dataSource = ref([]);
const dataSource = ref<Array<{ path: string; lastModifiedTime: string; directory: boolean }>>([]);
const searchType = ref('path');
const searchKey = ref('');

onMounted(() => {
  // axios.get('/api/explore/all').then((response) => {
  //   dataSource.value = response.data.data.list;
  // });
});

const dataSourceComputed = computed(() => {
  // if (searchKey.value.length === 0) {
  //   return [];
  // }
  // const ds = dataSource.value.filter((node) => node.path.toLowerCase().includes(searchKey.value.toLowerCase()));
  // return ds.sort((a, b) => {
  //   return a.path.localeCompare(b.path);
  // });
  return dataSource.value.sort((a, b) => {
    return a.path.localeCompare(b.path);
  });
});

const columns: ColumnsType<any> = [
  {
    title: '路径',
    dataIndex: 'path',
  },
  {
    title: '修改时间',
    dataIndex: 'lastModifiedTime',
    width: '160px',
    align: 'center',
    responsive: ['sm'],
  },
];

const search = () => {
  axios
    .post(searchType.value === 'content' ? '/api/search/content' : '/api/search/path',
      {keyword: searchKey.value})
    .then((response) => {
      dataSource.value = response.data.data.list;
    });
};
</script>

<template>
  <div class="search-header">
    <a-space>
      <a-input v-model:value="searchKey" placeholder="输入关键字搜索" :allow-clear="true">
        <template #prefix>
          <SearchOutlined/>
        </template>
      </a-input>
      <a-select v-model:value="searchType">
        <a-select-option value="path">路径</a-select-option>
        <a-select-option value="content">内容</a-select-option>
      </a-select>
      <a-button @click="search()">搜索</a-button>
    </a-space>
  </div>
  <a-card>
    <a-table
      :columns="columns"
      :row-key="(record) => record.path"
      :data-source="dataSourceComputed"
      :pagination="false"
      size="small"
    >
      <template #emptyText>
        <a-empty description="没有搜索结果"/>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'path'">
          <RouterLink v-if="record.directory" :to="{ path: '/explore', query: { path: record.path + '\\' } }">
            <FolderFilled :style="{ color: '#f7c427' }"/>
            {{ record.path }}
          </RouterLink>
          <RouterLink v-if="!record.directory" :to="{ path: '/note', query: { path: record.path } }">
            <FileTextOutlined :style="{ color: '#000000' }"/>
            {{ record.path }}
          </RouterLink>
        </template>
        <template v-if="column.dataIndex === 'lastModifiedTime'">
          {{ record.lastModifiedTime }}
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<style scoped>
.search-header {
  background-color: #fffbe6;
  /*font-weight: bold;*/
  padding-left: 24px;
  padding-right: 24px;
  height: 40px;
  line-height: 40px;
  position: sticky;
  top: 40px;
  left: 0;
  right: 0;
  z-index: 1000;
  display: flex;
}

.search-header * {
  /*font-weight: bold;*/
}
</style>
