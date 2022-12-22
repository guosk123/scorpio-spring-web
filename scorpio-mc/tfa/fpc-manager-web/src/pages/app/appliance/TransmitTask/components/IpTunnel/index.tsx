import { isIpAddress, isIpv4, macAddressRegex, parseObjJson } from '@/utils/utils';
import { QuestionCircleOutlined } from '@ant-design/icons';
import type { FormInstance } from 'antd';
import { Card, Col, Form, Input, InputNumber, Radio, Row, Select, Tooltip } from 'antd';
import React, { useEffect, useState } from 'react';
import type { IIpTunnel } from '../../typings';
import { EIpTunnelCheckSum, EIpTunnelMode } from '../../typings';
import { formLayout, formTailLayout } from '../TransmitTaskForm';
import { PORT_MAX_NUMBER, PORT_MIN_NUMBER } from '../TransmitTaskForm';

/** 隧道封装模式 */
const IP_TUNNEL_MODE_LIST = [
  { value: EIpTunnelMode.NONE, label: '不封装' },
  { value: EIpTunnelMode.GRE, label: 'GRE封装' },
  { value: EIpTunnelMode.VXLAN, label: 'VXLAN封装' },
];

const MIN_GRE_KEY = 0;
const MAX_GRE_KEY = 4294967295;

const MIN_VNID = 0;
const MAX_VNID = 16777215;

const formTunnelLayout = {
  labelCol: { span: 6 },
  wrapperCol: { span: 15 },
};

const formSmSizeLayout = {
  labelCol: { span: 10 },
  wrapperCol: { span: 15 },
};

export const initIpTunnel = {
  mode: EIpTunnelMode.NONE,
  params: {},
} as IIpTunnel;

/** 格式化隧道封装对象 */
export const parseIpTunnelJson = (ipTunnelString: string) => {
  let ipTunnelObj = initIpTunnel;
  if (ipTunnelString) {
    ipTunnelObj = parseObjJson(ipTunnelString) as IIpTunnel;
    if (!ipTunnelObj.params) {
      ipTunnelObj.params = {} as any;
    }
  }

  return ipTunnelObj;
};

