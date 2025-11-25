<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import { EditorState } from '@codemirror/state';
import {
  EditorView,
  keymap,
  lineNumbers,
  highlightWhitespace,
  highlightActiveLine,
  drawSelection,
} from '@codemirror/view';
import { syntaxHighlighting, defaultHighlightStyle, foldGutter } from '@codemirror/language';
import { defaultKeymap, history, undo, redo } from '@codemirror/commands';
import { highlightSelectionMatches } from '@codemirror/search';
import { markdown } from '@codemirror/lang-markdown';

const editorRef = ref(null);
let view = null;
onMounted(() => {
  view = new EditorView({
    state: EditorState.create({
      doc: '初始内容\n初始内容\n初始内容',
      extensions: [
        lineNumbers(),
        foldGutter(),
        history(),
        drawSelection(),
        //dropCursor(),
        //EditorState.allowMultipleSelections.of(true),
        //indentOnInput(),
        syntaxHighlighting(defaultHighlightStyle),
        //bracketMatching(),
        //closeBrackets(),
        //autocompletion(),
        //rectangularSelection(),
        //crosshairCursor(),
        highlightActiveLine(),
        //highlightActiveLineGutter(),
        highlightSelectionMatches(),
        //keymap.of(defaultKeymap),
        keymap.of([
          ...defaultKeymap,
          { key: 'Mod-z', run: undo },
          { key: 'Mod-y', run: redo },
          { key: 'Shift-Mod-z', run: redo },
        ]),
        highlightWhitespace(),
        markdown(),
      ],
    }),
    parent: editorRef.value,
  });
});

onUnmounted(() => {
  view.destroy();
});
</script>

<template>
  <div ref="editorRef" style="height: 100%; background-color: #fff"></div>
</template>

<style scoped>
@font-face {
  font-family: 'Maple Mono NF CN';
  src: url('/font/MapleMono-NF-CN-Regular.woff2') format('truetype');
  font-weight: normal;
  font-style: normal;
}

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
</style>
