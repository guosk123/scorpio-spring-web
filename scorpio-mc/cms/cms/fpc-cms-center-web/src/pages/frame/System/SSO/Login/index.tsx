import config from '@/common/applicationConfig';
import { appKeyAndTokenRegex, passwordRegex, randomSecret, userFullNameRegex } from '@/utils/utils';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import type { FormComponentProps } from '@ant-design/compatible/es/form';
import { Alert, Button, Col, Input, Result, Row, Tabs, Tooltip } from 'antd';
import { connect } from 'dva';
import { Base64 } from 'js-base64';
import { stringify } from 'qs';
import React, { Fragment, useEffect, useState } from 'react';
import type { Dispatch } from 'redux';

const { CONTEXT_PATH } = config;

const { TabPane } = Tabs;
const FormItem = Form.Item;

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 4 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 16 },
  },
};
const tailFormItemLayout = {
  wrapperCol: {
    xs: {
      span: 24,
      offset: 0,
    },
    sm: {
      span: 16,
      offset: 4,
    },
  },
};

interface IPayload {
  platform_user_id: string;
  platform_id: string;
}

enum ETabKey {
  'Bind' = 'bind',
  'Register' = 'register',
}

interface ICondition {
  startTime?: string;
  endTime?: string;
  ipInitiator?: string;
  portInitiator?: string | number;
  ipResponder?: string;
  portResponder?: string | number;
  ipProtocol?: 'tcp' | 'udp' | 'icmp';
}

interface ISsoOauthProps extends FormComponentProps {
  dispatch: Dispatch<any>;
  location: {
    query: {
      /**
       * JWT
       */
      jwt: string;
      /**
       * 跳转页面
       */
      redirect_uri?: string;
      /**
       * 搜索条件
       */
      condition?: string;
      token: string;
    };
  };
  ssoUserBindLoading: boolean;
  ssoUserRegisterLoading: boolean;
}

const JwtErrorResult = <Result status="error" title="参数错误" subTitle="请检查跳转链接" />;

