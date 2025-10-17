<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { EditFilled, RollbackOutlined, SaveFilled, StarFilled, StarOutlined } from '@ant-design/icons-vue'

import { Marked } from 'marked'
import { markedHighlight } from 'marked-highlight'
import hljs from 'highlight.js'
import 'highlight.js/styles/default.min.css'

import { EditorState } from '@codemirror/state'
import { EditorView, keymap, lineNumbers, highlightWhitespace, highlightActiveLine, drawSelection } from '@codemirror/view'
import { syntaxHighlighting, defaultHighlightStyle, foldGutter } from '@codemirror/language'
import { defaultKeymap, history, undo, redo } from '@codemirror/commands'
import { highlightSelectionMatches } from '@codemirror/search'
import { markdown } from '@codemirror/lang-markdown'

import axios from '@/utility/axios-utility.js'
import * as localStorageUtility from '@/utility/local-storage-utility.js'
import { showDateTime } from '@/utility/datetime-utility.js'

const route = useRoute()
const notePath = route.query.path
const noteContent = ref('')
const noteContentEdit = ref('')
const editMode = ref(false)
const isFavorite = ref(false)
const lastSaveTime = ref(null)

const editorRef = ref(null)
let view
onMounted(() => {
  loadNote()

  view = new EditorView({
    state: EditorState.create({
      //doc: noteContentEdit.value,
      extensions: [
        lineNumbers(),
        foldGutter(),
        history(),
        drawSelection(),
        syntaxHighlighting(defaultHighlightStyle),
        highlightActiveLine(),
        highlightSelectionMatches(),
        keymap.of([
          ...defaultKeymap,
          { key: "Mod-z", run: undo },
          { key: "Mod-y", run: redo },
          { key: "Shift-Mod-z", run: redo }
        ]),
        highlightWhitespace(),
        markdown(),
      ]
    }),
    parent: editorRef.value
  })
})

const loadNote = function() {
  axios.post('/api/note/get', {
    path: notePath
  }).then(response => {
    noteContent.value = response.data.data.content
    //isFavorite.value = localStorageUtility.isFavoriteNotePath(notePath)
  })
  // 当笔记不存在时可以取消收藏
  isFavorite.value = localStorageUtility.isFavoriteNotePath(notePath)
}

const editNote = function() {
  noteContentEdit.value = noteContent.value
  editMode.value = true
  view.dispatch({
    changes: {
      from: 0,
      to: view.state.doc.length,
      insert: noteContentEdit.value
    }
  })
}

onUnmounted(() => {
  if (view) view.destroy()
})

