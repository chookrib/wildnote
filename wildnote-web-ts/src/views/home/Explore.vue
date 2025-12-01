<script setup lang="ts">
import {computed, onMounted, ref} from 'vue';
import {RouterLink, useRoute} from 'vue-router';
import {FileTextOutlined, FolderFilled, StarFilled, StarOutlined,} from '@ant-design/icons-vue';
import axios from '@/utility/axios-utility';
import * as localStorageUtility from '@/utility/local-storage-utility';
import type {FilterValue, SorterResult} from 'ant-design-vue/es/table/interface';
import type {TablePaginationConfig} from 'ant-design-vue/lib';
import type {ColumnsType} from 'ant-design-vue/es/table';
import router from '@/router.ts';
import {showConfirm} from '@/utility/confirm-utility.ts';
import dayjs from 'dayjs';

const route = useRoute();
const explorePath = (route.query.path as string) || '\\';
const isFavorite = ref(false);
// const dataSource = ref([]);
const dataSource = ref<Array<{ path: string; lastModifiedTime: string; level: number; directory: boolean }>>([]);

onMounted(() => {
  loadAllNote();
  isFavorite.value = localStorageUtility.isFavorite(explorePath);
});

const loadAllNote = () => {
  axios.get('/api/explore/all').then((response) => {
    dataSource.value = response.data.data.list;
  });
};

// const sorterParam = ref({});
const sorterParam = ref<SorterResult<any>>();

const dataSourceComputed = computed(() => {
  let ds = [];
  if (explorePath) {
    const level = explorePath.split('\\').length - 2;
    ds = dataSource.value.filter((node) => node.path.startsWith(explorePath) && node.level === level);
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
    width: '140px',
    align: 'center',
  },
];

const setFavorite = () => {
  localStorageUtility.addFavorite(explorePath);
  isFavorite.value = true;
};

const unsetFavorite = () => {
  localStorageUtility.deleteFavorite(explorePath);
  isFavorite.value = false;
};

const createDirectory = ref(false);
const createPanelVisible = ref(false);
const createPath = ref('');

const openCreatePanel = (directory: boolean) => {
  createDirectory.value = directory;
  createPanelVisible.value = true;
  createPath.value = explorePath + dayjs().format('YYYYMMDD_HHmmss') + (directory ? '' : '.md');
};

const createNote = () => {
  if (createDirectory.value) {
    axios.post('/api/explore/create-directory', {path: createPath.value}).then((response) => {
      createPanelVisible.value = false;
      loadAllNote();
    });
  } else {
    axios.post('/api/explore/create-file', {path: createPath.value}).then((response) => {
      createPanelVisible.value = false;
      // 创建笔记文件后转到笔记文件编辑页面
      router.push({path: '/note', query: {path: createPath.value, edit: 'true'}});
    });
  }
};

const deleteNote = (path: string, directory: boolean) => {
  showConfirm({
    title: directory ? '删除笔记文件夹' : '删除笔记文件',
    content: `确定要删除笔记${directory ? '文件夹' : '文件'} ${path} 吗？`,
    onOk: () => {
      axios
        .post(directory ? '/api/explore/delete-directory' : '/api/explore/delete-file', {path: path})
        .then((response) => {
          loadAllNote();
        });
    }
  });
};

const moveDirectory = ref(false);
const movePanelVisible = ref(false);
const moveSourcePath = ref('');
const moveTargetPath = ref('');

const openMovePanel = (path: string, directory: boolean) => {
  moveDirectory.value = directory;
  movePanelVisible.value = true;
  moveSourcePath.value = path;
  moveTargetPath.value = path;
};

const moveNote = () => {
  axios
    .post(moveDirectory.value ? '/api/explore/move-directory' : '/api/explore/move-file', {
      sourcePath: moveSourcePath.value,
      targetPath: moveTargetPath.value,
    })
    .then((response) => {
      movePanelVisible.value = false;
      loadAllNote();
    });
};
</script>

<template>

  <div class="explore-header">
    <div>
      <StarOutlined @click="setFavorite" v-if="!isFavorite"/>
      <StarFilled @click="unsetFavorite" v-if="isFavorite" style="color: #1677ff;"/>
    </div>
    <div class="explore-header-title">
      <RouterLink :to="{ path: '/explore' }">根</RouterLink>
      <span>\</span>
      <span v-if="explorePath">
      <template v-for="(segment, index) in explorePath.split('\\').filter((s) => s)" :key="index">
        <RouterLink
          :to="{
            path: '/explore',
            query: { path: explorePath.split('\\').slice(0, index + 2).join('\\') + '\\' },
          }">
          {{ segment }} </RouterLink>\</template>
    </span>
    </div>
  </div>

  <a-card>
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
          <a-button type="link" @click="openMovePanel(record.path, record.directory)">移动</a-button>
          <a-button type="link" @click="deleteNote(record.path, record.directory)">删除</a-button>
        </template>
      </template>
    </a-table>
  </a-card>

  <div class="explore-footer">
    <a-button type="primary" @click="openCreatePanel(true)">
      创建笔记文件夹
    </a-button>
    <a-button type="primary" @click="openCreatePanel(false)">
      创建笔记文件
    </a-button>
  </div>

  <a-modal
    v-model:open="createPanelVisible"
    :title="createDirectory ? '创建笔记文件夹' : '创建笔记文件'"
    @ok="createNote"
  >
    <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
      <a-form-item label="创建于">
        <a-input v-model:value="createPath"></a-input>
      </a-form-item>
    </a-form>
  </a-modal>

  <a-modal v-model:open="movePanelVisible" :title="moveDirectory ? '移动笔记文件夹' : '移动笔记文件'" @ok="moveNote">
    <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
      <a-form-item label="当前路径">
        {{ moveSourcePath }}
      </a-form-item>
      <a-form-item label="移动到">
        <a-input v-model:value="moveTargetPath"></a-input>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<style scoped>
.explore-header {
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
  align-items: center;
  justify-content: left;
}

.explore-header-title {
  white-space: nowrap;
  margin-left: 5px;
}

.explore-footer {
  position: sticky;
  bottom: 0;
  text-align: center;
  padding: 10px 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
</style>
