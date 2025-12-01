<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {RouterLink} from 'vue-router';
import {message} from 'ant-design-vue';
import {DownloadOutlined, FileTextOutlined, FolderFilled, UploadOutlined} from '@ant-design/icons-vue';
import axios from '@/utility/axios-utility';
import * as localStorageUtility from '@/utility/local-storage-utility';
import {showConfirm} from '@/utility/confirm-utility';

// const favoriteListRemote = ref([]);
// const favoriteList = ref([]);
const favoriteListRemote = ref<string[]>([]);
const favoriteList = ref<string[]>([]);
// let remindLog = ref('');
let favoriteDragItemIndex: number | null = null;

onMounted(() => {
  favoriteList.value = localStorageUtility.getFavorite();
  // axios.get('/api/log/get?type=remind').then(response => {
  //   //remindLog.value = response.data.data.result.log;
  //   //remindLog.value = response.data.data.replace(/(\\[^|]+)/g, '<a href="#/note?path=$1">$1</a>');
  //   remindLog.value = response.data.data.replace(/(\s\\[^|]+)/g,
  //     (match, p1) => `<a href="#/note?path=${encodeURIComponent(p1.trim())}">${p1}</a>`);
  // });
  loadFavoriteRemote();
});

const loadFavoriteRemote = () => {
  axios.get('/api/favorite/get').then((response) => {
    favoriteListRemote.value = response.data.data.list;
  });
};

const onFavoriteItemDragStart = (index: number) => {
  favoriteDragItemIndex = index;
};

const onFavoriteItemDrop = (dropIndex: number) => {
  if (favoriteDragItemIndex) {
    localStorageUtility.moveFavorite(favoriteDragItemIndex, dropIndex);
    favoriteDragItemIndex = null;
  }
  favoriteList.value = localStorageUtility.getFavorite();
};

const deleteFavorite = (path: string) => {
  showConfirm(
    {
      title: '删除本地收藏',
      content: `确定删除本地收藏的笔记路径 ${path} 吗？`,
      onOk: () => {
        localStorageUtility.deleteFavorite(path);
        favoriteList.value = localStorageUtility.getFavorite();
      }
    });
};

const downloadFavorite = () => {
  showConfirm(
    {
      title: '下载收藏',
      content: `确定下载服务端收藏的笔记路径到本地吗？（将覆盖本地收藏的笔记路径）`,
      onOk: () => {
        axios.get('/api/favorite/get').then((response) => {
          favoriteList.value = response.data.data.list;
          localStorageUtility.setFavorite(favoriteList.value);
          message.success('下载收藏成功');
        });
      }
    });
};

const uploadFavorite = () => {
  showConfirm(
    {
      title: '上传收藏',
      content: `确定上传本地收藏的笔记路径到服务端吗？（将覆盖服务端收藏的笔记路径）`,
      onOk: () => {
        axios.post('/api/favorite/set', {
          list: favoriteList.value,
        }).then((response) => {
          message.success('上传收藏成功');
          loadFavoriteRemote();
        });
      }
    });
};
</script>

<template>
  <a-card>
    <template #title>本地收藏笔记路径</template>
    <a-empty description="没有收藏笔记路径" v-if="favoriteList.length === 0"/>
    <a-flex wrap="wrap" gap="small" v-if="favoriteList.length > 0">
      <a-tag
        v-for="(item, index) in favoriteList"
        :key="index"
        draggable="true"
        @dragstart="onFavoriteItemDragStart(index)"
        @dragover.prevent
        @drop="onFavoriteItemDrop(index)"
        closable
        @close.prevent="deleteFavorite(item)"
      >
        <template #icon>
          <template v-if="item.endsWith('\\')">
            <FolderFilled :style="{ color: '#f7c427' }"/>
          </template>
          <template v-if="!item.endsWith('\\')">
            <FileTextOutlined/>
          </template>
        </template>
        <template v-if="item.endsWith('\\')">
          <RouterLink :to="{ path: '/explore', query: { path: item } }"> {{ item }}&nbsp;&nbsp;</RouterLink>
        </template>
        <template v-if="!item.endsWith('\\')">
          <RouterLink :to="{ path: '/note', query: { path: item } }"> {{ item }}&nbsp;&nbsp;</RouterLink>
        </template>
      </a-tag>
    </a-flex>
  </a-card>

  <a-card>
    <template #title>服务端收藏笔记路径</template>
    <a-empty description="没有收藏笔记路径" v-if="favoriteListRemote.length === 0"/>
    <a-flex wrap="wrap" gap="small" v-if="favoriteListRemote.length > 0">
      <a-tag v-for="(item, index) in favoriteListRemote" :key="index">
        <template #icon>
          <template v-if="item.endsWith('\\')">
            <FolderFilled :style="{ color: '#f7c427' }"/>
          </template>
          <template v-if="!item.endsWith('\\')">
            <FileTextOutlined/>
          </template>
        </template>
        <template v-if="item.endsWith('\\')">
          <RouterLink :to="{ path: '/explore', query: { path: item } }"> {{ item }}&nbsp;&nbsp;</RouterLink>
        </template>
        <template v-if="!item.endsWith('\\')">
          <RouterLink :to="{ path: '/note', query: { path: item } }"> {{ item }}&nbsp;&nbsp;</RouterLink>
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
      <DownloadOutlined/>
    </template>
  </a-float-button>
  <a-float-button type="primary" @click="uploadFavorite">
    <template #icon>
      <UploadOutlined/>
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
