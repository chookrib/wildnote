<script setup>
import { onMounted, ref } from 'vue'
import { DeleteTwoTone } from '@ant-design/icons-vue'
import { getAll, unpin } from '@/utils/pinnedNote'
import { RouterLink } from 'vue-router'
import { confirm } from '../utils/confirm'

const pinnedNotes = ref([])
let dragIndex = null

const onDragStart = (index) => {
  dragIndex = index
}

const onDrop = (dropIndex) => {
  if (dragIndex === null || dragIndex === dropIndex) return
  const moved = pinnedNotes.value.splice(dragIndex, 1)[0]
  pinnedNotes.value.splice(dropIndex, 0, moved)
  dragIndex = null
  // 可选：持久化顺序
  localStorage.setItem('pinnedNotes', JSON.stringify(pinnedNotes.value))
}

onMounted(() => {
  pinnedNotes.value = getAll()
})

const unpinNote = function (notePath) {
  confirm('确定取消固定笔记吗？', function () {
    unpin(notePath)
    pinnedNotes.value = getAll()
  })
}
</script>

<template>
  <a-card title="">
    <a-empty description="没有固定笔记" v-if="pinnedNotes.length === 0" />
    <a-flex wrap="wrap" gap="small" v-if="pinnedNotes.length > 0">
      <a-card
        v-for="(notePath, index) in pinnedNotes"
        :key="index"
        :hoverable="true"
        draggable="true"
        @dragstart="onDragStart(index)"
        @dragover.prevent
        @drop="onDrop(index)"
      >
        <template #title>
          <RouterLink :to="{ path: '/note', query: { path: notePath } }">
            {{ notePath }}&nbsp;&nbsp;
          </RouterLink>
        </template>
        <template #extra>
          <a-button @click="unpinNote(notePath)" size="small">
            <template #icon>
              <DeleteTwoTone />
            </template>
          </a-button>
        </template>
      </a-card>
    </a-flex>
  </a-card>
</template>

<style scoped></style>
