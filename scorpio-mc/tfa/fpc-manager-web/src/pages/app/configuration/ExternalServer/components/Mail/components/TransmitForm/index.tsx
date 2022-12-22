import { Form, Input, Button, Modal, message } from 'antd';
import { useEffect } from 'react';
import { history } from 'umi';
import { SettingOutlined } from '@ant-design/icons';
import { validateMail } from '@/pages/app/configuration/Transmit/utils/validators';
import { createTransmitMail, queryTransmitMailById, updateTransmitMail } from './service';
import { EReceiverType } from '../../../../typings';
import { useForm } from 'antd/lib/form/Form';

export default function TransmitForm({
  mailId,
  embed = false,
  onSubmit,
  onCancel,
}: {
  mailId?: string;
  embed?: boolean;
  onSubmit?: (success: boolean) => void;
  onCancel?: () => void;
}) {
  const [form] = useForm<any>();

  useEffect(() => {
    (async () => {
      if (mailId) {
        const { success, result } = await queryTransmitMailById(mailId);
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
          id: mailId,
          name: params?.name,
          receiverContent: JSON.stringify({
            mailTitle: params?.mailTitle,
            receiver: params?.receiver,
            cc: params?.cc,
            bcc: params?.bcc,
          }),
          receiverType: EReceiverType.MAIL,
        };
        if (mailId) {
          const { success } = await updateTransmitMail(payload);
          if (success) {
            message.success('编辑成功!');
            if (!embed) {
              history.push('/configuration/third-party/external-server/mail/list');
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
          const { success } = await createTransmitMail(payload);
          if (success) {
            message.success('创建成功！');
            if (!embed) {
              history.push('/configuration/third-party/external-server/mail/list');
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
            disabled={mailId !== undefined}
            style={{ width: '100%' }}
            allowClear
            placeholder="请输入名称"
          />
        </Form.Item>
        <Form.Item
          label="邮件主题"
          name="mailTitle"
          rules={[{ required: true, message: '必须输入邮件主题' }]}
        >
          <Input style={{ width: '100%' }} allowClear placeholder="请输入邮件主题" />
        </Form.Item>
        <Form.Item
          label="收件人"
          name="receiver"
          rules={[{ required: true, message: '必须输入收件人' }, { validator: validateMail }]}
        >
          <Input style={{ width: '100%' }} allowClear placeholder="请输入收件人邮箱地址" />
        </Form.Item>
        <Form.Item label="抄送人" name="cc" rules={[{ validator: validateMail }]}>
          <Input style={{ width: '100%' }} allowClear placeholder="请输入抄送人邮箱地址" />
        </Form.Item>

        <Form.Item label="密送人" name="bcc" rules={[{ validator: validateMail }]}>
          <Input style={{ width: '100%' }} allowClear placeholder="请输入密送人邮箱地址" />
        </Form.Item>

        <Form.Item wrapperCol={{ span: 16, offset: 5 }}>
          <div style={{ display: 'flex', position: 'absolute', left: 0 }}>
            <Button style={{ marginRight: '10px' }} type="primary" htmlType="submit">
              确定
            </Button>
            <Button
              onClick={() => {
                if (!embed) {
                  history.push('/configuration/third-party/external-server/mail/list');
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
