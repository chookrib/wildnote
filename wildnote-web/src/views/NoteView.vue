<script setup>
import { onMounted, ref } from 'vue'
import axios from '@/utils/axiosUtil'
import { isLocalPinnedPath, localPinPath, localUnpinPath } from '@/utils/localStorageUtil'
import { message } from 'ant-design-vue'
import { RouterLink, useRoute } from 'vue-router'
import { showDateTime } from '@/utils/dateTimeUtil'
import { EditFilled, RollbackOutlined, SaveFilled, StarFilled, StarOutlined } from '@ant-design/icons-vue'
import { Marked } from 'marked'
import { markedHighlight } from 'marked-highlight'
import 'highlight.js/styles/default.min.css'
import hljs from 'highlight.js'

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
    isPinnedNote.value = isLocalPinnedPath(notePath)
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
  localPinPath(notePath)
  isPinnedNote.value = true
}

const unpinNote = function() {
  localUnpinPath(notePath)
  isPinnedNote.value = false
}

const renderer = {
  link(href, title, text) {
    const link = marked.Renderer.prototype.link.call(this, href, title, text)
    return link.replace('<a', '<a target=\'_blank\' rel=\'noreferrer\' ')
  },
  // paragraph(text) {
  //   console.log(text)
  //   if (/^\s*$/.test(text)) {
  //     return '<br>'
  //   }
  //   return marked.Renderer.prototype.paragraph.call(this, text)
  // }
  table(...args) {
    return `<div class="markdown-table-wrapper">${marked.Renderer.prototype.table.apply(this, args)}</div>`;
  }
}
const marked = new Marked({
    breaks: true,
    renderer: renderer
  },
  markedHighlight({
    emptyLangClass: 'hljs',
    langPrefix: 'hljs language-',
    highlight(code, lang, info) {
      const language = hljs.getLanguage(lang) ? lang : 'plaintext'
      return hljs.highlight(code, { language }).value
    }
  })
)

const markdownHtml = function() {
  return marked.parse(noteContent.value)
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
    <span v-if="lastSaveTime">最后保存于 {{ showDateTime(lastSaveTime) }}</span>
  </div>
  <div style="margin-top: 40px; height: calc(100% - 40px); background-color: #fff; overflow: hidden;">
    <div v-if="!editMode" class="markdown" style="padding: 20px; height: 100%; overflow: scroll;">
      <!--<div v-if="!editMode" style="white-space: pre-wrap; word-wrap: anywhere;">-->
      <!--  {{ noteContent }}-->
      <!--</div>-->
      <div v-html="markdownHtml()">
      </div>
    </div>
    <a-textarea v-if="editMode" v-model:value="noteContentEdit" wrap="off"
      style="height: 100%;">
    </a-textarea>
  </div>
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
@font-face {
  font-family: 'Maple Mono NF CN';
  src: url('/font/MapleMono-NF-CN-Regular.woff2') format('truetype');
  font-weight: normal;
  font-style: normal;
}

:global(textarea) {
  font-family: 'Maple Mono NF CN'!important;
}

/*@font-face {
  font-family: 'Sarasa Mono SC';
  src: url('/font/SarasaMonoSC-Regular.woff2') format('truetype');
  font-weight: normal;
  font-style: normal;
}

:global(textarea) {
  font-family: 'Sarasa Mono SC'!important;
}*/

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

.markdown :deep(blockquote) {
  border-left: 4px solid #ffe58f;
  background: #fffbe6;
  padding: 12px 16px;
  margin: 12px 0;
  color: #8c6d1f;
  font-style: italic;
}

.markdown :deep(:not(blockquote) > p) {
  margin-bottom: 10px;
}

.markdown :deep(h1),
.markdown :deep(h2),
.markdown :deep(h3),
.markdown :deep(h4),
.markdown :deep(h5),
.markdown :deep(h6) {
  margin-top: 10px;
  font-weight: bold;
}

.markdown :deep(.markdown-table-wrapper) {
  overflow-x: auto;
}

.markdown :deep(table) {
  border-collapse: collapse;
}

.markdown :deep(table th), .markdown :deep(table td) {
  border: 1px solid #8c8c8c;
  padding: 4px;
  white-space: nowrap;
}

</style>