const saveNote = function() {
  axios.post('/api/note/save', {
    path: notePath,
    //content: noteContentEdit.value
    content: view.state.doc.toString()
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

const addFavorite = function() {
  localStorageUtility.addFavoritePath(notePath)
  isFavorite.value = true
}

const dropFavorite = function() {
  localStorageUtility.dropFavoriteNotePath(notePath)
  isFavorite.value = false
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
  // 将连续两个及以上的空行替换为 <br> 标签
  // const md = noteContent.value.replace(/\n{2,}/g, match => '<br>'.repeat(match.length - 1));
  // return marked.parse(md)
  return marked.parse(noteContent.value)
}
</script>

<template>
  <div class="fixed-title">
    <RouterLink :to="{ path: '/explore' }">根</RouterLink>
    \<template v-for="(segment, index) in notePath.split('\\').filter(s => s)" :key="index">
      <template v-if="index < notePath.split('\\').length - 2">
        <RouterLink
          :to="{ path: '/explore', query: { path: notePath.split('\\').slice(0, index + 2).join('\\') + '\\' } }">
          {{ segment }}
        </RouterLink>\</template>
      <template v-if="index === notePath.split('\\').length - 2"><span>{{ segment }}</span></template>
    </template>
    <span v-if="lastSaveTime" style="font-size: 12px; margin-left: 20px;">最后保存于 {{ showDateTime(lastSaveTime) }}</span>
  </div>
  <div v-if="!editMode" style="margin-top: 40px; height: calc(100% - 40px); background-color: #fff; overflow: hidden;">
      <!--<div style="padding: 20px; height: 100%; overflow: scroll; white-space: pre-wrap; word-wrap: anywhere;">-->
      <!--  {{ noteContent }}-->
      <!--</div>-->
      <div class="markdown" style="padding: 20px; height: 100%; overflow: scroll;" v-html="markdownHtml()">
      </div>
  </div>
  <div v-show="editMode" style="margin-top: 40px; height: calc(100% - 40px); background-color: #fff; overflow: hidden;">
    <!--<a-textarea v-model:value="noteContentEdit" wrap="off" style="height: 100%;"></a-textarea>-->
    <div ref="editorRef" style="height: 100%"></div>
  </div>
  <a-float-button type="default" @click="addFavorite" v-if="!editMode&&!isFavorite" style="right: 80px;">
    <template #icon>
      <StarOutlined />
    </template>
  </a-float-button>
  <a-float-button type="primary" @click="dropFavorite" v-if="!editMode&&isFavorite" style="right: 80px;">
    <template #icon>
      <StarFilled />
    </template>
  </a-float-button>
  <a-float-button type="primary" @click="editNote" v-if="!editMode">
    <template #icon>
      <EditFilled />
    </template>
  </a-float-button>
  <a-float-button type="primary" @click="saveNote" v-if="editMode" style="right: 80px;">
    <template #icon>
      <SaveFilled />
    </template>
  </a-float-button>
  <a-float-button type="primary" @click="cancelEditNote" v-if="editMode">
    <template #icon>
      <RollbackOutlined />
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

/*:global(textarea) {
  font-family: 'Maple Mono NF CN'!important;
}*/

/*@font-face {
  font-family: 'Sarasa Mono SC';
  src: url('/font/SarasaMonoSC-Regular.woff2') format('truetype');
  font-weight: normal;
  font-style: normal;
}

:global(textarea) {
  font-family: 'Sarasa Mono SC'!important;
}*/

:deep(.cm-editor) {
  height: 100%;
}

:deep(.cm-editor *) {
  font-family: 'Maple Mono NF CN';
}

:deep(.cm-scroller) {
  overflow: auto;
  height: 100%;
}

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
  display: flex;
  overflow-x: auto;
  scrollbar-width: none; /* Firefox 隐藏滚动条 */
  -ms-overflow-style: none; /* IE/Edge 隐藏滚动条 */
}
.fixed-title::-webkit-scrollbar {
  display: none; /* Chrome/Safari/Webkit 隐藏滚动条 */
}

.fixed-title * {
  /*font-weight: bold;*/
  white-space: nowrap;
}

.markdown {
  line-height: 1.3rem;
}

.markdown :deep(blockquote) {
  border-left: 0.25rem solid #ffe58f;
  background: #fffbe6;
  padding: 0 1rem;
  margin: 1rem 0;
  color: #8c6d1f;
  /*font-style: italic;*/
  display: flow-root;/*阻止外边距合并*/
}

/*.markdown :deep(:not(blockquote) > p) {
  margin-bottom: 10px;
}*/

.markdown :deep(code:not([class])) {
  background-color: #f3f3f3;
}


.markdown :deep(h1),
.markdown :deep(h2),
.markdown :deep(h3),
.markdown :deep(h4),
.markdown :deep(h5),
.markdown :deep(h6) {
  margin-top: 1rem;
  margin-bottom: 1rem;
  line-height: 1rem;
  font-weight: bold;
  min-height: 1rem; /*没有内容时仍保持高度*/
}

.markdown :deep(.markdown-table-wrapper) {
  overflow-x: auto;
}

.markdown :deep(table) {
  border-collapse: collapse;
}

.markdown :deep(table th), .markdown :deep(table td) {
  border: 1px solid #8c8c8c;
  padding: 0.2rem;
  /*padding: 0.5rem;*/
  white-space: nowrap;
}

.markdown :deep(p) {
  margin-top: 1rem;
  margin-bottom: 1rem;
  line-height: 1.2rem;
}

</style>
