import { BOOL_NO, BOOL_YES } from '@/common/dict';
import { checkTextAreaIp, hostRegex, ipV4Regex } from '@/utils/utils';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Button, Card, Col, Input, InputNumber, Modal, Radio, Row, Spin, Switch } from 'antd';
import { connect } from 'dva';
import React, { Component, useState } from 'react';

const { TextArea } = Input;

// 最小账户锁定时长
const MIN_FORBIDDEN_DURATION_MINUTES = 10;
// 默认的密码有效期天数
const DEFAULT_PASSWORD_MAX_DAYS = 30;
//密码长度
const DEFAULT_PASSWORD_MIN_LENGTH = 6; //默认密码最小长度
const DEFAULT_PASSWORD_MAX_LENGTH = 25; //默认密码最大长度

const PASSWORD_MIN_LENGTH_UPPER_LIMIT = 10; //密码最小长度的上限
const PASSWORD_MAX_LENGTH_UPPER_LIMIT = 30; //密码最大长度的上限
//默认历史密码保存次数
const DEFAULT_PASSWORD_HISTORY_NUM = 3;
const MAX_PASSWORD_HISTORY_NUM = 8;
// 密码复杂度
const passwordComplexityOptions = [
  {
    label: '大小写字母/数字/特殊字符',
    value: 'case_sensitivity_letter_number_char',
  },
  { label: '大小写字母/数字', value: 'case_sensitivity_letter_number' },
  { label: '大小写字母/特殊字符', value: 'case_sensitivity_letter_char' },
];

const PasswordLengthInput = ({ value = {}, onChange }) => {
  const passwordLength = value;
  const [passwordMinLength, setpasswordMinLength] = useState(0);
  const [passwordMaxLength, setpasswordMaxLength] = useState(0);

  const triggerChange = (changedValue) => {
    onChange?.({
      passwordMinLength,
      passwordMaxLength,
      ...passwordLength,
      ...changedValue,
    });
  };

  const onPasswordMinLengthChange = (e) => {
    const newPasswordMinLength = parseInt(e.target.value || '0', 10);
    // console.log(newPasswordMinLength, 'newPasswordMinLength');
    if (Number.isNaN(passwordMinLength)) {
      return;
    }
    if (!('passwordMinLength' in passwordLength)) {
      setpasswordMinLength(newPasswordMinLength);
    }
    triggerChange({ passwordMinLength: newPasswordMinLength });
  };

  const onPasswordMaxLengthChange = (e) => {
    const newPasswordMaxLength = parseInt(e.target.value || '0', 10);
    // console.log(newPasswordMaxLength, 'newPasswordMaxLength');
    if (Number.isNaN(passwordMaxLength)) {
      return;
    }
    if (!('passwordMaxLength' in passwordLength)) {
      setpasswordMaxLength(newPasswordMaxLength);
    }
    triggerChange({ passwordMaxLength: newPasswordMaxLength });
  };
  return (
    <Input.Group compact>
      <Input
        compact="true"
        style={{ width: 100, textAlign: 'center' }}
        placeholder="最小值"
        value={passwordLength?.passwordMinLength || passwordMinLength}
        onChange={onPasswordMinLengthChange}
      />
      <Input
        className="site-input-split"
        style={{
          width: 30,
          borderLeft: 0,
          borderRight: 0,
          pointerEvents: 'none',
        }}
        placeholder="~"
        disabled
      />
      <Input
        compact="true"
        className="site-input-right"
        style={{
          width: 100,
          textAlign: 'center',
        }}
        placeholder="最大值"
        value={passwordLength.passwordMaxLength || passwordMaxLength}
        onChange={onPasswordMaxLengthChange}
      />
    </Input.Group>
  );
};

