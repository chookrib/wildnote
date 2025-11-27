<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { message } from 'ant-design-vue';
import { FolderFilled, FileTextOutlined, DownloadOutlined, UploadOutlined, PlusOutlined } from '@ant-design/icons-vue';
import axios from '@/utility/axios-utility';
import * as localStorageUtility from '@/utility/local-storage-utility';
import { showConfirm } from '@/utility/confirm-utility';
import router from '@/router';

// const favoriteNotePathsRemote = ref([]);
// const favoriteNotePaths = ref([]);
const favoriteNotePathsRemote = ref<string[]>([]);
const favoriteNotePaths = ref<string[]>([]);
// let remindLog = ref('');
let dragIndex: number | null = null;

onMounted(() => {
  favoriteNotePaths.value = localStorageUtility.getFavoriteNotePaths();
  // axios.get('/api/log/get?type=remind').then(response => {
  //   //remindLog.value = response.data.data.result.log;
  //   //remindLog.value = response.data.data.replace(/(\\[^|]+)/g, '<a href="#/note?path=$1">$1</a>');
  //   remindLog.value = response.data.data.replace(/(\s\\[^|]+)/g,
  //     (match, p1) => `<a href="#/note?path=${encodeURIComponent(p1.trim())}">${p1}</a>`);
  // });
  loadFavoriteNotePathRemote();
});

const loadFavoriteNotePathRemote = () => {
  axios.get('/api/note/favorite/get').then((response) => {
    favoriteNotePathsRemote.value = response.data.data.paths;
  });
};

const onDragStart = (index: number) => {
  dragIndex = index;
};

const onDrop = (dropIndex: number) => {
  if (dragIndex) {
    localStorageUtility.moveFavoritePath(dragIndex, dropIndex);
    dragIndex = null;
  }
  favoriteNotePaths.value = localStorageUtility.getFavoriteNotePaths();
};

const delFavorite = (path: string) => {
  showConfirm(`确定删除本地收藏的笔记路径 ${path} 吗？`, () => {
    localStorageUtility.delFavoriteNotePath(path);
    favoriteNotePaths.value = localStorageUtility.getFavoriteNotePaths();
  });
};

const downloadFavorite = () => {
  showConfirm(`确定下载服务端收藏的笔记路径到本地吗？（将覆盖本地收藏的笔记路径）`, () => {
    axios.get('/api/note/favorite/get').then((response) => {
      favoriteNotePaths.value = response.data.data.paths;
      localStorageUtility.setFavoriteNotePaths(favoriteNotePaths.value);
      message.success('下载笔记路径成功');
    });
  });
};

const uploadFavorite = () => {
  showConfirm(`确定上传本地收藏的笔记路径到服务端吗？（将覆盖服务端收藏的笔记路径）`, () => {
    axios
      .post('/api/note/favorite/set', {
        paths: favoriteNotePaths.value,
      })
      .then((response) => {
        message.success('上传笔记路径成功');
        loadFavoriteNotePathRemote();
      });
  });
};
</script>

<template>
  <a-card>
    <template #title>本地收藏笔记路径</template>
    <a-empty description="没有收藏笔记路径" v-if="favoriteNotePaths.length === 0" />
    <a-flex wrap="wrap" gap="small" v-if="favoriteNotePaths.length > 0">
      <a-tag
        v-for="(path, index) in favoriteNotePaths"
        :key="index"
        draggable="true"
        @dragstart="onDragStart(index)"
        @dragover.prevent
        @drop="onDrop(index)"
        closable
        @close.prevent="delFavorite(path)"
      >
        <template #icon>
          <template v-if="path.endsWith('\\')">
            <FolderFilled :style="{ color: '#f7c427' }" />
          </template>
          <template v-if="!path.endsWith('\\')">
            <FileTextOutlined />
          </template>
        </template>
        <template v-if="path.endsWith('\\')">
          <RouterLink :to="{ path: '/explore', query: { path: path } }"> {{ path }}&nbsp;&nbsp; </RouterLink>
        </template>
        <template v-if="!path.endsWith('\\')">
          <RouterLink :to="{ path: '/note', query: { path: path } }"> {{ path }}&nbsp;&nbsp; </RouterLink>
        </template>
      </a-tag>
    </a-flex>
  </a-card>
  <a-card>
    <template #title>服务端收藏笔记路径</template>
    <a-empty description="没有收藏笔记路径" v-if="favoriteNotePathsRemote.length === 0" />
    <a-flex wrap="wrap" gap="small" v-if="favoriteNotePathsRemote.length > 0">
      <a-tag v-for="(path, index) in favoriteNotePathsRemote" :key="index">
        <template #icon>
          <template v-if="path.endsWith('\\')">
            <FolderFilled :style="{ color: '#f7c427' }" />
          </template>
          <template v-if="!path.endsWith('\\')">
            <FileTextOutlined />
          </template>
        </template>
        <template v-if="path.endsWith('\\')">
          <RouterLink :to="{ path: '/explore', query: { path: path } }"> {{ path }}&nbsp;&nbsp; </RouterLink>
        </template>
        <template v-if="!path.endsWith('\\')">
          <RouterLink :to="{ path: '/note', query: { path: path } }"> {{ path }}&nbsp;&nbsp; </RouterLink>
        </template>
      </a-tag>
    </a-flex>
  </a-card>
  <!--<a-card>
    <template #title>
      最新提醒日志
    </template>
    <div class="log" v-html="remindLog">
    </div>
  </a-card>-->
  <a-float-button type="primary" @click="downloadFavorite" style="right: 80px">
    <template #icon>
      <DownloadOutlined />
    </template>
  </a-float-button>
  <a-float-button type="primary" @click="uploadFavorite">
    <template #icon>
      <UploadOutlined />
    </template>
  </a-float-button>
</template>

<style scoped>
/*.log{
  white-space: pre-wrap;
  word-wrap: anywhere;
  font-size: 12px;
}*/
.ant-tag {
  font-size: 14px;
  padding: 4px;
}
</style>
