import { parseObjJson } from '@/utils/utils';
import { ArrowRightOutlined, MinusCircleOutlined, PlusOutlined } from '@ant-design/icons';
import type { FormInstance } from 'antd';
import { Button, Checkbox, InputNumber, Space } from 'antd';
import { Card, Col, Form, Radio, Row } from 'antd';
import React, { useEffect, useState } from 'react';
import type { IVlanProcess } from '../../typings';
import { EVlanProcessType } from '../../typings';
import { EVlanProcesslMode } from '../../typings';

/** 隧道封装模式 */
const VLANN_PROCESS_MODE_LIST = [
  { value: EVlanProcesslMode.IGNORE, label: '不处理' },
  { value: EVlanProcesslMode.NEWVLAN, label: '新增VLAN' },
  { value: EVlanProcesslMode.CHGCLANID, label: '更改VLANID' },
];

export const formTailLayout = {
  labelCol: { span: 3 },
  wrapperCol: { span: 19, offset: 3 },
};

export const formLayout = {
  labelCol: { span: 3 },
  wrapperCol: { span: 19 },
};

const formVlanProcessLayout = {
  labelCol: { span: 3 },
  wrapperCol: { span: 15 },
};

const formChgSmSizeLayout = {
  labelCol: { span: 5 },
  wrapperCol: { span: 10 },
};

export const initVlanProcess = {
  mode: EVlanProcesslMode.IGNORE,
  rule: {},
} as IVlanProcess;

/** 格式化VLAN封装对象 */
export const parseVlanProcessJson = (vlanProcessString: string) => {
  let vlanProcessObj = initVlanProcess;
  if (vlanProcessString) {
    vlanProcessObj = parseObjJson(vlanProcessString) as IVlanProcess;
    if (!vlanProcessObj.rule) {
      vlanProcessObj.rule = {} as any;
    }
  }

  return vlanProcessObj;
};