@Form.create()
@connect(({ securityModel: { settings }, loading }) => ({
  settings,
  queryLoading: loading.effects['securityModel/querySecuritySettings'] || false,
  updateLoading: loading.effects['securityModel/updateSecuritySettings'] || false,
}))
class Security extends Component {
  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'securityModel/querySecuritySettings',
    });
  }

  componentWillUnmount() {}

  checkReferer = (rule, value, callback) => {
    if (!value) {
      callback();
      return;
    }

    const passRefererArr = []; // 已经检查通过
    const valueArr = value.split('\n');

    try {
      if (Array.isArray(valueArr)) {
        valueArr.forEach((referer, index) => {
          const lineText = `第${index + 1}行[${referer}]: `;
          if (!referer) {
            // eslint-disable-next-line no-throw-literal
            throw new Error(`${lineText}不能为空`);
          }

          if (!ipV4Regex.test(referer) && !hostRegex.test(referer)) {
            // eslint-disable-next-line no-throw-literal
            throw new Error(`${lineText}请输入正确的IP地址或域名`);
          }
          // 是否重复了
          if (passRefererArr.indexOf(referer) !== -1) {
            // eslint-disable-next-line no-throw-literal
            throw new Error(`${lineText}已重复`);
          }

          passRefererArr.push(referer);
        });
      }
    } catch (e) {
      callback(e);
    } finally {
      callback();
    }
  };
  checkPassword = (rule, value, callback) => {
    if (
      value.passwordMinLength < DEFAULT_PASSWORD_MIN_LENGTH ||
      value.passwordMinLength > PASSWORD_MIN_LENGTH_UPPER_LIMIT
    ) {
      callback('最小密码请输入范围内的数字');
      return;
    }
    if (
      value.passwordMaxLength < DEFAULT_PASSWORD_MAX_LENGTH ||
      value.passwordMaxLength > PASSWORD_MAX_LENGTH_UPPER_LIMIT
    ) {
      callback('最大密码请输入范围内的数字');
      return;
    }
    callback();
  };

  handleSubmit = (e) => {
    const { dispatch } = this.props;
    e.preventDefault();
    const { form } = this.props;
    form.validateFieldsAndScroll((err, values) => {
      if (err) return;

      Modal.confirm({
        title: '确定保存吗？',
        maskClosable: false,
        destroyOnClose: true,
        keyboard: false,
        autoFocusButton: true,
        onOk: () => {
          const {
            forbiddenDurationMinutes = MIN_FORBIDDEN_DURATION_MINUTES,
            forbiddenMaxFailed,
            permitMultiSession,
            whitelistIpAddress = '',
            whitelistIpAddressState,
            whitelistReferer = '',
            sessionExpiredSecondMinutes = 10,
            passwordMaxDayFlag,
            passwordMaxDay = DEFAULT_PASSWORD_MAX_DAYS,
            passwordLength,
            passwordMinLength = passwordLength.passwordMinLength,
            passwordMaxLength = passwordLength.passwordMaxLength,
            passwordComplexity = '',
            historyPasswordSaveTimes,
          } = values;
          // console.log(passwordMinLength, 'passwordMinLength');
          // console.log(passwordMaxLength, 'passwordMaxLength');
          // console.log(passwordComplexity, 'passwordComplexity');
          // console.log(historyPasswordSaveTimes, 'historyPasswordSaveTimes');
          const submitData = {
            forbiddenMaxFailed,
            forbiddenDurationSecond: forbiddenDurationMinutes * 60,
            permitMultiSession: permitMultiSession ? BOOL_YES : BOOL_NO,
            whitelistIpAddressState: whitelistIpAddressState ? BOOL_YES : BOOL_NO,
            whitelistIpAddress: whitelistIpAddress.split('\n').join(','),
            whitelistReferer: whitelistReferer.split('\n').join(','),

            sessionExpiredSecond: sessionExpiredSecondMinutes * 60,
            passwordMaxDay: passwordMaxDayFlag ? passwordMaxDay : 0,
            passwordMinLength: passwordMinLength,
            passwordMaxLength: passwordMaxLength,
            passwordComplexity: passwordComplexity,
            historyPasswordSaveTimes: historyPasswordSaveTimes
          };
          // console.log(submitData.passwordMinLength, '提交passwordMinLength');
          // console.log(submitData.passwordMaxLength, '提交passwordMaxLength');
          // console.log(submitData.passwordComplexity, '提交passwordComplexity');
          // console.log(submitData.historyPasswordSaveTimes, '提交historyPasswordSaveTimes');
          dispatch({
            type: 'securityModel/updateSecuritySettings',
            payload: {
              ...submitData,
            },
          });
        },
      });
    });
  };

  render() {
    const { form, settings, queryLoading, updateLoading } = this.props;
    const { getFieldDecorator, getFieldValue } = form;
    const formItemLayout = {
      labelCol: { span: 6 },
      wrapperCol: { span: 14 },
    };

    const passwordMaxDayFlag = getFieldValue('passwordMaxDayFlag');

    return (
      <Card bordered={false}>
        <Spin spinning={queryLoading || updateLoading}>
          <Form {...formItemLayout} onSubmit={this.handleSubmit}>
            <Form.Item label="密码有效期" style={{ marginBottom: passwordMaxDayFlag ? 0 : 24 }}>
              <Row>
                <Col span={3}>
                  {getFieldDecorator('passwordMaxDayFlag', {
                    valuePropName: 'checked',
                    initialValue: settings.passwordMaxDay > 0,
                  })(<Switch />)}
                </Col>

                <Col span={12} style={{ display: passwordMaxDayFlag ? 'flex' : 'none' }}>
                  <Form.Item
                    extra={
                      getFieldValue('passwordMaxDay') && (
                        <span>
                          密码有效期为{getFieldValue('passwordMaxDay')}
                          天，超出有效期需要重新修改密码
                        </span>
                      )
                    }
                  >
                    {getFieldDecorator('passwordMaxDay', {
                      initialValue: settings.passwordMaxDay || DEFAULT_PASSWORD_MAX_DAYS,
                      rules: [
                        {
                          required: getFieldDecorator('passwordMaxDayFlag'),
                          message: '请填写密码有效期',
                        },
                      ],
                    })(<InputNumber precision={0} min={1} max={999} />)}
                    <span className="ant-form-text"> 天</span>
                  </Form.Item>
                </Col>
              </Row>
            </Form.Item>

            <Form.Item
              label="密码长度"
              extra={
                <span>
                  <div>
                    最小密码长度的最小值为{DEFAULT_PASSWORD_MIN_LENGTH}，最小密码长度的最大值为
                    {PASSWORD_MIN_LENGTH_UPPER_LIMIT}
                  </div>
                  <div>
                    最大密码长度的最小值为{DEFAULT_PASSWORD_MAX_LENGTH}，最大密码长度的最大值为
                    {PASSWORD_MAX_LENGTH_UPPER_LIMIT}
                  </div>
                </span>
              }
            >
              {getFieldDecorator('passwordLength', {
                initialValue: {
                  passwordMinLength: settings.passwordMinLength || DEFAULT_PASSWORD_MIN_LENGTH,
                  passwordMaxLength: settings.passwordMaxLength || DEFAULT_PASSWORD_MAX_LENGTH,
                },
                rules: [{ required: true }, { validator: this.checkPassword }],
              })(<PasswordLengthInput />)}
            </Form.Item>

            <Form.Item label="密码复杂度">
              {getFieldDecorator('passwordComplexity', {
                initialValue: settings.passwordComplexity ||'case_sensitivity_letter_number_char',
                rules: [{ required: true, message: '请选择密码复杂度' }],
              })(<Radio.Group options={passwordComplexityOptions} />)}
            </Form.Item>

            <Form.Item
              label="历史密码保存次数"
              extra={
                getFieldValue('historyPasswordSaveTimes') && (
                  <span>
                    历史密码保存次数为{getFieldValue('historyPasswordSaveTimes')}，保存次数范围为
                    {DEFAULT_PASSWORD_HISTORY_NUM}-{MAX_PASSWORD_HISTORY_NUM}
                  </span>
                )
              }
            >
              {getFieldDecorator('historyPasswordSaveTimes', {
                initialValue: settings.historyPasswordSaveTimes || DEFAULT_PASSWORD_HISTORY_NUM,
                rules: [{ required: true, message: '请填写历史密码保存次数' }],
              })(<InputNumber precision={0} min={1} max={999} />)}
            </Form.Item>

            <Form.Item
              label="登录超时时间"
              extra={
                getFieldValue('sessionExpiredSecondMinutes') && (
                  <span>
                    用户登录后，页面无操作{getFieldValue('sessionExpiredSecondMinutes')}
                    分钟后将自动退出登录
                  </span>
                )
              }
            >
              {getFieldDecorator('sessionExpiredSecondMinutes', {
                initialValue: settings.sessionExpiredSecond
                  ? settings.sessionExpiredSecond / 60
                  : undefined,
                rules: [
                  {
                    required: true,
                    message: '请填写登录超时时间',
                  },
                ],
              })(<InputNumber precision={0} min={1} max={1440} />)}
              <span className="ant-form-text"> 分钟</span>
            </Form.Item>

            <Form.Item
              label="密码错误锁定阈值数"
              extra={
                getFieldValue('forbiddenMaxFailed') && (
                  <span>连续登录{getFieldValue('forbiddenMaxFailed')}次失败后，将锁定用户</span>
                )
              }
            >
              {getFieldDecorator('forbiddenMaxFailed', {
                initialValue: settings.forbiddenMaxFailed,
                rules: [
                  {
                    required: true,
                    message: '请填写密码错误锁定阈值数',
                  },
                ],
              })(<InputNumber precision={0} min={3} max={10} />)}
              <span className="ant-form-text"> 次</span>
            </Form.Item>
            <Form.Item
              label="账户锁定时长"
              extra={
                getFieldValue('forbiddenDurationMinutes') && (
                  <span>
                    用户被锁定后，
                    {getFieldValue('forbiddenDurationMinutes')}
                    分钟内将无法登录
                  </span>
                )
              }
            >
              {getFieldDecorator('forbiddenDurationMinutes', {
                initialValue:
                  (settings.forbiddenDurationSecond && settings.forbiddenDurationSecond / 60) ||
                  MIN_FORBIDDEN_DURATION_MINUTES,
                rules: [
                  {
                    required: true,
                    message: '请填写账户锁定时长',
                  },
                ],
              })(<InputNumber precision={0} min={MIN_FORBIDDEN_DURATION_MINUTES} max={60} />)}
              <span className="ant-form-text"> 分钟</span>
            </Form.Item>
            <Form.Item
              label="允许同一账号多人同时登录"
              extra="关闭后，同一时刻同一个账户只允许1人在线"
            >
              {getFieldDecorator('permitMultiSession', {
                valuePropName: 'checked',
                initialValue: settings.permitMultiSession === BOOL_YES,
                rules: [
                  {
                    required: false,
                    message: '请选择是否允许同一账号多人同时登录',
                  },
                ],
              })(<Switch />)}
            </Form.Item>
            <Form.Item label="是否限制可登录IP段" extra="开启后，不在登录IP段内的IP主机将无法登录">
              {getFieldDecorator('whitelistIpAddressState', {
                valuePropName: 'checked',
                initialValue: settings.whitelistIpAddressState === BOOL_YES,
                rules: [
                  {
                    required: false,
                    message: '请选择是否限制可登录IP段',
                  },
                ],
              })(<Switch />)}
            </Form.Item>
            <Form.Item
              label="可登录IP段"
              extra={
                <ul style={{ paddingLeft: 20, listStyle: 'decimal' }}>
                  <li>每行请输入A.B.C.D-E.F.G.H格式的IP组，请确保 E.F.G.H &gt;= A.B.C.D</li>
                  <li>或者输入A.B.C.D格式的IP地址</li>
                  <li>最多可输入5000个字符</li>
                  <li>不填写时，将不限制登录IP段</li>
                </ul>
              }
            >
              {getFieldDecorator('whitelistIpAddress', {
                initialValue:
                  settings.whitelistIpAddress && settings.whitelistIpAddress.replace(/,/g, '\n'),
                validateFirst: true,
                validateTrigger: ['onChange', 'onBlur'],
                rules: [
                  {
                    required: false,
                    whitespace: true,
                    message: '请填写IP/IP段',
                  },
                  {
                    validator: checkTextAreaIp,
                  },
                  {
                    max: 5000,
                    message: '最多可输入5000个字符',
                  },
                ],
              })(<TextArea rows={5} />)}
            </Form.Item>
            <Form.Item
              label="Referer白名单"
              extra={
                <ul style={{ paddingLeft: 20, listStyle: 'decimal' }}>
                  <li>每行请输入一个Referer</li>
                  <li>每个Referer允许输入IP地址或域名</li>
                  <li>最多可输入5000个字符</li>
                </ul>
              }
            >
              {getFieldDecorator('whitelistReferer', {
                initialValue:
                  settings.whitelistReferer && settings.whitelistReferer.replace(/,/g, '\n'),
                validateFirst: true,
                validateTrigger: ['onChange', 'onBlur'],
                rules: [
                  {
                    required: false,
                    whitespace: true,
                    message: '请填写Referer白名单',
                  },
                  {
                    validator: this.checkReferer,
                  },
                  {
                    max: 5000,
                    message: '最多可输入5000个字符',
                  },
                ],
              })(<TextArea rows={5} />)}
            </Form.Item>
            <Form.Item wrapperCol={{ span: 12, offset: 6 }}>
              <Button type="primary" htmlType="submit" style={{ marginRight: 10 }}>
                保存
              </Button>
            </Form.Item>
          </Form>
        </Spin>
      </Card>
    );
  }
}

export default Security;
