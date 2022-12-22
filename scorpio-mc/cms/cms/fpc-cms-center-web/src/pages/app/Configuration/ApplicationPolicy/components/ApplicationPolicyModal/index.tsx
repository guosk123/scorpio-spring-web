import {
  Button,
  Card,
  Col,
  Form,
  Input,
  InputNumber,
  Modal,
  Radio,
  Row,
  Select,
  TreeSelect,
} from 'antd';
import type { AppCategoryItem } from '../../../SAKnowledge/typings';
import { useEffect, useMemo, useState } from 'react';
import { EApplicationPolicyAction } from '../../typings';
import {
  convertComplexAppIdToSimple,
  convertSimpleAppIdToComplex,
  getApplicationTree,
} from '../../utils/appTree';
import { DEFAULT_POLICY_ID } from '../PolicyForm';
import { checkPort, checkSourceOrDestIp, checkVlan } from '../../utils/validators';
import type { INetworkGroup, INetworkSensor } from '../../../Network/typings';
import { exceptTupleExtra } from '../../../IngestPolicy/components/IngestPolicyForm';

const { Option } = Select;
interface IApplicationPolicyModalProps {
  /** Modal开关 */
  visiable: boolean;
  /** 关闭Modal时调用 */
  closeFunc: () => void;
  /** 策略列表，渲染应用 */
  allCategoryList: AppCategoryItem[];
  /* 探针网络 */
  allNetworkSensor: INetworkSensor[];
  /** 网络组 */
  allNetworkGroup: INetworkGroup[];
  /** 表单提交函数 */
  handleSubmit?: (formData: any) => void;
  /** 表单初始值 */
  initialValues: Record<string, any> | null;
  /** Modal标题 */
  title?: string;
}
const { TextArea } = Input;
/**
 * 传输层协议
 */
const IP_PROTOCOL_LIST = ['TCP', 'UDP'];

const layout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 16 },
};

