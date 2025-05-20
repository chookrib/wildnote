<script setup>
import { onMounted, ref } from 'vue'
import axios from '../utils/axios'
import { isPinned, pin, unpin } from '../utils/pinnedNote'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import { showDateTime } from '@/utils/dateTime'
import { StarOutlined, StarFilled, EditFilled, RollbackOutlined, SaveFilled } from '@ant-design/icons-vue'

const route = useRoute()
const notePath = route.query.path
const noteContent = ref('')
const noteContentEdit = ref('')
const editMode = ref(false)
const notePinned = ref(false)

const lastSaveTime = ref(null)

onMounted(() => {
  loadNote()
})

const loadNote = function() {
  axios.post('/api/note/get', {
    path: notePath
  }).then(response => {
    noteContent.value = response.data.data
    notePinned.value = isPinned(notePath)
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
  notePinned.value = true
}

const unpinNote = function() {
  unpin(notePath)
  notePinned.value = false
}
</script>

<template>
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
  <a-float-button type="primary" @click="pinNote" v-if="!editMode&&!notePinned" style="right: 80px;">
    <template #icon>
      <StarOutlined />
    </template>
  </a-float-button>
  <a-float-button type="primary" @click="unpinNote" v-if="!editMode&&notePinned" style="right: 80px;">
    <template #icon>
      <StarFilled />
    </template>
  </a-float-button>
  <a-float-button type="primary" @click="editNote" v-if="!editMode">
    <template #icon>
      <EditFilled />
    </template>
  </a-float-button>
  <a-card>
    <template #title>
      <span style="font-weight: bold;">{{ notePath }}</span>
    </template>
    <template #extra>
      <span v-if="lastSaveTime">最后保存于 {{ showDateTime(lastSaveTime) }}</span>
    </template>
    <div v-if="!editMode" style="white-space: pre-wrap;">
      {{ noteContent }}
    </div>
    <a-textarea v-if="editMode" :autosize="true" v-model:value="noteContentEdit" :showCount="true">
    </a-textarea>
  </a-card>
</template>

<style scoped>
</style>
