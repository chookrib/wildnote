<script setup>
import { onMounted } from 'vue'
import axios from '@/utility/axios-utility.js'
import router from '@/router'
import { ref, computed } from 'vue'
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { showDateTime } from '@/utility/datetime-utility.js'

const notes = ref([])
const searchKey = ref('')

const filteredNotes = computed(() => {
  return notes.value.filter(note => !note.directory && note.relPath.includes(searchKey.value))
})

onMounted(() => {
  loadNoteIndex()
})

function loadNoteIndex() {
  axios.get('/api/note/all').then(response => {
    notes.value = response.data.data
  })
}

function editNote(note) {
  router.push({ path: '/note', query: { path: note.relPath } })
}

</script>

<template>
  <a-layout>
    <Header>
      <span style=" padding: 10px; font-weight: bold;">目录</span>
      <a-button @click=loadNoteIndex>
        <template #icon>
          <ReloadOutlined />
        </template>
        刷新
      </a-button>
      <a-input v-model:value=searchKey>
        <template #prefix>
          <SearchOutlined />
        </template>
      </a-input>
    </Header>
    <a-layout-content>
      <div id="node-index">
        <div>
          笔记文件
          <div style="float: right">修改时间</div>
        </div>
        <div v-for="note in filteredNotes" :key="note.relPath" @click="editNote(note)">
          {{ note.relPath }}
          <div style="float: right">{{ showDateTime(note.lastModifiedTime) }}</div>
        </div>
      </div>
    </a-layout-content>
  </a-layout>
</template>

<style scoped>
.ant-layout {
  height: 100%;
}

#node-index {
  height: 100%;
  white-space: nowrap;
  overflow: auto;
  line-height: 1.5;
  padding: 10px;
  font-size: 12pt;
}

#node-index div {
  cursor: pointer;
  border-bottom: 1px dotted #3c3c3c;

  &:hover {
    background-color: #3c3c3c;
    color: #fff;
  }
}
</style>
