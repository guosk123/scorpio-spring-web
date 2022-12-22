import { Button, Form, Input, message } from 'antd';
import { connect } from 'dva';
import { Fragment, useCallback, useEffect, useState } from 'react';
import { queryDeviceInfo, updateDeviceInfo } from '../service';

const FormItem = Form.Item;

const formItemLayout = {
  labelCol: { span: 6 },
  wrapperCol: { span: 14 },
};

interface IDeviceInfo {
  deviceId?: string;
  deviceName?: string;
}

function DeviceInfo() {
  const [settings, setSettings] = useState<IDeviceInfo>();

  useEffect(() => {
    queryDeviceInfo().then((res) => {
      const { success, result } = res;
      if (success) {
        setSettings(result);
      } else {
        message.error('获取设备信息失败');
        setSettings({});
      }
    });
  }, []);

  const [form] = Form.useForm();

  const onFinish = useCallback((e) => {
    const payload = {
      deviceId: e.deviceId,
      deviceName: e.deviceName,
    };
    updateDeviceInfo(payload).then((res) => {
      const { success } = res;
      if (success) {
        message.info('保存成功');
      } else {
        message.info('保存失败');
      }
    });
  }, []);

  return (
    <Fragment>
      {settings && (
        <Form
          style={{ marginTop: 40 }}
          {...formItemLayout}
          initialValues={settings}
          name={'DeviceSetting'}
          form={form}
          onFinish={onFinish}
        >
          <FormItem label="设备ID" name="deviceId">
            <span>{settings.deviceId}</span>
          </FormItem>
          <FormItem
            label="设备名称"
            name="deviceName"
            rules={[
              {
                required: true,
                message: '设备名称',
              },
            ]}
          >
            <Input />
          </FormItem>
          <FormItem wrapperCol={{ span: 12, offset: 6 }}>
            <Button type="primary" htmlType="submit" style={{ marginRight: 10 }}>
              保存
            </Button>
          </FormItem>
        </Form>
      )}
    </Fragment>
  );
}
export default connect()(DeviceInfo);
