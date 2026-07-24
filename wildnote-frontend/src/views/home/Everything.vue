<script setup lang="ts">
import {computed, onMounted, ref} from 'vue';

import {RouterLink} from 'vue-router';
import {FileTextOutlined, FolderFilled, SearchOutlined} from '@ant-design/icons-vue';
import axios from '@/utility/axios-utility';
import type {ColumnsType} from 'ant-design-vue/es/table';

// const dataSource = ref([]);
const dataSource = ref<Array<{ type: string; name: string; path: string, size: string, date_modified: string }>>([]);
const searchKey = ref('');
const downloadToken = ref('');
const downloadUrlRoot = import.meta.env.VITE_API_URL;

onMounted(() => {
});

const columns: ColumnsType<any> = [
  {
    title: '路径',
    dataIndex: 'path',
  }
];

const search = () => {
  axios
    .post('/api/search/everything',{keyword: searchKey.value})
    .then((response) => {
      dataSource.value = response.data.data.list;
      downloadToken.value = response.data.data.downloadToken;
    });
};
</script>

<template>
  <div class="search-header">
    <a-space>
      <a-input v-model:value="searchKey" placeholder="输入关键字搜索文件" :allow-clear="true">
        <template #prefix>
          <SearchOutlined/>
        </template>
      </a-input>
      <a-button @click="search()">搜索</a-button>
    </a-space>
  </div>
  <a-card>
    <a-table
      :columns="columns"
      :row-key="(record) => record.path"
      :data-source="dataSource"
      :pagination="false"
      size="small"
    >
      <template #emptyText>
        <a-empty description="没有搜索结果"/>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'path'">
          <div style="word-break: break-all;" v-if="record.type==='file'">
            <a :href="downloadUrlRoot+'api/search/everything/download?path='+encodeURIComponent(record.path+'\\'+record.name)+'&token='+downloadToken">
              {{ record.path+'\\'+record.name }}
              <a-tag>{{record.size}}</a-tag>
            </a>
          </div>
          <div style="word-break: break-all;" v-if="record.type==='folder'">
            {{ record.path+'\\'+record.name }}
          </div>
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