interface IIpTunnelProps {
  value?: string;
  onChange?: (value: IIpTunnel) => void;
  form: FormInstance;
  smallSize?: boolean;
}
const IpTunnel: React.FC<IIpTunnelProps> = ({ value, onChange, smallSize = false }) => {
  const [ipTunnelJson] = useState<IIpTunnel>(() => {
    return parseIpTunnelJson(value!);
  });

  useEffect(() => {
    if (onChange) {
      onChange(ipTunnelJson);
    }
  }, [ipTunnelJson]);

  const renserTunnelParams = () => (
    <>
      <Col span={12}>
        <Form.Item
          {...(smallSize ? formSmSizeLayout : formTunnelLayout)}
          name={['ipTunnel', 'params', 'sourceMac']}
          initialValue={ipTunnelJson.params?.sourceMac}
          validateFirst
          label={
            <span>
              源MAC{' '}
              <Tooltip title="不配置时将使用已选择的转发接口的MAC地址">
                <QuestionCircleOutlined />
              </Tooltip>
            </span>
          }
          rules={[
            { required: false, message: '请输入源MAC' },
            { pattern: macAddressRegex, message: '请输入正确的MAC地址' },
          ]}
        >
          <Input />
        </Form.Item>
      </Col>
      <Col span={12}>
        <Form.Item
          {...(smallSize ? formSmSizeLayout : formTunnelLayout)}
          name={['ipTunnel', 'params', 'destMac']}
          initialValue={ipTunnelJson.params?.destMac}
          label="目的MAC"
          validateFirst
          rules={[
            { required: true, message: '请输入目的MAC' },
            { pattern: macAddressRegex, message: '请输入正确的MAC地址' },
          ]}
        >
          <Input />
        </Form.Item>
      </Col>
      <Col span={12}>
        <Form.Item
          shouldUpdate={(prevValues, currentValues) =>
            prevValues.ipTunnel?.params?.destIp !== currentValues.ipTunnel?.params?.destIp
          }
          noStyle
        >
          {({ getFieldValue }) => (
            <Form.Item
              {...(smallSize ? formSmSizeLayout : formTunnelLayout)}
              label="源IP"
              name={['ipTunnel', 'params', 'sourceIp']}
              initialValue={ipTunnelJson.params?.sourceIp}
              validateFirst
              rules={[
                { required: true, message: '请输入源IP' },
                {
                  validator: async (rule, sourceIp) => {
                    if (!sourceIp) {
                      throw new Error('请输入源IP');
                    }
                    if (!isIpAddress(sourceIp)) {
                      throw new Error('请输入正确的IPv4或IPv6地址');
                    }

                    const destIp = getFieldValue(['ipTunnel', 'params', 'destIp']);
                    if (destIp && isIpAddress(destIp)) {
                      // 源IP和目的IP值不能相同
                      if (sourceIp === destIp) {
                        throw new Error('源IP和目的IP不能相同');
                      }

                      // 源IP和目的IP类型相同，要么都是IPv4，要么都是IPv6
                      if (isIpv4(sourceIp) !== isIpv4(destIp)) {
                        throw new Error('源IP和目的IP类型应该相同');
                      }
                    }
                  },
                },
              ]}
            >
              <Input />
            </Form.Item>
          )}
        </Form.Item>
      </Col>
      <Col span={12}>
        <Form.Item
          shouldUpdate={(prevValues, currentValues) =>
            prevValues.ipTunnel?.params?.sourceIp !== currentValues.ipTunnel?.params?.sourceIp
          }
          noStyle
        >
          {({ getFieldValue }) => (
            <Form.Item
              {...(smallSize ? formSmSizeLayout : formTunnelLayout)}
              label="目的IP"
              name={['ipTunnel', 'params', 'destIp']}
              initialValue={ipTunnelJson.params?.destIp}
              validateFirst
              rules={[
                { required: true, message: '请输入目的IP' },
                {
                  validator: async (rule, destIp) => {
                    if (!destIp) {
                      throw new Error('请输入目的IP');
                    }
                    if (!isIpAddress(destIp)) {
                      throw new Error('请输入正确的IPv4或IPv6地址');
                    }
                    const sourceIp = getFieldValue(['ipTunnel', 'params', 'sourceIp']);
                    if (sourceIp && isIpAddress(sourceIp)) {
                      // 源IP和目的IP值不能相同
                      if (sourceIp === destIp) {
                        throw new Error('源IP和目的IP不能相同');
                      }

                      // 源IP和目的IP类型相同，要么都是IPv4，要么都是IPv6
                      if (isIpv4(sourceIp) !== isIpv4(destIp)) {
                        throw new Error('源IP和目的IP类型应该相同');
                      }
                    }
                  },
                },
              ]}
            >
              <Input />
            </Form.Item>
          )}
        </Form.Item>
      </Col>
    </>
  );

  return (
    <>
      {/* 隧道封装 */}
      <Form.Item
        name={['ipTunnel', 'mode']}
        label="隧道封装"
        {...formLayout}
        initialValue={ipTunnelJson.mode || EIpTunnelMode.NONE}
      >
        <Radio.Group>
          {IP_TUNNEL_MODE_LIST.map((mode) => (
            <Radio key={mode.value} value={mode.value}>
              {mode.label}
            </Radio>
          ))}
        </Radio.Group>
      </Form.Item>

      <Form.Item
        shouldUpdate={(prevValues, currentValues) =>
          prevValues.ipTunnel?.mode !== currentValues.ipTunnel?.mode
        }
        noStyle
      >
        {({ getFieldValue }) => {
          const ipTunnelMode = getFieldValue(['ipTunnel', 'mode']);
          if (ipTunnelMode === EIpTunnelMode.GRE) {
            return (
              <Form.Item key="gre" {...formTailLayout}>
                <Card bordered bodyStyle={{ padding: '10px 0' }}>
                  <Row>
                    {renserTunnelParams()}
                    <Col span={12}>
                      <Form.Item
                        name={['ipTunnel', 'params', 'key']}
                        label="KEY"
                        rules={[{ required: false, message: '请输入KEY' }]}
                        initialValue={ipTunnelJson.params?.key}
                        {...(smallSize ? formSmSizeLayout : formTunnelLayout)}
                      >
                        <InputNumber
                          style={{ width: '100%' }}
                          min={MIN_GRE_KEY}
                          max={MAX_GRE_KEY}
                          precision={0}
                        />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item
                        label="计算校验和"
                        name={['ipTunnel', 'params', 'checksum']}
                        initialValue={ipTunnelJson.params?.checksum || EIpTunnelCheckSum.NO}
                        {...(smallSize ? formSmSizeLayout : formTunnelLayout)}
                      >
                        <Select placeholder="">
                          <Select.Option value={EIpTunnelCheckSum.NO}>不计算</Select.Option>
                          <Select.Option value={EIpTunnelCheckSum.YES}>计算</Select.Option>
                        </Select>
                      </Form.Item>
                    </Col>
                  </Row>
                </Card>
              </Form.Item>
            );
          }

          if (ipTunnelMode === EIpTunnelMode.VXLAN) {
            return (
              <Form.Item key="vxlan" {...formTailLayout}>
                <Card bordered bodyStyle={{ padding: '10px 0' }}>
                  <Row>
                    {renserTunnelParams()}
                    <Col span={12}>
                      <Form.Item
                        label="源端口"
                        name={['ipTunnel', 'params', 'sourcePort']}
                        initialValue={ipTunnelJson.params?.sourcePort}
                        {...(smallSize ? formSmSizeLayout : formTunnelLayout)}
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
                    <Col span={12}>
                      <Form.Item
                        label="目的端口"
                        name={['ipTunnel', 'params', 'destPort']}
                        initialValue={ipTunnelJson?.params?.destPort || 4789}
                        {...(smallSize ? formSmSizeLayout : formTunnelLayout)}
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
                    <Col span={12}>
                      <Form.Item
                        label="VNID"
                        name={['ipTunnel', 'params', 'vnid']}
                        initialValue={ipTunnelJson?.params?.vnid}
                        {...(smallSize ? formSmSizeLayout : formTunnelLayout)}
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
                  </Row>
                </Card>
              </Form.Item>
            );
          }

          return null;
        }}
      </Form.Item>
    </>
  );
};

export default IpTunnel;
