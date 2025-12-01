import {createVNode} from 'vue';
import {Modal} from 'ant-design-vue';
import {ExclamationCircleOutlined} from '@ant-design/icons-vue';

interface ConfirmOption {
  title?: string;
  content?: string;
  onOk?: () => void;
  onCancel?: () => void;
}

const showConfirm = (option: ConfirmOption) => {
  Modal.confirm({
    title: option.title ? option.title : '操作确认',
    icon: createVNode(ExclamationCircleOutlined),
    content: option.content || '',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      if (option.onOk) {
        option.onOk();
      }
    },
    onCancel: () => {
      if (option.onCancel) {
        option.onCancel();
      }
    },
  });
};

export {showConfirm};
