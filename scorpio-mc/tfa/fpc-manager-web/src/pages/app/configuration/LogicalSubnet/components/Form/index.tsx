import type { ConnectState } from '@/models/connect';
import type { IpAddressGroup } from '@/pages/app/configuration/IpAddressGroup/typings';
import {
  createConfirmModal,
  ip2number,
  ipV4Regex,
  ipV6Regex,
  macAddressRegex,
  updateConfirmModal,
} from '@/utils/utils';
import { Button, Card, Form, Input, InputNumber, Radio, Select, Space } from 'antd';
import { Address6 } from 'ip-address';
import React, { useEffect } from 'react';
import type { Dispatch } from 'umi';
import { connect, history } from 'umi';
import type { INetwork } from '../../../Network/typings';
import type { ILogicalSubnet } from '../../typings';
import { GreSettingCategoryEnum, LogicalSubnetEnum, LOGICAL_SUBNET_NAME_OBJ } from '../../typings';

const FormItem = Form.Item;
const { TextArea } = Input;

/**
 * IP 地址最多个数量限制
 */
const MAX_IP_COUNT = 50;

/**
 * MAC 地址最多数量限制
 */
const MAX_MAC_COUNT = 50;
/**
 * VLANID字符串的最大长度
 */
const MAX_VLANID_STRING_LENGTH = 256;

/**
 * MPLS字符串的最大长度
 */
const MAX_MPLS_STRING_LENGTH = 256;
/**
 * GRE识别关键字字符串的最大长度
 */
const MAX_GRE_KEY_STRING_LENGTH = 256;
/**
 * VNI字符串的最大长度
 */
const MAX_VNI_STRING_LENGTH = 256;

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 4 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 20 },
    md: { span: 18 },
  },
};

const submitFormLayout = {
  wrapperCol: {
    xs: { span: 24, offset: 0 },
    sm: { span: 12, offset: 4 },
  },
};

const descUlStyle = {
  paddingLeft: 20,
  listStyle: 'circle',
  marginBottom: 0,
};

interface LogicalSubnetProps {
  type: 'create' | 'update';

  submitting?: boolean;
  dispatch: Dispatch;
  detail?: ILogicalSubnet;

  allNetworks: INetwork[];
  queryAllNetworksLoading?: boolean;

  allIpAddressGroupList: IpAddressGroup[];
  queryIpAddressLoading?: boolean;
}

