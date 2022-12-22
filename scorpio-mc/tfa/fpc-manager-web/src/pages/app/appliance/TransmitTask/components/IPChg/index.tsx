import { isIpAddress, isIpv4, parseObjJson } from '@/utils/utils';
import { FormInstance } from 'antd';
import { Card, Col, Form, Input, Radio, Row } from 'antd';
import React, { useEffect, useState } from 'react';
import { EIpChgMode, IIpChg } from '../../typings';
import { EMacChgMode } from '../../typings';
import { EPageMode } from '../TransmitTaskForm';

/** mac修改模式 */
const IP_CHG_MODE_LIST = [
  { value: EMacChgMode.NOT_CHANGE, label: '不修改' },
  { value: EMacChgMode.CHANGE, label: '修改' },
];

export const formTailLayout = {
  labelCol: { span: 3 },
  wrapperCol: { span: 19, offset: 3 },
};

export const formLayout = {
  labelCol: { span: 3 },
  wrapperCol: { span: 19 },
};

const formIpChgLayout = {
  labelCol: { span: 3 },
  wrapperCol: { span: 6 },
};

const formSmSizeLayout = {
  labelCol: { span: 5 },
  wrapperCol: { span: 10 },
};

export const initIpChg = {
  mode: EIpChgMode.NOT_CHANGE,
  rule: {},
} as IIpChg;

/** 格式化VLAN封装对象 */
export const parseIpChgJson = (ipChgString: string) => {
  let ipChgObj = initIpChg;
  if (ipChgString) {
    ipChgObj = parseObjJson(ipChgString) as IIpChg;
    if (!ipChgObj.rule) {
      ipChgObj.rule = {} as any;
    }
  }

  return ipChgObj;
};

interface IIpChgProps {
  value?: string;
  init?: string;
  onChange?: (value: IIpChg) => void;
  form: FormInstance;
  pageMode: EPageMode;
  smallSize?: boolean;
}
const IpChg: React.FC<IIpChgProps> = ({ init, value, onChange, form, pageMode,smallSize=false }) => {
  const [ipChgJson] = useState<IIpChg>(() => {
    return parseIpChgJson(init || '');
  });

  useEffect(() => {
    if (onChange) {
      onChange(ipChgJson);
    }
  }, [ipChgJson]);

  return (
    <>
      {/* IP修改 */}
      <Form.Item
        name={['ipChg', 'mode']}
        label="IP修改"
        {...formLayout}
        initialValue={ipChgJson.mode || EIpChgMode.NOT_CHANGE}
      >
        <Radio.Group disabled={pageMode === EPageMode.Update}>
          {IP_CHG_MODE_LIST.map((mode) => (
            <Radio key={mode.value} value={mode.value}>
              {mode.label}
            </Radio>
          ))}
        </Radio.Group>
      </Form.Item>

      <Form.Item
        shouldUpdate={(prevValues, currentValues) =>
          prevValues.vlanProcess?.mode !== currentValues.vlanProcess?.mode
        }
        noStyle
      >
        {({ getFieldValue }) => {
          const macChgMode = getFieldValue(['ipChg', 'mode']);
          if (macChgMode === EMacChgMode.CHANGE) {
            return (
              <Form.Item key={EMacChgMode.CHANGE} {...formTailLayout}>
                <Card bordered bodyStyle={{ padding: '10px 0' }}>
                  <Row>
                    <Col span={24}>
                      <Form.Item
                        {...(smallSize ? formSmSizeLayout : formIpChgLayout)}
                        label="源IP"
                        name={['ipChg', 'rule', 'sourceIp']}
                        initialValue={ipChgJson.rule?.sourceIp}
                        validateFirst
                        rules={[
                          { required: true },
                          {
                            validator: async (rule, sourceIp) => {
                              if (!sourceIp) {
                                throw new Error('请输入源IP');
                              }
                              if (!isIpAddress(sourceIp)) {
                                throw new Error('请输入正确的IPv4或IPv6地址');
                              }

                              const destIp = getFieldValue(['ipChg', 'rule', 'destIp']);
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
                        <Input disabled={pageMode === EPageMode.Update}></Input>
                      </Form.Item>
                    </Col>
                  </Row>
                  <Row>
                    <Col span={24}>
                      <Form.Item
                        {...(smallSize ? formSmSizeLayout : formIpChgLayout)}
                        label="目的IP"
                        name={['ipChg', 'rule', 'destIp']}
                        initialValue={ipChgJson.rule?.destIp}
                        validateFirst
                        rules={[
                          { required: true },
                          {
                            validator: async (rule, destIp) => {
                              if (!destIp) {
                                throw new Error('请输入目的IP');
                              }
                              if (!isIpAddress(destIp)) {
                                throw new Error('请输入正确的IPv4或IPv6地址');
                              }
                              const sourceIp = getFieldValue(['ipChg', 'rule', 'sourceIp']);
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
                        <Input disabled={pageMode === EPageMode.Update}></Input>
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

export default IpChg;
