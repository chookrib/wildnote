import { createVNode } from 'vue';
import { Modal } from 'ant-design-vue';
import { ExclamationCircleOutlined } from '@ant-design/icons-vue';

const showConfirm = (content: string, onOk?: () => void, onCancel?: () => void) => {
  Modal.confirm({
    title: '操作确认',
    icon: createVNode(ExclamationCircleOutlined),
    content: content,
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      if (onOk) {
        onOk();
      }
    },
    onCancel: () => {
      if (onCancel) {
        onCancel();
      }
    },
  });
};

export { showConfirm };
