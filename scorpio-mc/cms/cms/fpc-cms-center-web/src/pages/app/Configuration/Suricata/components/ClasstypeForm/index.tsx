import {
  createSuricataRuleClasstype,
  updateSuricataRuleClasstype,
} from '@/pages/app/security/service';
import type { IRuleClasstype } from '@/pages/app/security/typings';
import { Button, Form, Input, message, Space } from 'antd';
import { useEffect } from 'react';
import { history } from 'umi';

interface Props {
  detail?: IRuleClasstype;
}

const ClasstypeForm = (props: Props) => {
  const { detail } = props;

  const [form] = Form.useForm();

  useEffect(() => {
    if (detail) {
      form.setFieldsValue(detail);
    }

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [detail]);

  const handleFinish = (values: any) => {
    const { id, ...rest } = values;
    if (detail) {
      updateSuricataRuleClasstype({ id: detail.id, ...values }).then(() => {
        message.success('修改成功');
      });
    } else {
      createSuricataRuleClasstype(rest).then(({ success }) => {
        if (success) {
          history.goBack();
        }
      });
    }
  };

  return (
    <Form form={form} onFinish={handleFinish} wrapperCol={{ span: 12 }} labelCol={{ span: 4 }}>
      <Form.Item name="name" label="分类名称" rules={[{ required: true }]}>
        <Input />
      </Form.Item>
      <Form.Item wrapperCol={{ offset: 4 }}>
        <Space>
          <Button type="primary" htmlType="submit">
            保存
          </Button>
          <Button
            onClick={() => {
              history.goBack();
            }}
          >
            返回
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
};

export default ClasstypeForm;
