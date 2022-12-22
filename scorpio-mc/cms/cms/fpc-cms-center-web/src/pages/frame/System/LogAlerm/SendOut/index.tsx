import { BOOL_NO, BOOL_YES } from '@/common/dict';
import type { ConnectState } from '@/models/connect';
import { ipV4Regex } from '@/utils/utils';
import {
  Button,
  Card,
  Checkbox,
  Form,
  Input,
  InputNumber,
  Modal,
  Select,
  Skeleton,
  Switch,
} from 'antd';
import { connect } from 'dva';
import type { FC } from 'react';
import { useEffect } from 'react';
import type { Dispatch } from 'umi';
import type { IAlarmAndLogSyslog } from './typings';

const layout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 12 },
};
const tailLayout = {
  wrapperCol: { offset: 4, span: 12 },
};

interface IAlarmAndLogSyslogProps {
  dispatch: Dispatch;
  queryLoading: boolean | undefined;
  updateLoading: boolean | undefined;
  systemSyslogSettings: IAlarmAndLogSyslog;
}

const SystemSyslog: FC<IAlarmAndLogSyslogProps> = ({
  dispatch,
  queryLoading,
  updateLoading,
  systemSyslogSettings: settings,
}) => {
  useEffect(() => {
    if (dispatch) {
      dispatch({
        type: 'systemSyslogModel/querySystemSyslogSettings',
      });
    }
  }, [dispatch]);

  const handleSubmit = (values: any) => {
    Modal.confirm({
      title: '确定保存吗？',
      maskClosable: false,
      keyboard: false,
      onOk: () => {
        const { state, dataSource = [] } = values;
        const submitData = {
          ...values,
          state: state ? BOOL_YES : BOOL_NO,
          dataSource: dataSource.join(','),
        };

        dispatch({
          type: 'systemSyslogModel/updateSystemSyslogSettings',
          payload: {
            ...submitData,
          },
        });
      },
    });
  };

  return (
    <Card bordered={false}>
      <Skeleton active loading={queryLoading}>
        <Form
          {...layout}
          onFinish={handleSubmit}
          initialValues={{
            ...settings,
            port: settings.port || undefined,
            state: settings.state === BOOL_YES,
            dataSource: settings.dataSource?.split(',') || [],
          }}
        >
          <Form.Item label="ID" name="id" style={{ display: 'none' }}>
            <Input />
          </Form.Item>
          <Form.Item label="状态" name="state" valuePropName="checked">
            <Switch checkedChildren="开启" unCheckedChildren="关闭" />
          </Form.Item>
          <Form.Item
            label="IP地址"
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
              style={{ width: '100%' }}
            />
          </Form.Item>

          <Form.Item
            label="协议"
            name="protocol"
            rules={[{ required: true, message: '请选择协议' }]}
          >
            <Select placeholder="选择协议">
              {['TCP', 'UDP'].map((item) => (
                <Select.Option key={item} value={item}>
                  {item}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="外发内容"
            name="dataSource"
            rules={[{ required: true, message: '请选择外发内容' }]}
          >
            <Checkbox.Group>
              <Checkbox value="system_log">系统日志</Checkbox>
              <Checkbox value="audit_log">审计日志</Checkbox>
              <Checkbox value="system_alarm">系统告警</Checkbox>
            </Checkbox.Group>
          </Form.Item>

          <Form.Item {...tailLayout}>
            <Button type="primary" htmlType="submit" loading={updateLoading}>
              保存
            </Button>
          </Form.Item>
        </Form>
      </Skeleton>
    </Card>
  );
};

export default connect(
  ({ loading: { effects }, systemSyslogModel: { systemSyslogSettings } }: ConnectState) => ({
    systemSyslogSettings,
    queryLoading: effects['systemSyslogModel/querySystemSyslogSettings'],
    updateLoading: effects['systemSyslogModel/updateSystemSyslogSettings'],
  }),
)(SystemSyslog);
