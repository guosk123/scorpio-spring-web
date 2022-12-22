import { Form, Input, Button, Modal, message, Select, InputNumber } from 'antd';
import { useEffect } from 'react';
import { history } from 'umi';
import { SettingOutlined } from '@ant-design/icons';
import { validateIp } from '@/pages/app/configuration/Transmit/utils/validators';
import { createTransmitZmq, queryTransmitMailById, updateTransmitZmq } from './service';
import { EReceiverType } from '../../../../typings';
import { useForm } from 'antd/lib/form/Form';

const { Option } = Select;

export default function TransmitForm({
  zmqId,
  embed = false,
  onSubmit,
  onCancel,
}: {
  zmqId?: string;
  embed?: boolean;
  onSubmit?: (success: boolean) => void;
  onCancel?: () => void;
}) {
  const [form] = useForm<any>();

  useEffect(() => {
    (async () => {
      if (zmqId) {
        const { success, result } = await queryTransmitMailById(zmqId);
        if (success) {
          form.setFieldsValue({
            ...result,
          });
        }
      }
    })();
  }, []);

  const handleSubmit = (params: any) => {
    Modal.confirm({
      width: 500,
      title: '确定保存吗?',
      icon: <SettingOutlined />,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        const payload = {
          id: zmqId,
          name: params?.name,
          receiverContent: JSON.stringify({
            zmqServerIpAddress: params?.zmqServerIpAddress,
            zmqServerPort: params?.zmqServerPort,
            protocol: params?.protocol,
          }),
          receiverType: EReceiverType.ZMQ,
        };
        if (zmqId) {
          const { success } = await updateTransmitZmq(payload);
          if (success) {
            message.success('编辑成功!');
            if (!embed) {
              history.push('/configuration/third-party/external-server/zmq/list');
            }
            if (onSubmit) {
              onSubmit(true);
            }
            return;
          }
          if (onSubmit) {
            onSubmit(false);
          }
          message.error('编辑失败!');
        } else {
          const { success } = await createTransmitZmq(payload);
          if (success) {
            message.success('创建成功！');
            if (!embed) {
              history.push('/configuration/third-party/external-server/zmq/list');
            }
            if (onSubmit) {
              onSubmit(true);
            }
            return;
          }
          if (onSubmit) {
            onSubmit(false);
          }
          message.error('编辑失败!');
        }
      },
    });
  };

  return (
    <>
      <Form
        name="transmit-mail-form"
        labelCol={{ span: 5 }}
        wrapperCol={{ span: 16 }}
        form={form}
        onFinish={handleSubmit}
        style={{ marginTop: 20 }}
      >
        <Form.Item label="名称" name="name" rules={[{ required: true, message: '必须输入名称' }]}>
          <Input
            disabled={zmqId !== undefined}
            style={{ width: '100%' }}
            allowClear
            placeholder="请输入ZMQ服务器名称"
          />
        </Form.Item>

        <Form.Item
          label="协议"
          name="protocol"
          initialValue={'TCP'}
          rules={[{ required: true, message: '必须选择协议' }]}
        >
          <Select placeholder="请选择协议" disabled>
            <Option value={'TCP'}>TCP</Option>
            <Option value={'UDP'}>UDP</Option>
          </Select>
        </Form.Item>

        <Form.Item
          label="IP地址"
          name="zmqServerIpAddress"
          rules={[{ required: true, message: '必须输入IP地址' }, { validator: validateIp }]}
        >
          <Input style={{ width: '100%' }} allowClear placeholder="请输入IP地址" />
        </Form.Item>

        <Form.Item
          label="端口"
          name="zmqServerPort"
          rules={[
            {
              required: true,
              message: '请输入端口',
            },
          ]}
        >
          <InputNumber
            placeholder="请输入端口"
            precision={0}
            min={0}
            max={65535}
            style={{ width: '100%' }}
          />
        </Form.Item>

        <Form.Item wrapperCol={{ span: 16, offset: 5 }}>
          <div style={{ display: 'flex', position: 'absolute', left: 0 }}>
            <Button style={{ marginRight: '10px' }} type="primary" htmlType="submit">
              确定
            </Button>
            <Button
              onClick={() => {
                if (!embed) {
                  history.push('/configuration/third-party/external-server/zmq/list');
                }
                if (onCancel) {
                  onCancel();
                }
              }}
            >
              取消
            </Button>
          </div>
        </Form.Item>
      </Form>
    </>
  );
}
