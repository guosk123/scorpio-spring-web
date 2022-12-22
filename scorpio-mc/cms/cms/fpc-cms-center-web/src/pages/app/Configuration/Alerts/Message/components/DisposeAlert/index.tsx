import { Button, Form } from 'antd';
import Modal from 'antd/lib/modal/Modal';
import { Fragment, useCallback, useRef, useState } from 'react';
import TextArea from 'antd/lib/input/TextArea';
import type { Dispatch } from 'umi';
import { connect } from 'dva';
import type { ButtonType } from 'antd/lib/button';

interface Props {
  id: string;
  disable?: boolean;
  buttonType?: ButtonType;
  onChange?: () => void;
  dispatch: Dispatch;
}

export enum EDispose {
  // 未处理
  'Untreated' = '0',
  // 已处理
  'Processed' = '1',
}

function DisposeAlert(props: Props) {
  const { id, disable, buttonType, onChange, dispatch } = props;
  const [isModalVisible, setIsModalVisible] = useState(false);
  const formRef = useRef<any>();
  const handleCancel = useCallback(() => {
    setIsModalVisible(false);
  }, []);
  const handleSubmit = (values: any) => {
    dispatch({ type: 'alertModel/disposeAlertRule', payload: { id, ...values } }).then(() => {
      setIsModalVisible(false);
      if (onChange) {
        onChange();
      }
    });
  };
  return (
    <Fragment>
      <Button
        type={buttonType}
        size={'small'}
        disabled={disable}
        onClick={() => {
          setIsModalVisible(true);
        }}
      >
        处理
      </Button>
      <Modal
        title="处理告警消息"
        visible={isModalVisible}
        maskClosable={false}
        onCancel={handleCancel}
        footer={[
          <Button onClick={handleCancel}>取消</Button>,
          <Button
            type={'primary'}
            onClick={() => {
              formRef.current.submit();
            }}
          >
            确定
          </Button>,
        ]}
      >
        <Form ref={formRef} onFinish={handleSubmit}>
          <Form.Item name={'reason'} rules={[{ required: true, message: '请输入处理内容' }]}>
            <TextArea rows={4} placeholder="请输入描述信息" />
          </Form.Item>
        </Form>
      </Modal>
    </Fragment>
  );
}
export default connect()(DisposeAlert);
