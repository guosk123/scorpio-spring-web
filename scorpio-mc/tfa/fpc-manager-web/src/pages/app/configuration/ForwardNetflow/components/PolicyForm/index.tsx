import {
  PORT_MAX_NUMBER,
  PORT_MIN_NUMBER,
} from '@/pages/app/appliance/TransmitTask/components/TransmitTaskForm';
import { queryDeviceNetifs } from '@/services/app/deviceNetif';
import { macAddressRegex, parseArrayJson } from '@/utils/utils';
import { MinusCircleOutlined } from '@ant-design/icons';
import type { RadioChangeEvent } from 'antd';
import { Tag } from 'antd';
import {
  Button,
  Card,
  Checkbox,
  Col,
  Form,
  Input,
  InputNumber,
  Radio,
  Row,
  Select,
  Space,
} from 'antd';
import type { CSSProperties } from 'react';
import { useEffect, useState } from 'react';
import { history } from 'umi';
import type { INetif } from '../../../DeviceNetif/typings';
import { ENetifCategory } from '../../../DeviceNetif/typings';
import { queryAllNetworks } from '../../../Network/service';
import type { INetwork } from '../../../Network/typings';
import {
  createForwardPolicy,
  queryForwardRulesList,
  queryNetworkPolicy,
  updateForwardPolicy,
} from '../../service';
import type { IForwardPolicy } from '../../typings';
import { EForwardPolicyIPTunnelMode, EIPTunnelChecksum, ELoadBalanceType } from '../../typings';
import { checkSourceOrDestIp } from '../common';
import styles from '../style.less';

const tupleTitleStyle: CSSProperties = {
  textAlign: 'center',
};

const MIN_VNID = 0;
const MAX_VNID = 16777215;
const MIN_GRE_KEY = 0;
const MAX_GRE_KEY = 4294967295;
const MAX_TUPLE_COUNT = 8;
interface Props {
  detail?: IForwardPolicy;
}

const requiredDom = <span style={{ color: 'red' }}>*</span>;

const greTitle = (
  <>
    <Col style={{ ...tupleTitleStyle }} span={1} />
    <Col style={{ ...tupleTitleStyle }} span={4}>
      源MAC
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={4}>
      {requiredDom}目的MAC
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={4}>
      {requiredDom}源IP
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={4}>
      {requiredDom}目的IP
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={3}>
      key
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={3}>
      计算校验和
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={1} />
  </>
);

const vxlanTitle = (
  <>
    <Col style={{ ...tupleTitleStyle }} span={1} />
    <Col style={{ ...tupleTitleStyle }} span={4}>
      源MAC
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={4}>
      {requiredDom}目的MAC
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={4}>
      {requiredDom}源IP
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={4}>
      {requiredDom}目的IP
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={2}>
      {requiredDom}源端口
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={2}>
      {requiredDom}目的端口
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={2}>
      {requiredDom}VNID
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={1} />
  </>
);

const loadBalanceExtra = (
  <section>
    <ul style={{ listStyle: 'decimal', paddingLeft: 20 }}>
      <li>勾选负载均衡，不进行隧道封装，对于命中的流量，会按照HASH规则选择接口，进行转发</li>
      <li>
        勾选负载均衡，且隧道封装为GRE或VXLAN时，对命中的流量会按照HASH规则选择封装方式，进行转发
      </li>
      <li>不勾选负载均衡，且隧道封装为不封装时，对命中的流量每个接口会复制一份进行转发</li>
      <li>不勾选负载均衡，且隧道封装为GRE或VXLAN时，对命中的流量每条封装配置会复制一份进行转发</li>
    </ul>
  </section>
);