const LogicalSubnet: React.FC<LogicalSubnetProps> = (props) => {
  const {
    detail = {} as ILogicalSubnet,
    allNetworks = [],
    queryAllNetworksLoading,
    allIpAddressGroupList = [],
    queryIpAddressLoading,
    submitting,
    dispatch,
  } = props;
  const [form] = Form.useForm();

  useEffect(() => {
    if (dispatch) {
      dispatch({
        type: 'ipAddressGroupModel/queryAllIpAddressGroup',
      });
      dispatch({
        type: 'logicSubnetModel/queryAllNetworks',
      });
    }
  }, [dispatch]);

  const handleGoBack = () => {
    history.goBack();
  };

  const handleReset = () => {
    form.resetFields();
  };

  const handleCreate = (values: any) => {
    createConfirmModal({
      dispatchType: 'logicSubnetModel/createLogicalSubnet',
      values,
      onOk: handleGoBack,
      onCancel: handleReset,
      dispatch,
    });
  };

  const handleUpdate = (values: any) => {
    updateConfirmModal({
      dispatchType: 'logicSubnetModel/updateLogicalSubnet',
      values,
      onOk: handleGoBack,
      dispatch,
      onCancel: () => {},
    });
  };

  const onFinishFailed = (errorInfo: any) => {
    // eslint-disable-next-line no-console
    console.log('Failed:', errorInfo);
  };

  const onFinish = (values: Record<string, any>) => {
    const { id, name, networkId, bandwidth, type, description, ...rest } = values;
    let configuration = null;
    if (type === LogicalSubnetEnum.IP) {
      configuration = rest.insideIpAddress?.split('\n').join(',') || '';
    }
    if (type === LogicalSubnetEnum.MAC) {
      configuration = rest.macAddress?.split('\n').join(',') || '';
    }
    if (type === LogicalSubnetEnum.VLAN) {
      configuration = rest.vlanId;
    }
    if (type === LogicalSubnetEnum.MPLS) {
      configuration = rest.mpls;
    }
    if (type === LogicalSubnetEnum.GRE) {
      configuration = JSON.stringify(rest.gre);
    }
    if (type === LogicalSubnetEnum.VXLAN) {
      configuration = rest.vni;
    }
    const data: ILogicalSubnet = {
      id,
      name,
      networkId,
      bandwidth,
      type,
      configuration,
      description,
    };

    if (id) {
      handleUpdate(data);
    } else {
      handleCreate(data);
    }
  };

  const quickSelectIpAddress = (ipAddress: string) => {
    // 往填充到内网配置里面
    if (ipAddress) {
      const oldIpAddress = form.getFieldValue(['insideIpAddress']);
      const splitIp = ipAddress.replace(/,/g, '\n');
      form.setFieldsValue({
        insideIpAddress: oldIpAddress ? `${oldIpAddress}\n${splitIp}` : splitIp,
      });
    }
  };

  /**
   * 校验内网 IP 地址
   */
  const checkIpAddress = async (rule: any, value: string) => {
    const passIpArr: string[] = []; // 已经检查通过的IP
    const valueArr = value.split('\n');

    for (let index = 0; index < valueArr.length; index += 1) {
      const item = valueArr[index];
      const lineText = `第${index + 1}行[${item}]: `;
      if (!item) {
        return Promise.reject(`${lineText}不能为空`);
      }

      // IP网段
      if (item.indexOf('/') > -1) {
        const [ip, mask] = item.split('/');

        const maskNum = +mask;

        if (!ipV4Regex.test(ip) && !ipV6Regex.test(ip)) {
          return Promise.reject(`${lineText}请输入正确的IP/IP段`);
        }

        if (ipV4Regex.test(ip) && (!mask || isNaN(maskNum) || maskNum <= 0 || maskNum > 32)) {
          return Promise.reject(`${lineText}请输入正确的IPv4网段。例，192.168.1.2/24`);
        }

        if (ipV6Regex.test(ip) && (!mask || isNaN(maskNum) || maskNum <= 0 || maskNum > 128)) {
          return Promise.reject(`${lineText}请输入正确的IPv6网段。例，2001:250:6EFA::/48`);
        }
      }

      // IP组
      else if (item.indexOf('-') > -1) {
        const ips = item.split('-');
        if (ips.length !== 2) {
          return Promise.reject(`${lineText}请输入正确的IP地址段。例，192.168.1.1-192.168.1.50`);
        }

        const [ip1, ip2] = ips;

        // 2个IPv4
        if (ipV4Regex.test(ip1) && ipV4Regex.test(ip2)) {
          const ip1Number = ip2number(ip1);
          const ip2Number = ip2number(ip2);

          // 起止地址是否符合大小要求
          if (ip1Number >= ip2Number) {
            return Promise.reject(`${lineText}IP地址段范围错误`);
          }
        }

        // 2个IPv6
        else if (ipV6Regex.test(ip1) && ipV6Regex.test(ip2)) {
          if (new Address6(ip1).bigInteger() >= new Address6(ip2).bigInteger()) {
            return Promise.reject(`${lineText}IP地址段范围错误`);
          }
        } else {
          return Promise.reject(`${lineText}请输入正确的IP地址段`);
        }
      } else if (!ipV4Regex.test(item) && !ipV6Regex.test(item)) {
        return Promise.reject(`${lineText}请输入正确的IP/IP段`);
      }

      // 是否重复了
      if (passIpArr.indexOf(item) !== -1) {
        return Promise.reject(`${lineText}已重复`);
      }
      passIpArr.push(item);
    }

    if (passIpArr.length > MAX_IP_COUNT) {
      return Promise.reject(`最多支持${MAX_IP_COUNT}个`);
    }

    return Promise.resolve();
  };

  /**
   * 校验 MAC 地址
   */
  const checkMacAddress = async (rule: any, value: string) => {
    const macArr = value.split('\n');
    const passArr: string[] = []; // 已经检查通过的IP
    for (let index = 0; index < macArr.length; index += 1) {
      const element = macArr[index];
      const lineText = `第${index + 1}行[${element}]: `;

      if (!element) {
        return Promise.reject(`${lineText}不能为空`);
      }
      if (!macAddressRegex.test(element)) {
        return Promise.reject(`${lineText}不是正确的MAC地址`);
      }
      if (passArr.indexOf(element) !== -1) {
        return Promise.reject(`${lineText}已重复`);
      }
      passArr.push(element);
    }

    if (passArr.length > MAX_MAC_COUNT) {
      return Promise.reject(`最多支持${MAX_MAC_COUNT}个`);
    }

    return Promise.resolve();
  };

  /**
   * 校验 VLANID
   */
  const checkVlanId = async (rule: any, value: string) => {
    const MIN_VLANID = 1;
    const MAX_VLANID = 4094;
    if (!/^[0-9,]+$/.test(value)) {
      return Promise.reject('只能输入数字和半角逗号,');
    }

    if (value.length > MAX_VLANID_STRING_LENGTH) {
      return Promise.reject(`最大长度为${MAX_VLANID_STRING_LENGTH}`);
    }

    const arr = value.split(',');
    const passArr: number[] = []; // 已经检查通过的值
    for (let index = 0; index < arr.length; index += 1) {
      const elementStr = arr[index];
      const lineText = `第${index + 1}个[${elementStr}]: `;

      if (!elementStr) {
        return Promise.reject(`不允许存在空值`);
      }
      const element = +elementStr;
      if (isNaN(element) || element < MIN_VLANID || element > MAX_VLANID) {
        return Promise.reject(`${lineText}不是正确的VLANID`);
      }
      if (passArr.indexOf(element) !== -1) {
        return Promise.reject(`${lineText}已重复`);
      }
      passArr.push(element);
    }

    return Promise.resolve();
  };

  /**
   * 校验 MPLS
   */
  const checkMPLS = async (rule: any, value: string) => {
    if (!/^[0-9,]+$/.test(value)) {
      return Promise.reject('只能输入数字和半角逗号,');
    }

    if (value.length > MAX_MPLS_STRING_LENGTH) {
      return Promise.reject(`最大长度为${MAX_MPLS_STRING_LENGTH}`);
    }

    const arr = value.split(',');
    const passArr: number[] = []; // 已经检查通过的值
    for (let index = 0; index < arr.length; index += 1) {
      const elementStr = arr[index];
      const lineText = `第${index + 1}个[${elementStr}]: `;

      if (!elementStr) {
        return Promise.reject(`不允许存在空值`);
      }
      const element = +elementStr;
      if (passArr.indexOf(element) !== -1) {
        return Promise.reject(`${lineText}已重复`);
      }
      passArr.push(element);
    }

    return Promise.resolve();
  };

  /**
   * 校验 GRE 识别关键字
   */
  const checkGreKey = async (rule: any, value: string) => {
    const MIN_GRE_KEY = 0;
    const MAX_GRE_KEY = 4294967295;

    if (!/^[0-9,]+$/.test(value)) {
      return Promise.reject('只能输入数字和半角逗号');
    }

    if (value.length > MAX_GRE_KEY_STRING_LENGTH) {
      return Promise.reject(`最大长度为${MAX_GRE_KEY_STRING_LENGTH}`);
    }

    const vlanidArr = value.split(',');
    const passArr: number[] = []; // 已经检查通过的值
    for (let index = 0; index < vlanidArr.length; index += 1) {
      const elementString = vlanidArr[index];
      const lineText = `第${index + 1}个[${elementString}]: `;

      if (!elementString) {
        return Promise.reject(`不允许存在空值`);
      }

      // 判断范围
      const element = +elementString;
      if (isNaN(element) || element < MIN_GRE_KEY || element > MAX_GRE_KEY) {
        return Promise.reject(`${lineText}范围错误，有效范围 ${MIN_GRE_KEY}-${MAX_GRE_KEY}`);
      }
      // 判断重复
      if (passArr.indexOf(element) !== -1) {
        return Promise.reject(`${lineText}已重复`);
      }
      passArr.push(element);
    }

    return Promise.resolve();
  };

  /**
   * 校验 GRE IP/IP对
   */
  const checkGreIp = async (rule: any, value: string) => {
    // IP 对
    if (value.indexOf('-') > -1) {
      const ipArr = value.split('-');
      if (ipArr.length !== 2) {
        return Promise.reject('不是正确的IP地址对');
      }
      const [ip1, ip2] = ipArr;
      if (!ip1 || !ip2) {
        return Promise.reject('不是正确的IP地址对');
      }

      // 2个IPv4
      if (ipV4Regex.test(ip1) && ipV4Regex.test(ip2)) {
        return Promise.resolve();
      }

      // 2个IPv6
      if (ipV6Regex.test(ip1) && ipV6Regex.test(ip2)) {
        return Promise.resolve();
      }
    }

    if (!ipV4Regex.test(value) && !ipV6Regex.test(value)) {
      return Promise.reject(`请输入正确的IP/IP地址对`);
    }

    return Promise.resolve();
  };

  /**
   * 校验 VNI 配置
   */
  const checkVNI = async (rule: any, value: string) => {
    if (!/^[0-9,]+$/.test(value)) {
      return Promise.reject('只能输入数字和半角逗号,');
    }

    if (value.length > MAX_VNI_STRING_LENGTH) {
      return Promise.reject(`最大长度为${MAX_VNI_STRING_LENGTH}`);
    }

    const arr = value.split(',');
    const passArr: number[] = []; // 已经检查通过的值
    for (let index = 0; index < arr.length; index += 1) {
      const elementStr = arr[index];
      const lineText = `第${index + 1}个[${elementStr}]: `;

      if (!elementStr) {
        return Promise.reject(`不允许存在空值`);
      }
      const element = +elementStr;
      if (passArr.indexOf(element) !== -1) {
        return Promise.reject(`${lineText}已重复`);
      }
      passArr.push(element);
    }

    return Promise.resolve();
  };

  let greTunnelJson = {};
  if (detail.type === LogicalSubnetEnum.GRE) {
    try {
      greTunnelJson = JSON.parse(detail.configuration);
    } catch (error) {
      greTunnelJson = {};
    }
  }

  return (
    <Card bordered={false}>
      <Form
        form={form}
        name="network-form"
        initialValues={{
          ...detail,
          // 处理各种配置
          insideIpAddress:
            detail.type === LogicalSubnetEnum.IP
              ? detail.configuration?.split(',').join('\n') || ''
              : '',
          macAddress:
            detail.type === LogicalSubnetEnum.MAC
              ? detail.configuration?.split(',').join('\n') || ''
              : '',
          vlanId: detail.type === LogicalSubnetEnum.VLAN ? detail.configuration : '',
          mpls: detail.type === LogicalSubnetEnum.MPLS ? detail.configuration : '',
          greTunnel: Object.keys(greTunnelJson).join(),
          gre: greTunnelJson,
          vni: detail.type === LogicalSubnetEnum.VXLAN ? detail.configuration : '',
          description: detail.description || '',
        }}
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
        scrollToFirstError
      >
        <FormItem {...formItemLayout} label="ID" name="id" hidden>
          <Input placeholder="逻辑子网id" />
        </FormItem>
        <FormItem
          {...formItemLayout}
          label="名称"
          name="name"
          rules={[
            {
              required: true,
              whitespace: true,
              message: '请填写子网名称',
            },
            { max: 30, message: '最多可输入30个字符' },
          ]}
        >
          <Input placeholder="填写子网名称" />
        </FormItem>
        <FormItem
          {...formItemLayout}
          label="所属网络"
          name="networkId"
          rules={[
            {
              required: true,
              message: '请选择所属网络',
            },
          ]}
        >
          <Select placeholder="选择所属网络" loading={queryAllNetworksLoading}>
            {allNetworks.map((network) => (
              <Select.Option key={network.id} value={network.id}>
                {network.name}
              </Select.Option>
            ))}
          </Select>
        </FormItem>

        <FormItem label="总带宽" {...formItemLayout} required>
          <Space>
            <FormItem
              noStyle
              label="总带宽"
              name="bandwidth"
              rules={[
                {
                  required: true,
                  message: '请填写子网总带宽',
                },
              ]}
            >
              <InputNumber
                min={0}
                precision={0}
                style={{ width: 200 }}
                placeholder="填写子网总带宽"
              />
            </FormItem>
            <span className="ant-form-text"> Mbps</span>
          </Space>
        </FormItem>

        <FormItem
          {...formItemLayout}
          label="子网类型"
          name="type"
          rules={[
            {
              required: true,
              message: '请选择子网类型',
            },
          ]}
        >
          <Select placeholder="选择子网类型">
            {Object.keys(LOGICAL_SUBNET_NAME_OBJ).map((type) => (
              <Select.Option key={type} value={type}>
                {LOGICAL_SUBNET_NAME_OBJ[type]}
              </Select.Option>
            ))}
          </Select>
        </FormItem>

        <Form.Item
          noStyle
          shouldUpdate={(prevValues, currentValues) => prevValues.type !== currentValues.type}
        >
          {({ getFieldValue }) => {
            const subnetType = getFieldValue('type');

            if (subnetType === LogicalSubnetEnum.IP) {
              return (
                <FormItem
                  {...formItemLayout}
                  label="IP地址配置"
                  required
                  style={{ marginBottom: 0 }}
                >
                  <FormItem label="选择IP地址组" style={{ marginBottom: 4 }}>
                    <Select
                      style={{ width: 300 }}
                      loading={queryIpAddressLoading}
                      onChange={quickSelectIpAddress}
                      value={''}
                    >
                      <Select.Option disabled value={''}>
                        可快速选择已有的IP地址组
                      </Select.Option>
                      {allIpAddressGroupList.map((group) => (
                        <Select.Option key={group.id} value={group.ipAddress || ''}>
                          {group.name}
                        </Select.Option>
                      ))}
                    </Select>
                  </FormItem>
                  <FormItem
                    name="insideIpAddress"
                    validateFirst
                    rules={[
                      {
                        required: true,
                        whitespace: true,
                        message: '请填写IP地址',
                      },
                      {
                        validator: checkIpAddress,
                      },
                    ]}
                    extra={
                      <ul style={{ ...descUlStyle }}>
                        <li>每行输入一种IP地址，最多支持{MAX_IP_COUNT}个；</li>
                        <li>可以输入【A.B.C.D】格式的IP地址；</li>
                        <li>或输入【A.B.C.D/掩码长度】格式的IP网段；</li>
                        <li>或输入【A.B.C.D-E.F.G.H】格式的IP组，请确保 E.F.G.H &gt;= A.B.C.D。</li>
                      </ul>
                    }
                  >
                    <TextArea rows={4} placeholder="请填写IP地址" />
                  </FormItem>
                </FormItem>
              );
            }

            if (subnetType === LogicalSubnetEnum.MAC) {
              return (
                <FormItem
                  {...formItemLayout}
                  label="MAC地址配置"
                  required
                  style={{ marginBottom: 0 }}
                >
                  <FormItem
                    name="macAddress"
                    validateFirst
                    rules={[
                      {
                        required: true,
                        whitespace: true,
                        message: '请填写MAC地址',
                      },
                      {
                        validator: checkMacAddress,
                      },
                    ]}
                    extra={
                      <ul style={{ ...descUlStyle }}>
                        <li>每行可输入一个MAC，最多支持{MAX_MAC_COUNT}个；</li>
                        <li>支持 2 种格式的MAC地址：01:23:45:67:89:ab 或 01-23-45-67-89-ab</li>
                      </ul>
                    }
                  >
                    <TextArea rows={4} placeholder="请填写MAC地址" />
                  </FormItem>
                </FormItem>
              );
            }
            if (subnetType === LogicalSubnetEnum.VLAN) {
              return (
                <FormItem {...formItemLayout} label="VLAN配置" required style={{ marginBottom: 0 }}>
                  <FormItem
                    name="vlanId"
                    validateFirst
                    rules={[
                      {
                        required: true,
                        whitespace: true,
                        message: '请填写VLANID',
                      },
                      {
                        validator: checkVlanId,
                      },
                    ]}
                    extra={`请输入VLANID，以半角逗号分隔，允许输入的最大长度为${MAX_VLANID_STRING_LENGTH}`}
                  >
                    <Input placeholder="请填写VLANID" />
                  </FormItem>
                </FormItem>
              );
            }
            if (subnetType === LogicalSubnetEnum.MPLS) {
              return (
                <FormItem
                  {...formItemLayout}
                  label="MPLS标签配置"
                  required
                  style={{ marginBottom: 0 }}
                >
                  <FormItem
                    name="mpls"
                    validateFirst
                    rules={[
                      {
                        required: true,
                        whitespace: true,
                        message: '请填写MPLS标签ID',
                      },
                      {
                        validator: checkMPLS,
                      },
                    ]}
                    extra={`请输入MPLS标签ID，以半角逗号分隔，允许输入的最大长度为${MAX_MPLS_STRING_LENGTH}`}
                  >
                    <Input placeholder="输入MPLS标签ID" />
                  </FormItem>
                </FormItem>
              );
            }
            if (subnetType === LogicalSubnetEnum.GRE) {
              return (
                <FormItem
                  {...formItemLayout}
                  label="GRE通道配置"
                  required
                  style={{ marginBottom: 0 }}
                >
                  <FormItem
                    style={{ marginBottom: 10 }}
                    name="greTunnel"
                    rules={[
                      {
                        required: true,
                        whitespace: true,
                        message: '请填写VNI配置',
                      },
                    ]}
                  >
                    <Radio.Group>
                      <Radio value={GreSettingCategoryEnum.KEY}>隧道识别关键字</Radio>
                      <Radio value={GreSettingCategoryEnum.IP}>隧道IP/IP对</Radio>
                    </Radio.Group>
                  </FormItem>

                  <Form.Item
                    noStyle
                    shouldUpdate={(prevValues, currentValues) =>
                      prevValues.greTunnel !== currentValues.greTunnel
                    }
                  >
                    {() => {
                      const greTunnel = getFieldValue('greTunnel');

                      if (greTunnel === GreSettingCategoryEnum.KEY) {
                        return (
                          <FormItem
                            name={['gre', greTunnel]}
                            validateFirst
                            rules={[
                              {
                                required: true,
                                whitespace: true,
                                message: '请填写隧道识别关键字',
                              },
                              {
                                validator: checkGreKey,
                              },
                            ]}
                            extra={`请输入隧道识别关键字，以半角逗号分隔，允许输入的最大长度为${MAX_GRE_KEY_STRING_LENGTH}`}
                          >
                            <Input placeholder="输入隧道识别关键字" />
                          </FormItem>
                        );
                      }

                      if (greTunnel === GreSettingCategoryEnum.IP) {
                        return (
                          <FormItem
                            name={['gre', greTunnel]}
                            validateFirst
                            rules={[
                              {
                                required: true,
                                whitespace: true,
                                message: '请填写隧道IP/IP地址对',
                              },
                              {
                                validator: checkGreIp,
                              },
                            ]}
                            extra={
                              <ul style={{ ...descUlStyle }}>
                                <li>支持单IP，或者IP地址对（例：192.168.1.1-192.168.2.2）</li>
                                <li>支持 IPv4 和 IPv6</li>
                              </ul>
                            }
                          >
                            <Input placeholder="输入隧道IP/IP对" />
                          </FormItem>
                        );
                      }

                      return null;
                    }}
                  </Form.Item>
                </FormItem>
              );
            }

            if (subnetType === LogicalSubnetEnum.VXLAN) {
              return (
                <FormItem {...formItemLayout} label="VNI配置" required style={{ marginBottom: 0 }}>
                  <FormItem
                    name="vni"
                    validateFirst
                    rules={[
                      {
                        required: true,
                        whitespace: true,
                        message: '请填写VNI配置',
                      },
                      {
                        validator: checkVNI,
                      },
                    ]}
                    extra={`请输入VNI，以半角逗号分隔，允许输入的最大长度为${MAX_VNI_STRING_LENGTH}`}
                  >
                    <Input placeholder="请输入VNI" />
                  </FormItem>
                </FormItem>
              );
            }

            return null;
          }}
        </Form.Item>

        <FormItem
          {...formItemLayout}
          name="description"
          label="描述信息"
          rules={[
            { required: false, message: '请输入描述信息' },
            { max: 255, message: '最多可输入255个字符' },
          ]}
        >
          <TextArea rows={4} placeholder="请输入描述信息" />
        </FormItem>

        <FormItem {...submitFormLayout}>
          <Button type="primary" htmlType="submit" loading={submitting}>
            保存
          </Button>
          <Button style={{ marginLeft: 8 }} onClick={handleGoBack}>
            取消
          </Button>
        </FormItem>
      </Form>
    </Card>
  );
};

export default connect(
  ({
    loading: { effects },
    ipAddressGroupModel: { allIpAddressGroupList },
    networkModel: { allNetworks },
  }: ConnectState) => ({
    allNetworks,
    queryAllNetworksLoading: effects['networkModel/queryAllNetworks'],
    allIpAddressGroupList,
    submitting:
      effects['logicSubnetModel/createLogicalSubnet'] ||
      effects['logicSubnetModel/updateLogicalSubnet'],
    queryIpAddressLoading: effects['ipAddressGroupModel/queryAllIpAddressGroup'],
  }),
)(LogicalSubnet);
