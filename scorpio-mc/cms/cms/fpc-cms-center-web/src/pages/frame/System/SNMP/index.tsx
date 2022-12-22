import { BOOL_NO, BOOL_YES } from '@/common/dict';
import { QuestionCircleOutlined } from '@ant-design/icons';
import {
  Alert,
  Button,
  Card,
  Col,
  Form,
  Input,
  Modal,
  Radio,
  Row,
  Select,
  Skeleton,
  Switch,
  Tooltip,
} from 'antd';
import { connect } from 'dva';
import React, { Fragment, useEffect } from 'react';
import type { Dispatch } from 'redux';
import styles from './index.less';
import type { ISnmpSettingModelState } from './model';
import type { ISnmpSettings } from './typings';
import { ESnmpAuthAlgorithm, ESnmpEncryptAlgorithm, ESnmpVersion } from './typings';

const FormItem = Form.Item;

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 5 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 14 },
  },
};
const tailFormItemLayout = {
  wrapperCol: {
    xs: {
      span: 24,
      offset: 0,
    },
    sm: {
      offset: 5,
      span: 14,
    },
  },
};

/**
 * 密码占位符
 */
const PASSWORD_PLACEHOLDER = '********';

const msgHelp =
  '必须包含下列4种字符组中的2种：英文大写字母<A-Z>；英文小写字母<a-z>；数字<0-9>；非字母数字字符（例如!,#,%）。';
const TootTipMessage = () => (
  <Tooltip title={`${msgHelp}`}>
    <QuestionCircleOutlined />
  </Tooltip>
);

const oidList = [
  { value: '.1.3.6.1.2.1.1', label: '系统描述' },
  { value: '.1.3.6.1.2.1.2.2.1', label: '网卡' },
  { value: '.1.3.6.1.2.1.31.1.1.1', label: '接口' },
  { value: '.1.3.6.1.4.1.2021.11', label: 'CPU' },
  { value: '.1.3.6.1.4.1.2021.10', label: 'LOAD' },
  { value: '.1.3.6.1.4.1.2021.4', label: '内存' },
  { value: '.1.3.6.1.2.1.25.2.3', label: '存储' },
];

const versionList = [ESnmpVersion.v1, ESnmpVersion.v2c, ESnmpVersion.v3];

interface ISnmpSettingProps {
  dispatch: Dispatch<any>;
  snmpSettings: ISnmpSettings;
  queryLoading: boolean;
  updateLoading: boolean;
}

