<script setup>
import { createVNode, onMounted, ref } from 'vue'
import { DeleteTwoTone, ExclamationCircleOutlined } from '@ant-design/icons-vue'
import { getAll, unpin } from '@/utils/pinnedNote'
import { RouterLink } from 'vue-router'
import { Modal } from 'ant-design-vue'

const pinnedNotes = ref([])

onMounted(() => {
  pinnedNotes.value = getAll()
})

const unpinNote = function(notePath) {
  Modal.confirm({
    title: '取消固定笔记',
    icon: createVNode(ExclamationCircleOutlined),
    content: '确定取消固定笔记吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: function() {
      unpin(notePath)
      pinnedNotes.value = getAll()
    },
    onCancel: function() {
    }
  })
}
</script>

<template>
  <a-card title="">
    <a-empty description="没有固定笔记" v-if="pinnedNotes.length === 0" />
    <a-flex wrap="wrap" gap="small" v-if="pinnedNotes.length > 0">
      <a-card v-for="(notePath, index) in pinnedNotes" :key="index" :hoverable="true">
        <template #title>
          <RouterLink :to="{path:'/note', query: {path: notePath}}">
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

<style scoped>
</style>
