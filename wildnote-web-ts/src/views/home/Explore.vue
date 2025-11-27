<script setup lang="ts">
import {computed, onMounted, ref} from 'vue';
import {RouterLink, useRoute} from 'vue-router';
import {FileTextOutlined, FolderFilled, PlusOutlined, StarFilled, StarOutlined, PlusSquareOutlined} from '@ant-design/icons-vue';
import axios from '@/utility/axios-utility';
import * as localStorageUtility from '@/utility/local-storage-utility';
import type {FilterValue, SorterResult} from 'ant-design-vue/es/table/interface';
import type {TablePaginationConfig} from 'ant-design-vue/lib';
import type {ColumnsType} from 'ant-design-vue/es/table';
import router from "@/router.ts";
import {showConfirm} from "@/utility/confirm-utility.ts";

const route = useRoute();
const path = (route.query.path as string) || '\\';
const isFavorite = ref(false);
// const dataSource = ref([]);
const dataSource = ref<Array<{ path: string; lastModifiedTime: string; level: number; directory: boolean }>>([]);

onMounted(() => {
  loadNotes();
  isFavorite.value = localStorageUtility.isFavoriteNotePath(path);
});

const loadNotes = () => {
  axios.get('/api/note/all').then((response) => {
    dataSource.value = response.data.data.list;
  });
}

// const sorterParam = ref({});
const sorterParam = ref<SorterResult<any>>();

const dataSourceComputed = computed(() => {
  let ds = [];
  if (path) {
    const level = path.split('\\').length - 2;
    ds = dataSource.value.filter((node) => node.path.startsWith(path) && node.level === level);
  } else {
    ds = dataSource.value.filter((node) => node.level === 0);
  }
  const sorter = sorterParam.value;
  if (sorter && sorter.field && sorter.order && sorter.order === 'descend') {
    return ds.sort((a, b) => {
      if (a.directory === b.directory) {
        return b.path.localeCompare(a.path, 'en');
      }
      return Number(a.directory) - Number(b.directory);
    });
  }
  return ds.sort((a, b) => {
    // 目录在前，文件在后，按名称排序
    if (a.directory === b.directory) {
      return a.path.localeCompare(b.path, 'en');
    }
    return Number(b.directory) - Number(a.directory);
  });
});

const handleTableChange = (
  pagination: TablePaginationConfig,
  filters: Record<string, FilterValue>,
  sorter: SorterResult<any> | SorterResult<any>[],
) => {
  if (Array.isArray(sorter)) {
    sorterParam.value = sorter[0];
  } else {
    sorterParam.value = sorter;
  }
};

const columns: ColumnsType<any> = [
  {
    title: '路径',
    dataIndex: 'path',
    sorter: true,
    sortDirections: ['descend'],
  },
  {
    title: '修改时间',
    dataIndex: 'lastModifiedTime',
    width: '160px',
    align: 'center',
    responsive: ['sm'],
  },
  {
    title: '操作',
    key: 'action',
    width: '100px',
    align: 'center',
  }
];

const addFavorite = () => {
  localStorageUtility.addFavoritePath(path);
  isFavorite.value = true;
};

const delFavorite = () => {
  localStorageUtility.delFavoriteNotePath(path);
  isFavorite.value = false;
};

const createNote = () => {
  axios
    .post('/api/note/create', {path: path})
    .then((response) => {
      router.push({path: '/note', query: {path: response.data.data.path, edit: 'true'}});
    });
};

const deleteNote = (path: string) => {
  showConfirm(`确定要删除笔记 ${path} 吗？`, () => {
    axios
      .post('/api/note/delete', {path: path})
      .then((response) => {
        loadNotes();
      });
  });
}

const createNoteFolder = () => {
  axios
    .post('/api/note/create-folder', {path: path})
    .then((response) => {
      loadNotes();
    });
};

const deleteNoteFolder = (path: string) => {
  showConfirm(`确定要删除笔记文件夹 ${path} 吗？`, () => {
    axios
      .post('/api/note/delete-folder', {path: path})
      .then((response) => {
        loadNotes();
      });
  });
}
</script>

<template>
  <div class="fixed-title">
    <RouterLink :to="{ path: '/explore' }">根</RouterLink>
    \<span v-if="path">
      <template v-for="(segment, index) in path.split('\\').filter((s) => s)" :key="index">
        <RouterLink
          :to="{
            path: '/explore',
            query: {
              path:
                path
                  .split('\\')
                  .slice(0, index + 2)
                  .join('\\') + '\\',
            },
          }">
          {{ segment }} </RouterLink>\</template>
    </span>
  </div>
  <a-card style="margin-top: 40px">
    <a-table
      :columns="columns"
      :row-key="(record) => record.path"
      :data-source="dataSourceComputed"
      :pagination="false"
      @change="handleTableChange"
      size="small"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'path'">
          <RouterLink v-if="record.directory" :to="{ path: '/explore', query: { path: record.path + '\\' } }">
            <FolderFilled :style="{ color: '#f7c427' }"/>
            {{ record.name }}
          </RouterLink>
          <RouterLink v-if="!record.directory" :to="{ path: '/note', query: { path: record.path } }">
            <FileTextOutlined/>
            {{ record.name }}
          </RouterLink>
        </template>
        <template v-if="column.dataIndex === 'lastModifiedTime'">
          {{ record.lastModifiedTime }}
        </template>
        <template v-if="column.key === 'action'">
          <a-button v-if="record.directory === true" type="link" @click="deleteNoteFolder(record.path)">删除</a-button>
          <a-button v-if="record.directory === false" type="link" @click="deleteNote(record.path)">删除</a-button>
        </template>
      </template>
    </a-table>
  </a-card>
  <a-float-button type="primary" @click="createNoteFolder" style="right: 135px">
    <template #icon>
      <PlusSquareOutlined />
    </template>
  </a-float-button>
  <a-float-button type="primary" @click="createNote" style="right: 80px">
    <template #icon>
      <PlusOutlined/>
    </template>
  </a-float-button>
  <a-float-button type="default" @click="addFavorite" v-if="!isFavorite">
    <template #icon>
      <StarOutlined/>
    </template>
  </a-float-button>
  <a-float-button type="primary" @click="delFavorite" v-if="isFavorite">
    <template #icon>
      <StarFilled/>
    </template>
  </a-float-button>
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
