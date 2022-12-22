import { BOOL_NO, BOOL_YES } from '@/common/dict';
import type { ConnectState } from '@/models/connect';
import { ipV4Regex } from '@/utils/utils';
import { Button, Card, Form, Input, InputNumber, Modal, Skeleton, Switch } from 'antd';
import React, { useEffect } from 'react';
import type { Dispatch } from 'umi';
import { connect } from 'dva';
import type { IAlertSyslog } from '../typings';

interface IAlertSyslogProps {
  dispatch: Dispatch;
  queryLoading: boolean | undefined;
  updateLoading: boolean | undefined;
  alertSyslogSettings: IAlertSyslog;
}
const AlertSyslog: React.FC<IAlertSyslogProps> = ({
  dispatch,
  queryLoading,
  updateLoading,
  alertSyslogSettings,
}) => {
  const [form] = Form.useForm();
  useEffect(() => {
    if (dispatch) {
      dispatch({
        type: 'alertModel/queryAlertSyslogSettings',
      });
    }
  }, [dispatch]);

  const onFinish = (values: IAlertSyslog) => {
    Modal.confirm({
      title: '确定保存吗?',
      cancelText: '取消',
      okText: '保存',
      onOk() {
        dispatch({
          type: 'alertModel/updateAlertSyslogSettings',
          payload: { ...values, state: values.state ? BOOL_YES : BOOL_NO },
        });
      },
      onCancel() {},
    });
  };

  const layout = {
    labelCol: { span: 4 },
    wrapperCol: { span: 12 },
  };
  const tailLayout = {
    wrapperCol: { offset: 4, span: 12 },
  };

  const stateValue = alertSyslogSettings.state === BOOL_YES;
  form.setFieldsValue({
    state: stateValue,
  });

  return (
    <Skeleton active loading={queryLoading}>
      <Card bordered={false}>
        <Form
          {...layout}
          form={form}
          name="alert-syslog-form"
          initialValues={{
            ...alertSyslogSettings,
            state: stateValue,
            protocol: 'UDP',
          }}
          scrollToFirstError
          onFinish={onFinish}
        >
          <Form.Item label="状态" name="state" valuePropName="checked">
            <Switch checkedChildren="开启" unCheckedChildren="关闭" />
          </Form.Item>

          <Form.Item label="ID" name="id" rules={[]} hidden>
            <Input />
          </Form.Item>

          <Form.Item
            label="日志主机IP地址"
            name="ipAddress"
            rules={[
              { required: true, message: '请输入IP地址' },
              {
                pattern: ipV4Regex,
                message: '请输入正确的IPv4地址',
              },
            ]}
          >
            <Input placeholder="请输入IP地址" />
          </Form.Item>

          <Form.Item label="端口" name="port" rules={[{ required: true, message: '请输入端口' }]}>
            <InputNumber
              min={1}
              max={65535}
              precision={0}
              placeholder="请输入端口"
              style={{ width: 200 }}
            />
          </Form.Item>

          <Form.Item label="协议" name="protocol" extra="Syslog日志外发，只支持UDP协议">
            <span className="ant-form-text">{alertSyslogSettings.protocol || 'UDP'}</span>
          </Form.Item>

          <Form.Item {...tailLayout}>
            <Button type="primary" htmlType="submit" loading={updateLoading}>
              保存
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </Skeleton>
  );
};

export default connect(
  ({ alertModel: { alertSyslogSettings }, loading: { effects } }: ConnectState) => ({
    alertSyslogSettings,
    queryLoading: effects['alertModel/queryAlertSyslogSettings'],
    updateLoading: effects['alertModel/updateAlertSyslogSettings'],
  }),
)(AlertSyslog);
