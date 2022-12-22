import { Select, Form, Button, Modal } from 'antd';
import { useEffect, useRef } from 'react';
import type { ConnectState } from '@/models/connect';
import { history, connect, Dispatch } from 'umi';
import { SettingOutlined } from '@ant-design/icons';
import {
  ESensorStatus,
  INetworkPolicy,
  INetworkSensor,
} from '@/pages/app/Configuration/Network/typings';
import { IApplicationPolicy } from '../../../ApplicationPolicy/typings';

const { Option } = Select;
const Index: React.FC<{
  dispatch: Dispatch;
  allNetworkSensor: INetworkSensor[];
  networkIngestPolicy: INetworkPolicy[];
  allApplicationPolicy: IApplicationPolicy[];
}> = ({ dispatch, allNetworkSensor, networkIngestPolicy, allApplicationPolicy = [] }) => {
  // 表单ref
  const form = useRef<any>();
  // 表单提交
  const handleSubmit = (values: any) => {
    Modal.confirm({
      width: 500,
      title: '确定保存吗?',
      icon: <SettingOutlined />,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        const success = await dispatch({
          type: 'networkModel/createNetworkApplicationPolicy',
          payload: values,
        });
        if (success) {
          Modal.success({
            keyboard: false,
            title: '保存成功',
            okText: '返回列表页',
            onOk: () => {
              history.goBack();
            },
          });
          return;
        }
      },
    });
  };

  useEffect(() => {
    /** 查询探针网络 */
    dispatch({
      type: 'networkModel/queryAllNetworkSensor',
    });
    dispatch({
      type: 'applicationPolicyModel/queryAllApplicationPolicies',
    });
  }, []);

  return (
    <Form
      name="network-ingest-groups"
      labelCol={{ span: 5 }}
      wrapperCol={{ span: 16 }}
      ref={form}
      onFinish={handleSubmit}
      style={{ marginTop: 20 }}
    >
      <Form.Item
        label="网络"
        name="networkId"
        rules={[{ required: true, message: '必须选择探针网络' }]}
      >
        <Select
          showSearch
          style={{ width: '100%' }}
          dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
          placeholder="请选择探针网络"
          allowClear
        >
          {allNetworkSensor.map((item) => (
            <Option key={item.networkInSensorId} value={item.networkInSensorId}>
              {item.name}[{item.sensorName}]{item.status === ESensorStatus.OFFLINE ? '(离线)' : ''}
            </Option>
          ))}
        </Select>
      </Form.Item>
      <Form.Item
        label="规则名称"
        name="policyId"
        rules={[{ required: true, message: '必须选择规则名称' }]}
      >
        <Select
          showSearch
          style={{ width: '100%' }}
          dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
          placeholder="请选择规则名称"
          allowClear
        >
          {allApplicationPolicy.map((item) => (
            <Option key={item.id} value={item.id}>
              {item.name}
            </Option>
          ))}
        </Select>
      </Form.Item>
      <Form.Item wrapperCol={{ offset: 5, span: 16 }}>
        <Button type="primary" htmlType="submit" style={{ marginRight: '20px' }}>
          确定
        </Button>
        <Button
          onClick={() => {
            history.push('/configuration/netflow/network-application-policy');
          }}
        >
          取消
        </Button>
      </Form.Item>
    </Form>
  );
};

export default connect(
  ({
    applicationPolicyModel: { allApplicationPolicy },
    networkModel: { allNetworkSensor, networkIngestPolicy },
  }: ConnectState) => ({
    allNetworkSensor,
    networkIngestPolicy,
    allApplicationPolicy,
  }),
)(Index as any);