const SnmpSetting: React.FC<ISnmpSettingProps> = ({
  dispatch,
  snmpSettings,
  queryLoading,
  updateLoading,
}) => {
  const [form] = Form.useForm();
  useEffect(() => {
    querySystemSnmpSettings();
  }, []);

  useEffect(() => {
    if (form && !queryLoading) {
      form.resetFields();
    }
  }, [snmpSettings, form, queryLoading]);

  const querySystemSnmpSettings = () => {
    if (dispatch) {
      dispatch({
        type: 'snmpSettingModel/querySystemSnmpSettings',
        payload: {},
      });
    }
  };

  const handleUpdate = (values: any) => {
    Modal.confirm({
      title: '确定保存吗？',
      maskClosable: false,
      onOk: () => {
        const { state } = values;
        let formData = {} as ISnmpSettings;
        const stateText = state ? BOOL_YES : BOOL_NO;
        if (state === BOOL_NO) {
          formData.state = stateText;
        } else {
          formData = { ...values, state: stateText };
        }

        (
          dispatch({
            type: 'snmpSettingModel/updateSystemSnmpSettings',
            payload: formData,
          } as unknown) as Promise<any>
        ).then(() => {
          window.setTimeout(() => {
            // form.resetFields();
            querySystemSnmpSettings();
          }, 1000);
        });
      },
    });
  };

  const quotationCheck = async (rule: any, value: string) => {
    if (value.match(/'|"/)) {
      return Promise.reject('输入内容不可包含单引号、双引号');
    }
    return Promise.resolve();
  };

  const fieldCheck = async (rule: any, value: string) => {
    // 如果是关闭状态，直接通过校验
    const state = form.getFieldValue('state');
    if (!state) {
      return Promise.resolve();
    }

    const { field } = rule;
    const minLength = field === 'roCommunity' ? 6 : 8;
    // 密码是8位
    if (value.length < minLength) {
      return Promise.reject(`最小允许输入字符数是${minLength}`);
    }
    // fix: 排除掉特殊字符，防止在sh脚本里面自动执行
    if (value.match(/[\s+`$():{}&]/)) {
      return Promise.reject(msgHelp);
    }

    // 判断组合情况
    let len = 0;
    if (value.match(/([a-z])+/)) {
      len += 1;
    }
    if (value.match(/([0-9])+/)) {
      len += 1;
    }
    if (value.match(/([A-Z])+/)) {
      len += 1;
    }
    if (value.match(/[^a-zA-Z0-9]+/)) {
      len += 1;
    }

    if (len < 2) {
      return Promise.reject(msgHelp);
    }

    return Promise.resolve();
  };

  const stateValue = snmpSettings.state === BOOL_YES;
  form.setFieldsValue({
    state: stateValue,
  });

  return (
    <Card bordered={false}>
      <Skeleton active loading={queryLoading}>
        <Form
          form={form}
          {...formItemLayout}
          scrollToFirstError
          onFinish={handleUpdate}
          initialValues={{
            ...snmpSettings,
            state: stateValue,
            // eslint-disable-next-line no-nested-ternary
            authPassword: snmpSettings.authPassword
              ? snmpSettings.authPassword === PASSWORD_PLACEHOLDER
                ? undefined
                : snmpSettings.authPassword
              : undefined,
            // eslint-disable-next-line no-nested-ternary
            encryptPassword: snmpSettings.encryptPassword
              ? snmpSettings.encryptPassword === PASSWORD_PLACEHOLDER
                ? undefined
                : snmpSettings.encryptPassword
              : undefined,
          }}
        >
          <Form.Item label="SNMP" name="state" valuePropName="checked" style={{ marginBottom: 10 }}>
            <Switch checkedChildren="开启" unCheckedChildren="关闭" />
          </Form.Item>

          <Form.Item noStyle shouldUpdate={true}>
            {({ getFieldValue }) => {
              const inputDisabled = !getFieldValue('state');
              return (
                <Fragment>
                  <Form.Item
                    shouldUpdate={true}
                    label="版本"
                    name="version"
                    extra="SNMP协议的v3版本比v1和v2c更安全，建议选择v3"
                    rules={[{ required: !inputDisabled, message: '请选择SNMP的版本' }]}
                  >
                    <Radio.Group disabled={inputDisabled}>
                      {versionList.map((item) => (
                        <Radio key={item} value={item}>
                          {item}
                        </Radio>
                      ))}
                    </Radio.Group>
                  </Form.Item>
                  {getFieldValue('version') === ESnmpVersion.v3 ? (
                    <Fragment>
                      <Form.Item
                        label="安全用户名"
                        name="username"
                        normalize={(value) => (value ? value.trim() : value)}
                        validateFirst
                        rules={[
                          { required: !inputDisabled, message: '请填写安全用户名' },
                          {
                            validator: fieldCheck,
                          },
                          {
                            validator: quotationCheck,
                          },
                        ]}
                      >
                        <Input maxLength={32} disabled={inputDisabled} />
                      </Form.Item>
                      <Form.Item
                        label="认证算法"
                        required={!inputDisabled}
                        style={{ marginBottom: 0 }}
                      >
                        <Row gutter={10}>
                          <Col span={8}>
                            <Form.Item
                              name="authAlgorithm"
                              rules={[{ required: !inputDisabled, message: '请选择认证算法' }]}
                            >
                              <Select disabled={inputDisabled}>
                                {[ESnmpAuthAlgorithm.MD5, ESnmpAuthAlgorithm.SHA].map((item) => (
                                  <Select.Option key={item} value={item}>
                                    {item}
                                  </Select.Option>
                                ))}
                              </Select>
                            </Form.Item>
                          </Col>
                          <Col span={16}>
                            <Form.Item
                              label={
                                <span>
                                  认证密码&nbsp;
                                  <TootTipMessage />
                                </span>
                              }
                              labelCol={{ span: 10 }}
                              wrapperCol={{ span: 14 }}
                              name="authPassword"
                              normalize={(value) => (value ? value.trim() : value)}
                              validateFirst
                              rules={[
                                { required: !inputDisabled, message: '请填写认证密码' },
                                {
                                  validator: fieldCheck,
                                },
                                {
                                  validator: quotationCheck,
                                },
                              ]}
                            >
                              <Input.Password
                                autoComplete="new-password"
                                maxLength={64}
                                disabled={inputDisabled}
                              />
                            </Form.Item>
                          </Col>
                        </Row>
                      </Form.Item>

                      <Form.Item
                        label="加密算法"
                        required={!inputDisabled}
                        style={{ marginBottom: 0 }}
                      >
                        <Row gutter={10}>
                          <Col span={8}>
                            <Form.Item
                              name="encryptAlgorithm"
                              rules={[{ required: !inputDisabled, message: '请选择加密算法' }]}
                            >
                              <Select disabled={inputDisabled}>
                                {[ESnmpEncryptAlgorithm.AES, ESnmpEncryptAlgorithm.DES].map(
                                  (item) => (
                                    <Select.Option key={item} value={item}>
                                      {item}
                                    </Select.Option>
                                  ),
                                )}
                              </Select>
                            </Form.Item>
                          </Col>
                          <Col span={16}>
                            <Form.Item
                              label={
                                <span>
                                  加密密码&nbsp;
                                  <TootTipMessage />
                                </span>
                              }
                              name="encryptPassword"
                              labelCol={{ span: 10 }}
                              wrapperCol={{ span: 14 }}
                              normalize={(value) => (value ? value.trim() : value)}
                              validateFirst
                              rules={[
                                { required: !inputDisabled, message: '请填写加密密码' },
                                {
                                  validator: fieldCheck,
                                },
                                {
                                  validator: quotationCheck,
                                },
                              ]}
                            >
                              <Input.Password
                                autoComplete="new-password"
                                maxLength={64}
                                disabled={inputDisabled}
                              />
                            </Form.Item>
                          </Col>
                        </Row>
                      </Form.Item>
                    </Fragment>
                  ) : (
                    <Form.Item
                      label={
                        <span>
                          SNMP只读团体名&nbsp;
                          <TootTipMessage />
                        </span>
                      }
                      name="roCommunity"
                      normalize={(value) => (value ? value.trim() : value)}
                      validateFirst
                      rules={[
                        { required: !inputDisabled, message: '请填写只读团体名' },
                        {
                          validator: fieldCheck,
                        },
                        {
                          validator: quotationCheck,
                        },
                      ]}
                    >
                      <Input maxLength={32} disabled={inputDisabled} />
                    </Form.Item>
                  )}
                  <Form.Item
                    label="设备位置"
                    name="sysLocation"
                    normalize={(value) => (value ? value.trim() : value)}
                    rules={[
                      { required: !inputDisabled, message: '请填写设备位置' },
                      {
                        validator: quotationCheck,
                      },
                    ]}
                  >
                    <Input maxLength={255} disabled={inputDisabled} />
                  </Form.Item>
                  <Form.Item
                    label="联系信息"
                    name="sysContact"
                    normalize={(value) => (value ? value.trim() : value)}
                    rules={[
                      { required: !inputDisabled, message: '请填写联系信息' },
                      {
                        validator: quotationCheck,
                      },
                    ]}
                  >
                    <Input maxLength={255} disabled={inputDisabled} />
                  </Form.Item>
                </Fragment>
              );
            }}
          </Form.Item>
          <FormItem {...tailFormItemLayout}>
            <Alert
              message="OID支持"
              description={
                <div className={styles.oidWrap}>
                  <ul>
                    {oidList.map((item) => (
                      <li key={item.value}>
                        <span>{item.value}</span>
                        <span>{item.label}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              }
              type="info"
              showIcon
            />
          </FormItem>
          <FormItem {...tailFormItemLayout}>
            <Button loading={updateLoading} type="primary" htmlType="submit">
              保存
            </Button>
            <Button
              loading={queryLoading}
              onClick={querySystemSnmpSettings}
              style={{ marginLeft: 10 }}
            >
              刷新
            </Button>
          </FormItem>
        </Form>
      </Skeleton>
    </Card>
  );
};

export default connect(
  ({
    snmpSettingModel: { snmpSettings },
    loading,
  }: {
    snmpSettingModel: ISnmpSettingModelState;
    loading: { effects: Record<string, boolean> };
  }) => ({
    snmpSettings,
    queryLoading: loading.effects['snmpSettingModel/querySystemSnmpSettings'] || false,
    updateLoading: loading.effects['snmpSettingModel/updateSystemSnmpSettings'] || false,
  }),
)(SnmpSetting);
