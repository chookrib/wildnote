<script setup>
import { onMounted, ref } from 'vue'
import axios from '../utils/axios'
import { isPinned, pin, unpin } from '../utils/pinnedPath'
import { message } from 'ant-design-vue'
import { RouterLink, useRoute } from 'vue-router'
import { showDateTime } from '@/utils/dateTime'
import { StarOutlined, StarFilled, EditFilled, RollbackOutlined, SaveFilled } from '@ant-design/icons-vue'

const route = useRoute()
const notePath = route.query.path
const noteContent = ref('')
const noteContentEdit = ref('')
const editMode = ref(false)
const isPinnedNote = ref(false)

const lastSaveTime = ref(null)

onMounted(() => {
  loadNote()
})

const loadNote = function() {
  axios.post('/api/note/get', {
    path: notePath
  }).then(response => {
    noteContent.value = response.data.data
    isPinnedNote.value = isPinned(notePath)
  })
}

const editNote = function() {
  noteContentEdit.value = noteContent.value
  editMode.value = true
}

const saveNote = function() {
  axios.post('/api/note/save', {
    path: notePath,
    content: noteContentEdit.value
  }).then(response => {
    lastSaveTime.value = new Date()
    message.success('保存成功')
    editMode.value = false
    loadNote()
  })
}

const cancelEditNote = function() {
  editMode.value = false
}

const pinNote = function() {
  pin(notePath)
  isPinnedNote.value = true
}

const unpinNote = function() {
  unpin(notePath)
  isPinnedNote.value = false
}
</script>

<template>
  <div class="fixed-title">
    <!--<RouterLink :to="{ path: '/explore' }">根</RouterLink>-->
    \<template v-for="(segment, index) in notePath.split('\\').filter(s => s)" :key="index">
      <template v-if="index < notePath.split('\\').length - 2">
        <RouterLink
          :to="{ path: '/explore', query: { path: notePath.split('\\').slice(0, index + 2).join('\\') + '\\' } }">
          {{ segment }}
        </RouterLink>\</template>
      <template v-if="index === notePath.split('\\').length - 2">{{ segment }}</template>
    </template>
  </div>
  <a-card style="margin-top: 40px;">
    <template #extra>
      <span v-if="lastSaveTime">最后保存于 {{ showDateTime(lastSaveTime) }}</span>
    </template>
    <div v-if="!editMode" style="white-space: pre-wrap; word-wrap: anywhere;">
      {{ noteContent }}
    </div>
    <a-textarea v-if="editMode" :autoSize="true" v-model:value="noteContentEdit" :showCount="true">
    </a-textarea>
  </a-card>
  <a-float-button type="default" @click="pinNote" v-if="!editMode&&!isPinnedNote" style="right: 80px;">
    <template #icon>
      <StarOutlined />
    </template>
  </a-float-button>
  <a-float-button type="primary" @click="unpinNote" v-if="!editMode&&isPinnedNote" style="right: 80px;">
    <template #icon>
      <StarFilled />
    </template>
  </a-float-button>
  <a-float-button type="primary" @click="editNote" v-if="!editMode">
    <template #icon>
      <EditFilled />
    </template>
  </a-float-button>
  <a-float-button type="primary" @click="cancelEditNote" v-if="editMode" style="right: 80px;">
    <template #icon>
      <RollbackOutlined />
    </template>
  </a-float-button>
  <a-float-button type="primary" @click="saveNote" v-if="editMode">
    <template #icon>
      <SaveFilled />
    </template>
  </a-float-button>
</template>

<style scoped>
.fixed-title {
  background-color: #FFFBE6;
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
