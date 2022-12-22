import { CONTEXT_PATH } from '@/common/applicationConfig';
import type { ConnectState } from '@/models/connect';
import type { ITempUserInfo } from '@/models/frame/login';
import type { BasicLayoutProps } from '@ant-design/pro-layout';
import { Button, Col, Form, Input, Row, Tooltip } from 'antd';
import { connect } from 'dva';
import React, { useEffect, useState } from 'react';
import type { Dispatch } from 'umi';
import { history } from 'umi';
import { useModel } from 'umi';
import styles from './index.less';

const FormItem = Form.Item;
interface LoginProps {
  route: BasicLayoutProps['route'] & {
    authority: string[];
  };
  dispatch: Dispatch;
  submitting?: boolean;
  tempUserInfo?: ITempUserInfo;
}

const Login: React.FC<LoginProps> = (props) => {
  const { initialState, setInitialState, refresh } = useModel('@@initialState');
  const { submitting, tempUserInfo } = props;
  // const { formatMessage } = useIntl();
  // const { menuData } = getMenuData(
  //   route?.routes || [],
  //   { locale: false },
  //   formatMessage,
  //   menuDataRender,
  // );
  const [form] = Form.useForm();
  // 验证码
  const [verifyCodeUrl, setVerifyCodeUrl] = useState<string>('');

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

  useEffect(() => {
    form.setFieldsValue({
      username: tempUserInfo?.username,
      password: tempUserInfo?.password,
    });
  }, [form, tempUserInfo]);

  const resetVerifyCode = () => {
    getVerifyCodeUrl();
  };

  // TODO： values type fix;
  const handleSubmit = (values: any) => {
    const { dispatch } = props;
    dispatch({
      type: 'loginModel/login',
      payload: { ...values },
    }).then(async (success: boolean) => {
      if (success) {
        const userInfo = await initialState?.fetchUserInfo?.();
        if (userInfo) {
          setInitialState({
            ...initialState,
            currentUser: userInfo,
          });
          // 重新进入用户退出时的页面
          if (history.location.query?.openUrl) {
            const tmpQuery = history.location.query || {};
            const urlStr = decodeURIComponent((history.location.query?.openUrl as string) || '/');
            const openUrl = urlStr.split('?')[0];
            const openQuery = !(urlStr.split('?')[1] || '').length
              ? ''
              : (urlStr.split('?')[1] || '')
                  .split('&')
                  .map((item) => {
                    const items = item.split('=');
                    return `${items[0]}=${encodeURIComponent(items[1])}`;
                  })
                  .join('&');
            const jumpToPath = `${openUrl}${openQuery.length ? '?' : ''}${openQuery}`;
            delete tmpQuery.openUrl;
            history.replace(jumpToPath);
          } else {
            const path = localStorage.getItem(`LOGOUT_USER_${window.location.host}_${userInfo.id}`);
            history.replace(path || '/');
          }
        }
        // 取用户登录失败时，不进行页面的跳转操作
        refresh();
      }
    });
  };

  return (
    <div className={styles.main}>
      <p className={styles.title}>用户登录</p>
      <Form layout="vertical" form={form} onFinish={handleSubmit}>
        <FormItem
          label="登录名"
          name="username"
          rules={[{ required: true, whitespace: true, message: '请输入您的登录名' }]}
        >
          <Input size="large" placeholder="请输入您的登录名" />
        </FormItem>
        <FormItem
          label="密码"
          name="password"
          rules={[{ required: true, whitespace: true, message: '请输入您的密码' }]}
        >
          <Input.Password size="large" placeholder="请输入您的密码" />
        </FormItem>
        <FormItem label="验证码">
          <Row gutter={8}>
            <Col span={16}>
              <FormItem
                noStyle
                name="code"
                rules={[{ required: true, whitespace: true, message: '请输入验证码' }]}
              >
                <Input size="large" type="text" placeholder="请输入验证码" />
              </FormItem>
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
        <FormItem>
          <Button
            type="primary"
            size="large"
            htmlType="submit"
            className={styles.submitBtn}
            loading={submitting}
          >
            立 即 登 录
          </Button>
        </FormItem>
      </Form>
    </div>
  );
};

const mapStateToProps = ({ loading: { effects }, loginModel: { tempUserInfo } }: ConnectState) => ({
  submitting: effects['loginModel/login'],
  tempUserInfo,
});

export default connect(mapStateToProps)(Login);
