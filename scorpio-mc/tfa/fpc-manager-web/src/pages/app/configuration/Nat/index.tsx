import { Button, Form, Popconfirm, Radio } from 'antd';
import { useEffect, useState } from 'react';
import { queryNatConfig, updateNatConfig } from './service';
import type { NATConfig } from './typings';
import { NATAction } from './typings';

const Nat = () => {
  const [form] = Form.useForm<{ natAction: NATAction }>();

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (loading === false) {
      queryNatConfig().then((res) => {
        const { success, result } = res;
        if (success) {
          form.setFieldsValue({
            natAction: result.natAction,
          });
        }
      });
    }
  }, [form, loading]);

  const handleUpdate = () => {
    const values = form.getFieldsValue();
    const data: NATConfig = {
      natAction: values.natAction,
      id: 1,
    };

    setLoading(true);
    updateNatConfig(data).then(() => {
      setLoading(false);
    });
  };

  return (
    <div style={{ paddingTop: '2em' }}>
      <Form labelCol={{ span: 8 }} wrapperCol={{ span: 8 }} form={form}>
        <Form.Item label="NAT会话关联" name="natAction">
          <Radio.Group
            options={[
              {
                label: '开启',
                value: NATAction.open,
              },
              {
                label: '关闭',
                value: NATAction.close,
              },
            ]}
          />
        </Form.Item>
        <Form.Item wrapperCol={{ offset: 8 }}>
          <Popconfirm title="是否确认修改" onConfirm={handleUpdate}>
            <Button type="primary" htmlType="submit" loading={loading}>
              确定
            </Button>
          </Popconfirm>
        </Form.Item>
      </Form>
    </div>
  );
};

export default Nat;
