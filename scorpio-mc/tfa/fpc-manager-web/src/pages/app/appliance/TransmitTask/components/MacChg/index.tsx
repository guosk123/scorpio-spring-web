import { macAddressRegex, parseObjJson } from '@/utils/utils';
import { FormInstance } from 'antd';
import { Card, Col, Form, Input, Radio, Row } from 'antd';
import React, { useEffect, useState } from 'react';
import { IMacChg } from '../../typings';
import { EMacChgMode } from '../../typings';

/** mac修改模式 */
const MAC_CHG_MODE_LIST = [
  { value: EMacChgMode.NOT_CHANGE, label: '不修改' },
  { value: EMacChgMode.CHANGE, label: '修改' },
];

export const formTailLayout = {
  labelCol: { span: 3 },
  wrapperCol: { span: 19, offset: 3 },
};

const formMacChgLayout = {
  labelCol: { span: 3 },
  wrapperCol: { span: 10 },
};

const formSmSizeLayout = {
  labelCol: { span: 5 },
  wrapperCol: { span: 10 },
};

export const initMacChg = {
  mode: EMacChgMode.NOT_CHANGE,
  rule: {},
} as IMacChg;

/** 格式化VLAN封装对象 */
export const parseMacChgJson = (macChgString: string) => {
  let macChgObj = initMacChg;
  if (macChgString) {
    macChgObj = parseObjJson(macChgString) as IMacChg;
    if (!macChgObj.rule) {
      macChgObj.rule = {} as any;
    }
  }

  return macChgObj;
};

interface IMacChgProps {
  value?: string;
  init?: string;
  onChange?: (value: IMacChg) => void;
  form: FormInstance;
  smallSize?: boolean;
}
const MacChg: React.FC<IMacChgProps> = ({ init, value, onChange, form, smallSize = false }) => {
  const [macChgJson] = useState<IMacChg>(() => {
    return parseMacChgJson(init || '');
  });
  const [macChgMode, setMacchgMode] = useState<EMacChgMode>(EMacChgMode.NOT_CHANGE);
  useEffect(() => {
    if (onChange) {
      onChange(macChgJson);
    }
    setMacchgMode(macChgJson.mode || EMacChgMode.NOT_CHANGE);
    form.setFieldsValue({
      macChg: {
        ...form.getFieldValue('macChg'),
        mode: macChgJson.mode || EMacChgMode.NOT_CHANGE,
      },
    });
  }, [macChgJson]);

  return (
    <>
      <Form.Item label="MAC修改">
        <Radio.Group
          value={macChgMode}
          onChange={(e) => {
            setMacchgMode(e.target.value);
            form.setFieldsValue({
              macChg: {
                ...form.getFieldValue('macChg'),
                mode: e.target.value,
              },
            });
          }}
        >
          {MAC_CHG_MODE_LIST.map((mode) => (
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
          const macChgMode = getFieldValue(['macChg', 'mode']);
          if (macChgMode === EMacChgMode.CHANGE) {
            return (
              <Form.Item key={EMacChgMode.CHANGE} {...formTailLayout}>
                <Card bordered bodyStyle={{ padding: '10px 0' }}>
                  <Row>
                    <Col span={24}>
                      <Form.Item
                        {...(smallSize ? formSmSizeLayout : formMacChgLayout)}
                        label="源MAC"
                        name={['macChg', 'rule', 'sourceMac']}
                        initialValue={macChgJson.rule?.sourceMac}
                        validateFirst
                        rules={[
                          {
                            required: true,
                            message: '请输入源MAC',
                          },
                          { pattern: macAddressRegex, message: '请输入正确的MAC地址' },
                        ]}
                      >
                        <Input></Input>
                      </Form.Item>
                    </Col>
                  </Row>
                  <Row>
                    <Col span={24}>
                      <Form.Item
                        {...(smallSize ? formSmSizeLayout : formMacChgLayout)}
                        label="目的MAC"
                        name={['macChg', 'rule', 'destMac']}
                        initialValue={macChgJson.rule?.destMac}
                        validateFirst
                        rules={[
                          {
                            required: true,
                            message: '请输入目的MAC',
                          },
                          { pattern: macAddressRegex, message: '请输入正确的MAC地址' },
                        ]}
                      >
                        <Input></Input>
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

export default MacChg;