interface IVlanProcessProps {
  value?: string;
  init?: string;
  onChange?: (value: IVlanProcess) => void;
  form: FormInstance;
  smallSize?: boolean;
}
const VlanProcess: React.FC<IVlanProcessProps> = ({
  init,
  value,
  onChange,
  form,
  smallSize = false,
}) => {
  const [vlanProcessJson] = useState<IVlanProcess>(() => {
    return parseVlanProcessJson(init || '');
  });
  const [vlanIdAlterationCnt, setVlanIdAlterationCnt] = useState<number>(0);

  useEffect(() => {
    if (onChange) {
      onChange(vlanProcessJson);
    }
  }, [vlanProcessJson]);

  return (
    <>
      {/* Vlan处理 */}
      <Form.Item
        name={['vlanProcess', 'mode']}
        label="VLAN处理"
        {...formLayout}
        initialValue={vlanProcessJson.mode || EVlanProcesslMode.IGNORE}
        extra={
          form.getFieldValue(['vlanProcess', 'mode']) === EVlanProcesslMode.CHGCLANID
            ? '新增VALN会在重放时对所有包统一处理，更改VLANID功能可以设置特定VLANID修改为新设置的VLANID'
            : ''
        }
      >
        <Radio.Group>
          {VLANN_PROCESS_MODE_LIST.map((mode) => (
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
          const vlanProcessMode = getFieldValue(['vlanProcess', 'mode']);
          if (vlanProcessMode === EVlanProcesslMode.NEWVLAN) {
            return (
              <Form.Item key={EVlanProcesslMode.NEWVLAN} {...formTailLayout}>
                <Card bordered bodyStyle={{ padding: '10px 0' }}>
                  <Row>
                    <Col span={24}>
                      <Form.Item
                        {...(smallSize ? formChgSmSizeLayout : formVlanProcessLayout)}
                        name={['vlanProcess', 'rule', 'vlanId']}
                        initialValue={vlanProcessJson.rule?.vlanId}
                        label="VLANID"
                        validateFirst
                        rules={[
                          { required: true },
                          {
                            validator: async (rule, vlanId) => {
                              if (!vlanId) {
                                throw new Error('请输入VLANID');
                              }
                              if (vlanId > 4094 || vlanId < 0) {
                                throw new Error('VLANID范围应在[0,4094]');
                              }
                            },
                          },
                        ]}
                      >
                        <InputNumber style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                  </Row>
                  <Row>
                    <Col span={24}>
                      <Form.Item
                        {...(smallSize
                          ? {
                              labelCol: { span: 9 },
                              wrapperCol: { span: 14 },
                            }
                          : {
                              labelCol: { span: 3 },
                              wrapperCol: { span: 14 },
                            })}
                        name={['vlanProcess', 'rule', 'processType']}
                        initialValue={vlanProcessJson.rule?.processType || EVlanProcessType.NEWVLAN}
                        label="已有VLAN头的包处理:"
                        validateFirst
                        extra={
                          <section>
                            <ul style={{ listStyle: 'decimal', paddingLeft: 20 }}>
                              <li>设置VLANID会给所有无VLAN的包增加一个VLAN头</li>
                              <li>
                                已有VLAN头可选择处理：新增VLAN，原有VALN外嵌套一个。替换VLANID，直接替换原有VLANID
                              </li>
                            </ul>
                          </section>
                        }
                      >
                        <Radio.Group
                          defaultValue={
                            vlanProcessJson.rule?.processType || EVlanProcessType.NEWVLAN
                          }
                        >
                          <Radio value={EVlanProcessType.NEWVLAN} key={EVlanProcessType.NEWVLAN}>
                            新增VLAN
                          </Radio>
                          <Radio
                            value={EVlanProcessType.CHGCLANID}
                            key={EVlanProcessType.CHGCLANID}
                          >
                            替换VLANID
                          </Radio>
                        </Radio.Group>
                      </Form.Item>
                    </Col>
                  </Row>
                </Card>
              </Form.Item>
            );
          }
          if (vlanProcessMode === EVlanProcesslMode.CHGCLANID) {
            return (
              <Form.Item key={EVlanProcesslMode.CHGCLANID} {...formTailLayout}>
                <Card bordered bodyStyle={{ padding: '10px 0' }}>
                  <Row>
                    <Col offset={3} span={24}>
                      <Form.Item
                        {...formVlanProcessLayout}
                        name={['vlanProcess', 'rule', 'vlanIdAlteration']}
                        initialValue={vlanProcessJson.rule?.vlanIdAlteration}
                        validateFirst
                      >
                        {smallSize ? (
                          <Form.List name={['vlanProcess', 'rule', 'vlanIdAlteration']}>
                            {(fields, { add, remove }) => {
                              setVlanIdAlterationCnt(fields.length);
                              return (
                                <>
                                  {fields.map(({ key, name, ...restField }) => (
                                    <Space
                                      key={key}
                                      style={{ display: 'flex', marginBottom: 10 }}
                                      align="baseline"
                                    >
                                      <Form.Item
                                        {...restField}
                                        name={[name, 'source']}
                                        rules={[
                                          { required: true },
                                          {
                                            validator: async (rule, vlanId) => {
                                              if (!vlanId) {
                                                throw new Error('请输入VLANID');
                                              }
                                              if (vlanId > 4094 || vlanId < 0) {
                                                throw new Error('VLANID范围应在[0,4094]');
                                              }
                                            },
                                          },
                                        ]}
                                      >
                                        <InputNumber placeholder='更改前VLANID' style={{ width: '100%' }} />
                                      </Form.Item>
                                      <ArrowRightOutlined
                                        style={{ lineHeight: '32px', fontSize: '20px' }}
                                      />
                                      <Form.Item
                                        {...restField}
                                        name={[name, 'target']}
                                        rules={[
                                          { required: true },
                                          {
                                            validator: async (rule, vlanId) => {
                                              if (!vlanId) {
                                                throw new Error('请输入VLANID');
                                              }
                                              if (vlanId > 4094 || vlanId < 0) {
                                                throw new Error('VLANID范围应在[0,4094]');
                                              }
                                            },
                                          },
                                        ]}
                                      >
                                        <InputNumber placeholder='更改后VLANID' style={{ width: '100%' }} />
                                      </Form.Item>
                                      <MinusCircleOutlined
                                        onClick={() => {
                                          remove(name);
                                        }}
                                      />
                                    </Space>
                                  ))}
                                  <Form.Item>
                                    <Button
                                      type="dashed"
                                      disabled={fields.length >= 10}
                                      onClick={() => {
                                        add();
                                      }}
                                      block
                                      icon={<PlusOutlined />}
                                    >
                                      添加
                                    </Button>
                                  </Form.Item>
                                </>
                              );
                            }}
                          </Form.List>
                        ) : (
                          <Form.List name={['vlanProcess', 'rule', 'vlanIdAlteration']}>
                            {(fields, { add, remove }) => {
                              setVlanIdAlterationCnt(fields.length);
                              return (
                                <>
                                  {fields.map(({ key, name, ...restField }) => (
                                    <Space
                                      key={key}
                                      style={{ display: 'flex', marginBottom: 10 }}
                                      align="baseline"
                                    >
                                      <Form.Item
                                        {...restField}
                                        name={[name, 'source']}
                                        label="更改前VLANID:"
                                        rules={[
                                          { required: true },
                                          {
                                            validator: async (rule, vlanId) => {
                                              if (!vlanId) {
                                                throw new Error('请输入VLANID');
                                              }
                                              if (vlanId > 4094 || vlanId < 0) {
                                                throw new Error('VLANID范围应在[0,4094]');
                                              }
                                            },
                                          },
                                        ]}
                                      >
                                        <InputNumber style={{ width: '100%' }} />
                                      </Form.Item>
                                      <ArrowRightOutlined
                                        style={{ lineHeight: '32px', fontSize: '20px' }}
                                      />
                                      <Form.Item
                                        {...restField}
                                        name={[name, 'target']}
                                        label="更改后VLANID:"
                                        rules={[
                                          { required: true },
                                          {
                                            validator: async (rule, vlanId) => {
                                              if (!vlanId) {
                                                throw new Error('请输入VLANID');
                                              }
                                              if (vlanId > 4094 || vlanId < 0) {
                                                throw new Error('VLANID范围应在[0,4094]');
                                              }
                                            },
                                          },
                                        ]}
                                      >
                                        <InputNumber style={{ width: '100%' }} />
                                      </Form.Item>
                                      <MinusCircleOutlined
                                        onClick={() => {
                                          remove(name);
                                        }}
                                      />
                                    </Space>
                                  ))}
                                  <Form.Item>
                                    <Button
                                      type="dashed"
                                      disabled={fields.length >= 10}
                                      onClick={() => {
                                        add();
                                      }}
                                      block
                                      icon={<PlusOutlined />}
                                    >
                                      添加
                                    </Button>
                                  </Form.Item>
                                </>
                              );
                            }}
                          </Form.List>
                        )}
                      </Form.Item>
                    </Col>
                  </Row>
                  {(() => {
                    if (vlanIdAlterationCnt > 0) {
                      return (
                        <Row>
                          <Col offset={3} span={24}>
                            <Form.Item
                              {...formVlanProcessLayout}
                              name={['vlanProcess', 'rule', 'extraVlanIdRule']}
                              getValueFromEvent={(e) => {
                                return e.target.checked;
                              }}
                              validateFirst
                              valuePropName={'checked'}
                              initialValue={
                                vlanProcessJson.rule?.extraVlanIdRule === '1' ? true : false
                              }
                            >
                              <Checkbox>
                                不命中上述VLANID时,继续回放 (不选: 不命中上述VLAN时直接丢包不回放)
                              </Checkbox>
                            </Form.Item>
                          </Col>
                        </Row>
                      );
                    }
                    return '';
                  })()}
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

export default VlanProcess;