export default function ApplicationPolicyModal({
  visiable,
  closeFunc,
  allCategoryList,
  allNetworkSensor,
  allNetworkGroup,
  handleSubmit,
  initialValues,
  title,
}: IApplicationPolicyModalProps) {
  const [form] = Form.useForm();
  /** 截断长度 */
  const [truncate, setTruncate] = useState<number>(64);
  const [networkList, setNetworkList] = useState<string[]>([]);
  const [networkGroupList, setNetworkGroupList] = useState<string[]>([]);
  /** 重制Modal */
  const resetModal = () => {
    form.resetFields();
    setTruncate(64);
  };

  /** 判断是否可编辑 */
  const ediable = useMemo(() => {
    return initialValues?.id !== DEFAULT_POLICY_ID;
  }, [initialValues]);

  useEffect(() => {
    setNetworkList([]);
    setNetworkGroupList([]);
  }, [visiable]);

  // 初始化表单
  const initForm = () => {
    if (initialValues) {
      const {
        name,
        sourceIp,
        sourcePort,
        destIp,
        destPort,
        protocol,
        vlanId,
        applicationId,
        action,
        networkId,
        networkGroupId,
        description,
        truncLen,
      } = initialValues;
      setTruncate(truncLen);
      setNetworkList(networkId?.split(',').filter((n: string) => n) || []);
      setNetworkGroupList(networkGroupId?.split(',').filter((n: string) => n) || []);
      form.setFieldsValue({
        name,
        sourceIp,
        sourcePort,
        destIp,
        destPort,
        protocol,
        vlanId,
        applicationId: convertComplexAppIdToSimple(applicationId),
        action,
        networkId: networkId?.split(',').filter((n: string) => n),
        networkGroupId: networkGroupId?.split(',').filter((n: string) => n),
        description,
      });
    }
  };

  useEffect(() => {
    if (!visiable) {
      resetModal();
    }
  }, [visiable]);

  /** 初始化 */
  useEffect(() => {
    if (initialValues) {
      initForm();
    }
  }, [initialValues]);

  /** 应用树 */
  const appTree = useMemo(() => {
    return getApplicationTree(allCategoryList);
  }, [allCategoryList]);

  return (
    <>
      <Modal
        title={title || ''}
        visible={visiable}
        maskClosable={false}
        destroyOnClose
        width={'70%'}
        footer={null}
        closable={false}
      >
        <Form
          name="basic"
          form={form}
          labelCol={{ span: 8 }}
          wrapperCol={{ span: 16 }}
          initialValues={{ remember: true }}
          onFinish={(e) => {
            Modal.confirm({
              title: initialValues ? '确定保存吗?' : '确定创建吗?',
              onOk: () => {
                const {
                  name,
                  sourceIp,
                  sourcePort,
                  destIp,
                  destPort,
                  protocol,
                  vlanId,
                  applicationId,
                  action,
                  networkId,
                  networkGroupId,
                  description,
                } = e;
                const tupleObj = {
                  sourceIp,
                  sourcePort,
                  destIp,
                  destPort,
                  protocol,
                  vlanId,
                  applicationId: convertSimpleAppIdToComplex(applicationId),
                  action,
                  truncLen: action === EApplicationPolicyAction.TRUNCATE ? truncate : undefined,
                };

                Object.keys(tupleObj).forEach((k) => {
                  if (tupleObj[k] === undefined) {
                    delete tupleObj[k];
                  }
                });
                const params = {
                  name,
                  tuple: JSON.stringify([tupleObj]),
                  networkId: networkId?.join(',') || '',
                  networkGroupId: networkGroupId?.join(',') || '',
                  description: description || '',
                };
                if (handleSubmit) {
                  handleSubmit(params);
                }
              },
            });
          }}
          autoComplete="off"
        >
          <Form.Item
            {...layout}
            label="名称"
            name="name"
            rules={[{ required: true, message: '请输入存储过滤策略名称!' }]}
          >
            <Input placeholder="请输入名称" disabled={!ediable} />
          </Form.Item>

          <Form.Item label="流过滤条件" {...layout} extra={exceptTupleExtra}>
            <Card>
              <Form.Item
                label="源IP"
                name="sourceIp"
                {...layout}
                rules={[
                  {
                    required: false,
                    message: '源IP不能为空',
                  },
                  { validator: checkSourceOrDestIp },
                ]}
              >
                <Input placeholder="请输入源IP" disabled={!ediable} />
              </Form.Item>

              <Form.Item
                label="目的IP"
                name="destIp"
                {...layout}
                rules={[
                  {
                    required: false,
                    message: '目的IP不能为空',
                  },
                  { validator: checkSourceOrDestIp },
                ]}
              >
                <Input placeholder="请输入目的IP" disabled={!ediable} />
              </Form.Item>

              <Form.Item
                label="源端口"
                name="sourcePort"
                {...layout}
                rules={[{ required: false, message: '源端口不能为空' }, { validator: checkPort }]}
              >
                <Input placeholder="请输入源端口" disabled={!ediable} />
              </Form.Item>

              <Form.Item
                label="目的端口"
                name="destPort"
                {...layout}
                rules={[{ required: false, message: '目的端口不能为空' }, { validator: checkPort }]}
              >
                <Input placeholder="请输入目的端口" disabled={!ediable} />
              </Form.Item>

              <Form.Item label="传输层协议" name="protocol" {...layout}>
                <Select placeholder="请选择传输层协议" disabled={!ediable}>
                  {IP_PROTOCOL_LIST.map((item) => (
                    <Select.Option value={item}>{item}</Select.Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item
                label="VLANID"
                name="vlanId"
                {...layout}
                rules={[
                  {
                    required: false,
                    validator: checkVlan,
                  },
                ]}
              >
                <Input placeholder="请输入VLANID" disabled={!ediable} />
              </Form.Item>

              <Form.Item label={'应用'} name={'applicationId'} {...layout}>
                <TreeSelect
                  disabled={!ediable}
                  treeData={appTree}
                  treeCheckable={true}
                  showSearch
                  // maxTagCount={1}
                  treeNodeFilterProp={'title'}
                  style={{ width: '100%' }}
                  placeholder="请选择应用"
                  dropdownStyle={{ maxHeight: 400, overflow: 'auto', minWidth: 400 }}
                  showCheckedStrategy={TreeSelect.SHOW_PARENT}
                />
              </Form.Item>
            </Card>
          </Form.Item>

          <Form.Item
            {...layout}
            label="动作"
            name="action"
            initialValue={EApplicationPolicyAction.STORE}
          >
            <Radio.Group>
              <Radio value={EApplicationPolicyAction.STORE}>全量存储</Radio>
              <Radio value={EApplicationPolicyAction.TRUNCATE}>截断存储</Radio>
              <InputNumber
                placeholder="截断长度范围为64-1500"
                min={64}
                max={1500}
                style={{ marginRight: '20px', width: '200px' }}
                value={truncate}
                onChange={(e) => setTruncate(e)}
              />
              <Radio value={EApplicationPolicyAction.DROP}>不存储</Radio>
            </Radio.Group>
          </Form.Item>

          {ediable ? (
            <>
              <Form.Item {...layout} label="作用网络" name="networkId">
                <Select
                  mode="multiple"
                  disabled={networkGroupList?.length !== 0}
                  onChange={(e) => setNetworkList(e)}
                  placeholder="请选择网络"
                >
                  {allNetworkSensor.map((item) => {
                    return <Option value={item.networkInSensorId}>{item.name}</Option>;
                  })}
                </Select>
              </Form.Item>

              <Form.Item {...layout} label="作用网络组" name="networkGroupId">
                <Select
                  mode="multiple"
                  disabled={networkList?.length !== 0}
                  onChange={(e) => setNetworkGroupList(e)}
                  placeholder="请选择网络组"
                >
                  {allNetworkGroup.map((item) => {
                    return <Option value={item.id}>{item.name}</Option>;
                  })}
                </Select>
              </Form.Item>
            </>
          ) : (
            <Form.Item {...layout} label="作用网络">
            <Select
              mode="multiple"
              disabled={true}
              placeholder="请选择网络"
              value={['all']}
            >
              <Option value={'all'}>ALL</Option>
            </Select>
          </Form.Item>
          )}

          <Form.Item name="description" {...layout} label="备注">
            <TextArea rows={4} placeholder="备注" disabled={!ediable} />
          </Form.Item>
          <Row>
            <Col offset={4} style={{ marginRight: '10px' }}>
              <Button type="primary" htmlType="submit">
                确定
              </Button>
            </Col>
            <Col>
              <Button
                onClick={() => {
                  closeFunc();
                }}
              >
                返回
              </Button>
            </Col>
          </Row>
        </Form>
      </Modal>
    </>
  );
}
