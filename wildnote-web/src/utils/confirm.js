import { Modal } from 'ant-design-vue'
import { createVNode } from 'vue'
import { ExclamationCircleOutlined } from '@ant-design/icons-vue'

const confirm = function (content, onOk, onCancel) {
  Modal.confirm({
    title: '操作确认',
    icon: createVNode(ExclamationCircleOutlined),
    content: content,
    okText: '确认',
    cancelText: '取消',
    onOk: function () {
      if (onOk) {
        onOk()
      }
    },
    onCancel: function () {
      if (onCancel) {
        onCancel()
      }
    },
  })
}

export { confirm }