const SsoOauth = ({
  location,
  dispatch,
  form,
  ssoUserBindLoading,
  ssoUserRegisterLoading,
}: ISsoOauthProps) => {
  // 密码确认校验
  const [pwdConfirmDirty, setPwdConfirmDirty] = useState<boolean>(false);
  // 签名验证结果
  const [verifySignaResult, setVerifySignaResult] = useState<200 | 401 | 403 | 404 | 500>();
  // 用户绑定和用户注册Tab
  const [tabKey, setTabKey] = useState<ETabKey>(ETabKey.Bind);
  // 验证码
  const [verifyCodeUrl, setVerifyCodeUrl] = useState<string>('');
  const {
    jwt,
    redirect_uri: redirectUri = '/analysis/situation/network',
    condition,
    token,
  } = location.query;

  // 获取验证码图片地址
  const getVerifyCodeUrl = () => {
    // 根据环境来判断 登录接口地址
    let url = `${CONTEXT_PATH}/verify-code`;
    if (process.env.NODE_ENV === 'development') {
      url = `/api${url}`;
    }
    setVerifyCodeUrl(`${url}?t=${new Date().getTime()}`);
  };

  useEffect(() => {
    getVerifyCodeUrl();
  }, []);

  // eslint-disable-next-line consistent-return
  useEffect(() => {
    if (verifySignaResult === 200) {
      const id = setTimeout(() => {
        const { origin, pathname } = window.location;
        if (dispatch) {
          dispatch({
            type: 'globalModel/queryCurrentUser',
          });
        }
        // 解析跳转参数
        let conditionJson = {} as ICondition;
        if (condition) {
          try {
            // 先解码
            const decodeCondition = decodeURIComponent(condition);
            conditionJson = JSON.parse(decodeCondition);
          } catch (error) {
            // @ts-ignore
            console.error('条件解析失败');
            conditionJson = {} as ICondition;
          }
        }

        // 跳转至目标页面
        window.location.href = `${origin}${pathname}#${redirectUri}?${stringify(conditionJson)}`;
      }, 1000);
      return () => clearTimeout(id);
    }
    return;
  }, [verifySignaResult]);

  // 解析JWT
  // 如果Token不存在
  // 替换jwt,否则通过HTTP传输，+会变成空格
  const fixJwt = jwt ? jwt.replace(/ /g, '+') : '';
  const jwtArr = fixJwt.split('.');
  if (!jwt || jwtArr.length !== 3) {
    return JwtErrorResult;
  }

  // head.payload.signature
  // @see: https://www.cnblogs.com/ningj3/archive/2009/03/11/1409000.html
  const payloadBase64 = jwtArr[1];
  // 补齐4的倍数

  let payload = {} as IPayload;
  try {
    const payloadText = Base64.decode(payloadBase64);
    // 再解析json
    try {
      payload = JSON.parse(payloadText);
    } catch (error) {
      payload = {} as IPayload;
    }
  } catch (error) {
    payload = {} as IPayload;
  }

  const { platform_id: platformId, platform_user_id: platformUserId } = payload;
  // if (!platformId || !platformUserId) {
  //   return JwtErrorResult;
  // }

  const { getFieldDecorator } = form;

  const handleTabChange = (tab: ETabKey) => {
    form.resetFields();
    setTabKey(tab);
  };

  // 更新图片验证码
  const resetVerifyCode = () => {
    getVerifyCodeUrl();
  };

  // 登录名称变化时。重新触发密码的校验
  const validateToPassword = (rule: any, value: string, callback: any) => {
    const password = form.getFieldValue('password');
    if (password) {
      form.validateFields(['password'], { force: true });
    }
    callback();
  };
  // 确认密码
  const compareToFirstPassword = (rule: any, value: string, callback: any) => {
    if (value && value !== form.getFieldValue('password')) {
      callback('两次密码输入不一致!');
    } else {
      callback();
    }
  };

  // 和用户名进行比较
  const validateToUserName = (rule: any, value: string, callback: any) => {
    const userName = form.getFieldValue('name');
    // 没有密码或者是没有登录名称的时候，可以通过
    if (!value || !userName) {
      callback();
      return;
    }
    // 比较是否相同
    if (value === userName) {
      callback('密码不能和登录名称相同');
      return;
    }
    const userNameReverse = userName.split('').reverse().join('');
    if (value === userNameReverse) {
      callback('密码不能是登录名称倒序');
      return;
    }

    callback();
  };

  // 校验密码
  const validateToNextPassword = (rule: any, value: string, callback: any) => {
    // 校验密码强度
    // 1. 必须同时包含大写字母、小写字母和数字，三种组合
    // 2. 长度在6-30之间
    if (value) {
      if (!passwordRegex.test(value)) {
        callback('密码必须同时包含大写字母、小写字母和数字');
      }
      if (value.length < 6 || value.length > 30) {
        callback('密码长度6-30位');
      }
    }

    if (value && pwdConfirmDirty) {
      form.validateFields(['confirm'], { force: true });
    }
    callback();
  };
  // 确认密码输入聚焦
  const handleConfirmBlur = (e: React.FocusEvent<HTMLInputElement>) => {
    const { value } = e.target;
    setPwdConfirmDirty(pwdConfirmDirty || !!value);
  };

  // 用户关联
  const handleUserBind = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    form.validateFieldsAndScroll((err, values) => {
      if (err) {
        return;
      }
      (
        dispatch({
          type: 'ssoLoginModel/ssoUserBind',
          payload: {
            token,
            jwt: fixJwt,
            ...values,
          },
        }) as unknown as Promise<any>
      ).then(({ success }) => {
        if (success) {
          setVerifySignaResult(200);
        } else {
          // 刷新验证码
          resetVerifyCode();
          // 清空原来的验证码
          form.setFieldsValue({
            code: '',
          });
        }
      });
    });
  };
  // 用户注册
  const handleUserRegister = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    form.validateFieldsAndScroll((err, values) => {
      if (err) {
        return;
      }
      // 删除确认密码
      // eslint-disable-next-line no-param-reassign
      delete values.confirm;
      (
        dispatch({
          type: 'ssoLoginModel/ssoUserRegister',
          payload: {
            token,
            jwt: fixJwt,
            ...values,
          },
        }) as unknown as Promise<any>
      ).then(({ success }) => {
        if (success) {
          setVerifySignaResult(200);
        }
      });
    });
  };

  const randomString = (fieldKey: string) => {
    const secret = randomSecret();
    form.setFieldsValue({
      [fieldKey]: secret,
    });
  };

  return (
    <Fragment>
      <Alert
        message={`未找到相关用户[系统ID：${platformId}，用户ID：${platformUserId}]`}
        description={
          <Fragment>
            <div>1. 您可以联系管理员进行用户关联；</div>
            <div>2. 如果已有账户，您可以进行用户关联；</div>
            <div>3. 如果没有账户，您可以进行用户注册。</div>
          </Fragment>
        }
        type="warning"
        showIcon
      />
      <div>
        <Tabs
          activeKey={tabKey}
          animated={false}
          onChange={(key) => {
            const newKey = key as ETabKey;
            handleTabChange(newKey);
          }}
        >
          <TabPane tab="用户关联" key={ETabKey.Bind}>
            {tabKey === ETabKey.Bind && (
              <Form {...formItemLayout} onSubmit={handleUserBind}>
                <FormItem label="登录名">
                  {getFieldDecorator('username', {
                    rules: [{ required: true, whitespace: true, message: '请输入您的登录名' }],
                  })(
                    <Input autoFocus autoComplete="new-password" placeholder="请输入您的登录名" />,
                  )}
                </FormItem>
                <FormItem label="密码">
                  {getFieldDecorator('password', {
                    rules: [{ required: true, message: '请输入您的密码' }],
                  })(
                    <Input.Password
                      type="password"
                      autoComplete="new-password"
                      placeholder="请输入您的密码"
                    />,
                  )}
                </FormItem>
                <FormItem label="验证码">
                  <Row gutter={8}>
                    <Col span={16}>
                      {getFieldDecorator('code', {
                        rules: [{ required: true, whitespace: true, message: '请输入验证码' }],
                      })(<Input size="large" type="text" placeholder="请输入验证码" />)}
                    </Col>
                    <Col span={8}>
                      <Tooltip placement="top" title="点击切换验证码">
                        <img
                          style={{ cursor: 'pointer', float: 'right' }}
                          src={verifyCodeUrl}
                          alt="验证码"
                          onClick={resetVerifyCode}
                        />
                      </Tooltip>
                    </Col>
                  </Row>
                </FormItem>
                <FormItem {...tailFormItemLayout}>
                  <Button loading={ssoUserBindLoading} type="primary" htmlType="submit">
                    用户关联
                  </Button>
                </FormItem>
              </Form>
            )}
          </TabPane>
          <TabPane tab="用户注册" key={ETabKey.Register}>
            {tabKey === ETabKey.Register && (
              <Form {...formItemLayout} onSubmit={handleUserRegister}>
                <FormItem
                  {...formItemLayout}
                  label="登录名称"
                  extra="只允许输入数字、英文字母和下划线,长度限制为6-30个字符"
                >
                  {form.getFieldDecorator('name', {
                    initialValue: '',
                    validateFirst: true,
                    rules: [
                      {
                        required: true,
                        whitespace: true,
                        message: '请填写登录名称',
                      },
                      {
                        pattern: /^[A-Za-z0-9_]+[^\s]$/,
                        message: '只允许输入数字、英文字母和下划线',
                      },
                      {
                        min: 6,
                        message: '登录名长度限制在6-30位之间',
                      },
                      {
                        max: 30,
                        message: '登录名长度限制在6-30位之间',
                      },
                      {
                        validator: validateToPassword,
                      },
                    ],
                  })(<Input autoFocus autoComplete="new-password" placeholder="请填写登录名称" />)}
                </FormItem>
                <FormItem {...formItemLayout} label="用户名称">
                  {form.getFieldDecorator('fullname', {
                    initialValue: '',
                    validateFirst: true,
                    rules: [
                      {
                        required: true,
                        whitespace: true,
                        message: '请填写用户名称',
                      },
                      {
                        pattern: userFullNameRegex,
                        message: '只允许输入数字、英文字母、下划线和中文汉字',
                      },
                      {
                        max: 30,
                        message: '最长可输入30个字符',
                      },
                    ],
                  })(<Input autoComplete="new-password" placeholder="请填写用户名称" />)}
                </FormItem>
                <FormItem {...formItemLayout} label="邮箱">
                  {form.getFieldDecorator('email', {
                    initialValue: '',
                    validateFirst: true,
                    rules: [
                      {
                        required: true,
                        whitespace: true,
                        message: '请填写用户邮箱',
                      },
                      {
                        type: 'email',
                        message: '输入的不是有效的邮箱地址',
                      },
                      {
                        max: 64,
                        message: '最长可输入64个字符',
                      },
                    ],
                  })(<Input autoComplete="new-password" placeholder="请填写用户邮箱" />)}
                </FormItem>
                <FormItem {...formItemLayout} label="设置密码">
                  {form.getFieldDecorator('password', {
                    validateFirst: true,
                    rules: [
                      {
                        required: true,
                        whitespace: true,
                        message: '请设置用户密码',
                      },
                      {
                        validator: validateToUserName,
                      },
                      {
                        validator: validateToNextPassword,
                      },
                    ],
                  })(<Input.Password autoComplete="new-password" placeholder="请设置密码" />)}
                </FormItem>
                <FormItem {...formItemLayout} label="确认密码">
                  {form.getFieldDecorator('confirm', {
                    validateFirst: true,
                    rules: [
                      {
                        required: true,
                        whitespace: true,
                        message: '请确认密码',
                      },
                      {
                        validator: compareToFirstPassword,
                      },
                    ],
                  })(
                    <Input.Password
                      autoComplete="new-password"
                      placeholder="请确认密码"
                      onBlur={handleConfirmBlur}
                    />,
                  )}
                </FormItem>
                <FormItem
                  {...formItemLayout}
                  label="appKey"
                  extra="允许输入字母、数字、中划线-、下划线_和 @ 的组合，不超过32字符"
                >
                  <Row gutter={8}>
                    <Col span={20}>
                      {getFieldDecorator('appKey', {
                        initialValue: platformUserId || '',
                        validateFirst: true,
                        rules: [
                          {
                            required: form.getFieldValue('appToken'),
                            message: '请填写appKey',
                          },
                          {
                            pattern: appKeyAndTokenRegex,
                            message: '只允许输入字母、数字、中划线-、下划线_ 和 @ 组合',
                          },
                          {
                            min: 10,
                            message: '最少请输入10个字符',
                          },
                          {
                            max: 32,
                            message: '最多限制32个字符',
                          },
                        ],
                      })(<Input placeholder="请填写 appKey" />)}
                    </Col>
                    <Col span={4}>
                      <Button
                        type="primary"
                        onClick={() => randomString('appKey')}
                        style={{ width: '100%' }}
                      >
                        随机生成
                      </Button>
                    </Col>
                  </Row>
                </FormItem>
                <FormItem
                  {...formItemLayout}
                  label="appToken"
                  extra={
                    <Fragment>
                      <div>允许输入字母、数字、中划线-、下划线_和 @ 的组合，不超过32字符</div>
                      <div>不填写时默认使用单点登录外部应用的Token，用户也可以自己生成</div>
                    </Fragment>
                  }
                >
                  <Row gutter={8}>
                    <Col span={20}>
                      {getFieldDecorator('appToken', {
                        initialValue: '',
                        validateFirst: true,
                        rules: [
                          {
                            required: false,
                            message: '请填写appToken',
                          },
                          {
                            pattern: appKeyAndTokenRegex,
                            message: '只允许输入字母、数字、中划线-、下划线_ 和 @ 组合',
                          },
                          {
                            min: 10,
                            message: '最少请输入10个字符',
                          },
                          {
                            max: 32,
                            message: '最多限制32个字符',
                          },
                        ],
                      })(
                        <Input.Password
                          autoComplete="new-password"
                          placeholder="请填写 appToken"
                        />,
                      )}
                    </Col>
                    <Col span={4}>
                      <Button
                        type="primary"
                        onClick={() => randomString('appToken')}
                        style={{ width: '100%' }}
                      >
                        随机生成
                      </Button>
                    </Col>
                  </Row>
                </FormItem>
                <FormItem {...tailFormItemLayout}>
                  <Button loading={ssoUserRegisterLoading} type="primary" htmlType="submit">
                    用户注册
                  </Button>
                </FormItem>
              </Form>
            )}
          </TabPane>
        </Tabs>
      </div>
    </Fragment>
  );
};

export default connect(({ loading }: { loading: { effects: Record<string, boolean> } }) => ({
  ssoUserBindLoading: loading.effects['ssoLoginModel/ssoUserBind'],
  ssoUserRegisterLoading: loading.effects['ssoLoginModel/ssoUserRegister'],
}))(Form.create<ISsoOauthProps>()(SsoOauth));
