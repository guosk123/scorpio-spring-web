import { Select, Form, Input, Button, Modal, Space, Divider } from 'antd';
import { useEffect, useRef, useState } from 'react';
import type { ConnectState } from '@/models/connect';
import type { INetworkGroupsParams} from '../../service';
import { querySendPolicyStateOn } from '../../service';
import type { Dispatch } from 'umi';
import { useParams, history, connect } from 'umi';
import type { INetworkSensor } from '../../typings';
import { ESensorStatus } from '../../typings';
import { EPageMode } from '../../typings';
import { SettingOutlined } from '@ant-design/icons';

const { TextArea } = Input;
const { Option } = Select;

const Index: React.FC<{
  dispatch: Dispatch;
  allNetworkSensor: INetworkSensor[];
}> = ({ dispatch, allNetworkSensor }) => {
  // 表单ref
  const form = useRef<any>();
  // 编辑时网络组id
  const { id }: { id: string } = useParams();
  // 表单类型
  const [formMode, setFormMode] = useState<EPageMode>();
  const [sendPolicies, setSendPolicies] = useState<any[]>([]);
  // 表单提交
  const handleSubmit = (values: any) => {
    const { id, name, description, sensorNetwork, sendPolicyIds } = values;
    const params: INetworkGroupsParams = {
      id: id,
      name: name,
      description: description || '',
      networkInSensorIds: sensorNetwork.join(','),
      sendPolicyIds: sendPolicyIds?.join(','),
    };

    Modal.confirm({
      width: 500,
      title: '确定保存吗?',
      icon: <SettingOutlined />,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        if (formMode === EPageMode.create) {
          const success = await dispatch({
            type: 'networkModel/createNetworkGroup',
            payload: { ...params },
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
        } else if (formMode === EPageMode.update) {
          const success = await dispatch({
            type: 'networkModel/updateNetworkGroup',
            payload: { ...params },
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
        }
      },
    });
  };
  // 网络列表
  // 获取表单默认数据
  const fetchGroupData = async () => {
    const result = await dispatch({
      type: 'networkModel/queryNetworkGroupById',
      payload: id,
    });
    const { setFieldsValue } = form.current;
    if (result) {
      const { name, id, networkInSensorIds, description, sendPolicyIds } = result;
      // 为表单设置默认值
      setFieldsValue({
        name: name,
        id: id,
        sensorNetwork: networkInSensorIds
          .split(',')
          .filter((networkInSensorId: string) =>
            allNetworkSensor.find((item) => item.networkInSensorId === networkInSensorId),
          ),
        description: description,
        sendPolicyIds: sendPolicyIds?.split(',').filter((f: any) => f),
      });
    }
  };
  // 获取初始化数据
  const initialFormData = async () => {
    const path = history.location.pathname;
    dispatch({
      type: 'networkModel/queryAllNetworkSensor',
    });
    if (path.indexOf(EPageMode[EPageMode.create]) !== -1) {
      setFormMode(EPageMode.create);
    } else if (path.indexOf(EPageMode[EPageMode.update]) !== -1) {
      setFormMode(EPageMode.update);
      fetchGroupData();
    }
  };

  useEffect(() => {
    initialFormData();
  }, []);

  const fetchPolicies = async () => {
    const { success, result } = await querySendPolicyStateOn();
    if (success) {
      setSendPolicies(result);
    }
  };

  useEffect(() => {
    fetchPolicies();
  }, []);

  return (
    <Form
      name="network-groups"
      labelCol={{ span: 5 }}
      wrapperCol={{ span: 16 }}
      ref={form}
      onFinish={handleSubmit}
      style={{ marginTop: 20 }}
    >
      <Form.Item
        label="网络组名称"
        name="name"
        rules={[{ required: true, message: '必须输入网络组名称' }]}
      >
        <Input style={{ width: '100%' }} allowClear />
      </Form.Item>
      {formMode === EPageMode.update ? (
        <Form.Item label="网络组ID" name="id">
          <Input style={{ width: '100%' }} allowClear disabled />
        </Form.Item>
      ) : (
        ''
      )}
      <Form.Item
        label="网络选择"
        name="sensorNetwork"
        rules={[{ required: true, message: '必须选择网络' }]}
      >
        <Select
          showSearch
          style={{ width: '100%' }}
          dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
          mode="multiple"
          placeholder="请选择网络"
          allowClear
        >
          {allNetworkSensor.map((item) => (
            <Option key={item.networkInSensorId} value={item.networkInSensorId}>
              {item.name}[{item.sensorName}]{item.status === ESensorStatus.OFFLINE ? `(离线)` : ''}
            </Option>
          ))}
        </Select>
      </Form.Item>
      <Form.Item
        label="外发策略"
        {...{
          labelCol: {
            xs: { span: 24 },
            sm: { span: 5 },
          },
          wrapperCol: {
            xs: { span: 24 },
            sm: { span: 20 },
            md: { span: 18 },
          },
        }}
      >
        <Space>
          <Form.Item name="sendPolicyIds" noStyle>
            <Select
              mode="multiple"
              placeholder="请选择外发策略"
              style={{ width: '53vw' }}
              dropdownRender={(menu) => {
                return (
                  <>
                    {menu}
                    <Divider style={{ margin: '0px' }} />
                    <Button
                      style={{ margin: '5px' }}
                      onClick={() => {
                        fetchPolicies();
                      }}
                      size="small"
                      type="link"
                    >
                      刷新
                    </Button>
                  </>
                );
              }}
            >
              {sendPolicies.map((policy) => {
                return <Option value={policy.id}>{policy.name}</Option>;
              })}
            </Select>
          </Form.Item>
          <Button
            type="link"
            onClick={() => {
              window.open('/#/configuration/transmit/send-policy/create');
            }}
          >
            新建外发策略
          </Button>
        </Space>
      </Form.Item>
      <Form.Item
        label="描述"
        name="description"
        rules={[{ max: 255, message: '描述不能大于255个字符' }]}
      >
        <TextArea placeholder="请输入描述信息" style={{ width: '100%' }} rows={5} />
      </Form.Item>
      <Form.Item wrapperCol={{ offset: 5, span: 16 }}>
        <Button type="primary" htmlType="submit" style={{ marginRight: '20px' }}>
          保存
        </Button>
        <Button
          onClick={() => {
            history.push('/configuration/network/group');
          }}
        >
          取消
        </Button>
      </Form.Item>
    </Form>
  );
};

export default connect(({ networkModel: { allNetworkSensor } }: ConnectState) => ({
  allNetworkSensor,
}))(Index as any);
