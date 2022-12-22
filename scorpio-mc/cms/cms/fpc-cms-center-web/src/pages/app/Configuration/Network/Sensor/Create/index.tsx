import { TreeSelect, Select, Form, Input, Button, Modal, message, Space, Divider } from 'antd';
import { useEffect, useRef, useState } from 'react';
import {
  queryNetworkSensorTree,
  createSensorNetwork,
  updateSensorNetwork,
  querySendPolicyStateOn,
} from '../../service';
import type { Dispatch } from 'umi';
import { useParams, history, connect } from 'umi';
import type { INetworkSensor, INetworkInSensor, INetworkSensorTree } from '../../typings';
import { EPageMode, EDeviceType, ENetworkSensorType } from '../../typings';
import { SettingOutlined } from '@ant-design/icons';

const { TextArea } = Input;
const { Option } = Select;
interface ISelectedNetworkTree extends INetworkSensorTree {
  label: string;
  key: string;
  value: string;
  child?: ISelectedNetworkTree[];
  disabled: boolean;
}
const Index: React.FC<{
  dispatch: Dispatch;
}> = ({ dispatch }) => {
  // 表单ref
  const form = useRef<any>();
  // 表单模式， 编辑｜创建
  const [formMode, setFormState] = useState<EPageMode>();
  // 表单id，用于编辑时查询初始数据
  const { id }: { id: string } = useParams();
  // 树状结构
  const [sensorNetworkTree, setSensorNetworkTree] = useState<ISelectedNetworkTree[]>();
  // 当前选中的探针
  const [currentSensor, setCurrentSensor] = useState<any>();
  // 探针中的网络列表
  const [netwokInSensors, setNetworkInSensors] = useState<INetworkInSensor[]>([]);
  const [sendPolicies, setSendPolicies] = useState<any[]>([]);
  // 编辑时，获取表单初始化数据
  const fetchSensorData = async () => {
    const result = await dispatch({
      type: 'networkModel/queryNetworkSensorById',
      payload: id,
    });
    const { setFieldsValue } = form.current;
    if (result) {
      // 为表单设置默认值
      const {
        name,
        sensorId,
        sensorName,
        sensorType,
        owner,
        networkInSensorName,
        networkInSensorId,
        description,
        sendPolicyIds,
      } = result as INetworkSensor;
      setFieldsValue({
        name: name,
        sensor: sensorName,
        sensorType: ENetworkSensorType[sensorType],
        owner,
        networkInSensorName: networkInSensorName,
        networkInSensorId: networkInSensorId,
        description: description,
        sendPolicyIds: sendPolicyIds?.split(',')?.filter(f=>f) || [],
      });
      setCurrentSensor({
        sensorId,
        sensorName,
        sensorType,
        owner,
      });
    }
  };
  // 获取树状结构
  const fetchSensorTree = async () => {
    const { success, result } = await queryNetworkSensorTree();
    if (success) {
      // 层序遍历修改树的结构
      const nodeQueue = [];
      nodeQueue.push(result);
      while (nodeQueue.length > 0) {
        const currentNode = nodeQueue.shift();
        currentNode.label = currentNode.deviceName;
        currentNode.key = currentNode.deviceSerialNumber;
        currentNode.value = currentNode.deviceSerialNumber;
        currentNode.data = currentNode;
        currentNode.children = currentNode.child;
        currentNode.disabled = currentNode.deviceType === EDeviceType.CMS;
        if (currentNode.child) {
          delete currentNode.child;
        }
        if (currentNode.children) {
          for (let i = 0; i < currentNode.children.length; i++) {
            nodeQueue.push(currentNode.children[i]);
          }
        }
      }
      setSensorNetworkTree([result]);
    }
  };

  // 层序遍历找到树节点
  const findTreeNodeById = (tree: any[], id: string) => {
    const queue = [...tree];
    while (queue.length > 0) {
      const treeNode = queue.shift();
      if (treeNode) {
        if (treeNode.key === id) {
          return treeNode;
        }
        queue.push(...(treeNode.children || []));
      }
    }
  };

  // 选取探针之后,获取探针中的网络
  const fetchNetworkInSensor = async () => {
    if (currentSensor) {
      setNetworkInSensors(
        (await dispatch({
          type: 'networkModel/queryNetworkInSensorListById',
          payload: currentSensor?.sensorId,
        })) || [],
      );
    }
  };

  // 表单提交
  const handleSubmit = async (item: any) => {
    const { networkInSensorId, name, description, sendPolicyIds } = item;
    const { sensorName, sensorId, sensorType, owner } = currentSensor;
    const networkInSensorName = netwokInSensors.find(
      (item) => item.fpcNetworkId === networkInSensorId,
    )?.fpcNetworkName;
    Modal.confirm({
      width: 500,
      title: '确定保存吗?',
      icon: <SettingOutlined />,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        if (formMode === EPageMode.create) {
          const { success } = await createSensorNetwork({
            networkInSensorId,
            networkInSensorName: networkInSensorName || '',
            name,
            description,
            sensorName,
            sensorId,
            sensorType,
            owner,
            sendPolicyIds: sendPolicyIds?.join(',')||'',
          });
          if (success) {
            message.success('保存成功!');
            Modal.success({
              keyboard: false,
              title: '保存成功',
              okText: '返回列表页',
              onOk: () => {
                history.goBack();
              },
            });
          }
          return;
        } else if (formMode === EPageMode.update) {
          const { success } = await updateSensorNetwork({
            id,
            networkInSensorId,
            networkInSensorName: networkInSensorName || '',
            name,
            description,
            sensorName,
            sensorId,
            sensorType,
            owner,
            sendPolicyIds: sendPolicyIds?.join(',') || '',
          });
          if (success) {
            message.success('保存成功!');
            Modal.success({
              keyboard: false,
              title: '保存成功',
              okText: '返回列表页',
              onOk: () => {
                history.goBack();
              },
            });
          }
        }
      },
    });
  };

  const fetchPolicies = async () => {
    const { success, result } = await querySendPolicyStateOn();
    if (success) {
      setSendPolicies(result);
    }
  };

  useEffect(() => {
    fetchPolicies();
  }, []);

  // 初始化数据
  useEffect(() => {
    const path = history.location.pathname;
    // 首先获取探针网络树状结构
    fetchSensorTree();
    // 判断表单页模式并且请求默认数据
    if (path.indexOf('update') !== -1) {
      setFormState(EPageMode.update);
      // 获取编辑数据
      fetchSensorData();
    } else if (path.indexOf('create') !== -1) {
      setFormState(EPageMode.create);
    } else {
      setFormState(EPageMode.create);
    }
  }, []);
  // 选中探针变化，重新请求该探针下的网络列表
  useEffect(() => {
    fetchNetworkInSensor();
  }, [currentSensor]);
  return (
    <Form
      name="basic"
      labelCol={{ span: 5 }}
      wrapperCol={{ span: 16 }}
      ref={form}
      onFinish={handleSubmit}
      style={{ marginTop: 20 }}
    >
      <Form.Item
        label="探针设备"
        name="sensor"
        rules={[{ required: true, message: '必须选择探针设备' }]}
      >
        <TreeSelect
          showSearch
          style={{ width: '100%' }}
          dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
          placeholder="请选择探针设备"
          allowClear
          treeData={sensorNetworkTree}
          onChange={(id: string) => {
            if (id) {
              const {
                key: sensorId,
                label: sensorName,
                owner,
                sensorType,
              } = findTreeNodeById(sensorNetworkTree || [], id) || {};
              setCurrentSensor({ sensorId, sensorName, sensorType, owner });
            } else {
              const { resetFields, setFieldsValue } = form.current;
              resetFields();
              setCurrentSensor({});
            }
          }}
        />
      </Form.Item>
      <Form.Item label="探针类型" name="sensorType">
        <Input style={{ width: '100%' }} allowClear disabled />
      </Form.Item>
      <Form.Item label="管理CMS" name="owner">
        <Input style={{ width: '100%' }} allowClear disabled />
      </Form.Item>
      <Form.Item
        label="网络"
        name="networkInSensorName"
        rules={[{ required: true, message: '必须选择网络' }]}
      >
        <Select
          showSearch
          style={{ width: '100%' }}
          dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
          disabled={currentSensor === undefined}
          placeholder="请选择探针中的网络"
          allowClear
          onChange={(networkInSensorId) => {
            const { setFieldsValue } = form.current;
            setFieldsValue({ networkInSensorId, name: '' });
          }}
        >
          {netwokInSensors.map((item) => {
            return (
              <Option key={item.fpcNetworkId} value={item.fpcNetworkId}>
                {item.fpcNetworkName}
              </Option>
            );
          })}
        </Select>
      </Form.Item>
      <Form.Item label="网络ID" name="networkInSensorId">
        <Input style={{ width: '100%' }} allowClear disabled />
      </Form.Item>
      <Form.Item label="网络名称" name="name">
        <Input style={{ width: '100%' }} allowClear />
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
            history.push('/configuration/network/sensor');
          }}
        >
          取消
        </Button>
      </Form.Item>
    </Form>
  );
};
export default connect()(Index as any);