const PolicyForm = (props: Props) => {
  const { detail } = props;
  const [form] = Form.useForm();

  const [forwardRules, setForwardRules] = useState<{ label: string; value: string }[]>([]);
  const [networks, setNetworks] = useState<INetwork[]>([]);
  const [netifs, setNetifs] = useState<INetif[]>([]);
  const [ipTunnelMode, setIpTunnelMode] = useState<EForwardPolicyIPTunnelMode>(
    EForwardPolicyIPTunnelMode.不封装,
  );

  const [loadBalance, setLoadBalance] = useState(false);
  const [networkPolicyMap, setNetworkPolicyMap] = useState<Record<string, string[]>>({});

  useEffect(() => {
    queryForwardRulesList().then(({ success, result }) => {
      if (success) {
        setForwardRules(
          result.map((item) => {
            return {
              label: item.name,
              value: item.id,
            };
          }),
        );
      }
    });

    queryNetworkPolicy().then((res) => {
      const { success, result } = res;
      if (success) {
        setNetworkPolicyMap(result);
      }
    });

    // 网络，策略的配置网络只能是主网络
    // 每个网络最多只能配置4个策略
    queryAllNetworks().then((res) => {
      const { success, result } = res;
      if (success) {
        setNetworks(result);
      }
    });

    // 接口
    queryDeviceNetifs().then(({ success, result }) => {
      if (success) {
        // 配置转发策略时，只能从重放口中选择
        setNetifs((result as INetif[]).filter((item) => item.category === ENetifCategory.REPLAY));
      }
    });

    if (detail) {
      const { networkId, netifName, ipTunnel, loadBalance: lb, ...rest } = detail;
      if (lb) {
        setLoadBalance(true);
      }

      setIpTunnelMode(((JSON.parse(ipTunnel) as any) || {}).mode);

      form.setFieldsValue({
        ...rest,
        networkId: parseArrayJson(networkId),
        netifName: parseArrayJson(netifName),
        ipTunnel: JSON.parse(ipTunnel),
        loadBalance: lb ? lb : undefined,
      });
    } else {
      form.setFieldsValue({
        ipTunnel: {
          mode: EForwardPolicyIPTunnelMode.不封装,
        },
      });
    }

    // 初始化页面参数，仅执行一遍
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleFinish = (values: any) => {
    const { networkId, netifName, ipTunnel, ...rest } = values;
    const serializeValue = {
      networkId: JSON.stringify(networkId),
      netifName: JSON.stringify(netifName),
      ipTunnel: JSON.stringify(ipTunnel),
      loadBalance: loadBalance ? values.loadBalance : null,
    };
    if (detail) {
      const policy = { id: detail.id, ...rest, ...serializeValue };
      updateForwardPolicy(policy).then(({ success }) => {
        if (success) {
          history.goBack();
        }
      });
    } else {
      createForwardPolicy({ ...rest, ...serializeValue }).then(({ success }) => {
        if (success) {
          history.goBack();
        }
      });
    }
  };

  const handleIpTunnelModeChange = (e: RadioChangeEvent) => {
    setIpTunnelMode(e.target.value);
  };

  return (
    <Form wrapperCol={{ span: 12 }} labelCol={{ span: 4 }} form={form} onFinish={handleFinish}>
      <Form.Item label="策略名称" name="name" rules={[{ required: true }]}>
        <Input />
      </Form.Item>
      <Form.Item label="转发规则" name="ruleId" rules={[{ required: true }]}>
        <Select options={forwardRules} allowClear />
      </Form.Item>
      <Form.Item label="网络" name="networkId" rules={[{ required: true }]}>
        <Select
          mode="multiple"
          allowClear
          // tag的默认逻辑是， 当option为disable时，渲染的tag是不能close的
          // 所以这里需要使用tagRender自己写渲染逻辑
          tagRender={(tagProps) => (
            <Tag
              {...tagProps}
              closable={true}
              onClose={() => {
                // tag无论何时都可以关闭，关闭时，充填form值，
                // 注意更新networkPolicyMap
                const prevNetworkId = form.getFieldValue('networkId');
                setNetworkPolicyMap((prev) => {
                  return {
                    ...prev,
                    [tagProps.value]: [
                      ...(prev[tagProps.value]?.filter((item) => item !== detail?.id) || []),
                    ],
                  };
                });
                form.setFieldsValue({
                  networkId: prevNetworkId?.filter((item: string) => item !== tagProps.value),
                });
                form.validateFields();
              }}
            >
              {tagProps.label}
            </Tag>
          )}
          options={networks.map((network) => {
            const disabled =
              !!networkPolicyMap[network.id]?.length && networkPolicyMap[network.id]?.length >= 4;
            return {
              label: network.name,
              value: network.id,
              disabled: disabled,
            };
          })}
        />
      </Form.Item>
      <Form.Item label="转发接口" name="netifName" rules={[{ required: true }]}>
        <Select
          allowClear
          mode="multiple"
          options={netifs.map((item) => {
            return { label: item.name, value: item.name };
          })}
        />
      </Form.Item>
      <Form.Item label="隧道封装" required>
        <Form.Item
          name={['ipTunnel', 'mode']}
          rules={[{ type: 'enum', enum: Object.values(EForwardPolicyIPTunnelMode) }]}
        >
          <Radio.Group
            value={ipTunnelMode}
            onChange={handleIpTunnelModeChange}
            options={Object.keys(EForwardPolicyIPTunnelMode).map((label) => ({
              label,
              value: EForwardPolicyIPTunnelMode[label],
            }))}
          />
        </Form.Item>
        {ipTunnelMode !== EForwardPolicyIPTunnelMode.不封装 && (
          <Form.Item>
            <Form.List name={['ipTunnel', 'params']}>
              {(fields, { add, remove }) => {
                return (
                  <Card bordered size="small" className={styles.ipTunnel}>
                    <Row gutter={4}>
                      {ipTunnelMode === EForwardPolicyIPTunnelMode.GRE封装 && greTitle}
                      {ipTunnelMode === EForwardPolicyIPTunnelMode.VXLAN封装 && vxlanTitle}
                    </Row>
                    {fields.map(({ key, name, ...restField }, index) => {
                      return (
                        <Row key={key} gutter={4} style={{ height: '3em' }}>
                          <Col span={1}>
                            <Form.Item>{index + 1}</Form.Item>
                          </Col>
                          <Col span={4}>
                            <Form.Item
                              {...restField}
                              name={[name, 'sourceMac']}
                              rules={[
                                { required: false, message: '请输入源MAC' },
                                { pattern: macAddressRegex, message: '请输入正确的MAC地址' },
                              ]}
                            >
                              <Input />
                            </Form.Item>
                          </Col>
                          <Col span={4}>
                            <Form.Item
                              {...restField}
                              name={[name, 'destMac']}
                              rules={[
                                { required: true, message: '请输入目的MAC' },
                                { pattern: macAddressRegex, message: '请输入正确的MAC地址' },
                              ]}
                            >
                              <Input />
                            </Form.Item>
                          </Col>
                          <Col span={4}>
                            <Form.Item
                              {...restField}
                              name={[name, 'sourceIp']}
                              rules={[
                                { required: true, message: '请输入源IP' },
                                { validator: checkSourceOrDestIp },
                              ]}
                            >
                              <Input />
                            </Form.Item>
                          </Col>
                          <Col span={4}>
                            <Form.Item
                              {...restField}
                              name={[name, 'destIp']}
                              rules={[
                                { required: true, message: '请输入目的IP' },
                                { validator: checkSourceOrDestIp },
                              ]}
                            >
                              <Input />
                            </Form.Item>
                          </Col>
                          {ipTunnelMode === EForwardPolicyIPTunnelMode.GRE封装 && (
                            <>
                              <Col style={{ ...tupleTitleStyle }} span={3}>
                                <Form.Item {...restField} name={[name, 'key']}>
                                  <InputNumber
                                    style={{ width: '100%' }}
                                    min={MIN_GRE_KEY}
                                    max={MAX_GRE_KEY}
                                    precision={0}
                                  />
                                </Form.Item>
                              </Col>
                              <Col style={{ ...tupleTitleStyle }} span={3}>
                                <Form.Item>
                                  <Form.Item {...restField} name={[name, 'checksum']}>
                                    <Select
                                      options={[
                                        { label: '计算', value: EIPTunnelChecksum.计算 },
                                        { label: '不计算', value: EIPTunnelChecksum.不计算 },
                                      ]}
                                    />
                                  </Form.Item>
                                </Form.Item>
                              </Col>
                            </>
                          )}
                          {ipTunnelMode === EForwardPolicyIPTunnelMode.VXLAN封装 && (
                            <>
                              <Col style={{ ...tupleTitleStyle }} span={2}>
                                <Form.Item
                                  {...restField}
                                  name={[name, 'sourcePort']}
                                  rules={[{ required: true, message: '请输入源端口' }]}
                                >
                                  <InputNumber
                                    style={{ width: '100%' }}
                                    min={PORT_MIN_NUMBER}
                                    max={PORT_MAX_NUMBER}
                                    precision={0}
                                  />
                                </Form.Item>
                              </Col>
                              <Col style={{ ...tupleTitleStyle }} span={2}>
                                <Form.Item
                                  {...restField}
                                  name={[name, 'destPort']}
                                  rules={[{ required: true, message: '请输入目的端口' }]}
                                >
                                  <InputNumber
                                    style={{ width: '100%' }}
                                    min={PORT_MIN_NUMBER}
                                    max={PORT_MAX_NUMBER}
                                    precision={0}
                                  />
                                </Form.Item>
                              </Col>
                              <Col style={{ ...tupleTitleStyle }} span={2}>
                                <Form.Item
                                  {...restField}
                                  name={[name, 'vnid']}
                                  rules={[{ required: true, message: '请输入VNID' }]}
                                >
                                  <InputNumber
                                    style={{ width: '100%' }}
                                    min={MIN_VNID}
                                    max={MAX_VNID}
                                    precision={0}
                                  />
                                </Form.Item>
                              </Col>
                            </>
                          )}
                          <Col span={1}>
                            <Form.Item>
                              <MinusCircleOutlined onClick={() => remove(name)} />
                            </Form.Item>
                          </Col>
                        </Row>
                      );
                    })}
                    <Form.Item
                      style={{ marginBottom: 0, textAlign: 'center' }}
                      wrapperCol={{ span: 4, offset: 10 }}
                    >
                      {fields.length < MAX_TUPLE_COUNT ? (
                        <Button type="primary" size="small" onClick={() => add()} block>
                          添加
                        </Button>
                      ) : (
                        <Button type="primary" size="small" disabled>
                          最多可配置{MAX_TUPLE_COUNT}个
                        </Button>
                      )}
                    </Form.Item>
                  </Card>
                );
              }}
            </Form.List>
          </Form.Item>
        )}
      </Form.Item>
      <Form.Item label="负载均衡" required={true} extra={loadBalanceExtra}>
        <Form.Item>
          <Checkbox
            checked={loadBalance}
            onChange={(e) => {
              setLoadBalance(e.target.checked);
            }}
          />
        </Form.Item>
        {loadBalance && (
          <Form.Item name="loadBalance" label="HASH方式">
            <Select
              options={Object.keys(ELoadBalanceType).map((key) => {
                return {
                  label: key,
                  value: ELoadBalanceType[key],
                };
              })}
            />
          </Form.Item>
        )}
      </Form.Item>

      <Form.Item label="备注" name="description">
        <Input.TextArea />
      </Form.Item>
      <Form.Item />

      <Form.Item wrapperCol={{ offset: 4 }}>
        <Space>
          <Button type="primary" htmlType="submit">
            保存
          </Button>
          <Button
            style={{ marginLeft: 8 }}
            onClick={() => {
              history.goBack();
            }}
          >
            取消
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
};

export default PolicyForm;
