import { appKeyAndTokenRegex, randomSecret, userFullNameRegex } from '@/utils/utils';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Alert, Button, Col, Input, message, Modal, Row } from 'antd';
import { connect } from 'dva';
import React, { PureComponent } from 'react';

const FormItem = Form.Item;

@connect(({ loading: { effects } }) => ({
  submitLoading: effects['globalModel/updateCurrentUserInfo'],
  checkLoading: effects['globalModel/checkCurrentPassword'],
}))
@Form.create()
class UserInfo extends PureComponent {
  constructor(props) {
    super(props);
    const { currentUser } = props;
    if (currentUser && currentUser.id) {
      this.state = {
        isCheckPwd: false,
        fullname: currentUser.fullname,
        email: currentUser.email,
        appKey: currentUser.appKey,
        appToken: currentUser.appToken,
      };
    }
  }

  handleNext = () => {
    const { form } = this.props;
    form.validateFields((err, fieldsValue) => {
      if (err) return;
      const { appKey = '', appToken = '', fullname, email } = fieldsValue;
      // appKey 和 appToken 都有或者是都没有
      if (!appKey && appToken) {
        message.warning('appToken已填写，请填写appKey');
        return;
      }
      if (appKey && !appToken) {
        message.warning('appKey已填写，请填写appToken');
        return;
      }
      this.setState({
        isCheckPwd: true,
        fullname,
        email,
        appKey,
        appToken,
      });
    });
  };

  handlePrev = () => {
    this.setState({
      isCheckPwd: false,
    });
  };

  handleCheck = () => {
    const { form, dispatch } = this.props;
    form.validateFields((err, fieldsValue) => {
      if (err) return;

      const { password } = fieldsValue;

      // 先确认当前密码
      dispatch({
        type: 'globalModel/checkCurrentPassword',
        payload: {
          password,
        },
      }).then((result) => {
        if (result) {
          // 密码正常，进行提交
          this.handleSubmit();
        } else {
          // 重置登录密码
          form.resetFields(['password']);
        }
      });
    });
  };

  handleSubmit = () => {
    const { dispatch } = this.props;
    const { fullname, email, appKey, appToken } = this.state;
    dispatch({
      type: 'globalModel/updateCurrentUserInfo',
      payload: {
        fullname,
        email,
        appKey,
        appToken,
      },
    });
  };

  handleCancel = () => {
    const { dispatch } = this.props;
    this.setState({
      isCheckPwd: false,
    });
    dispatch({
      type: 'globalModel/updateState',
      payload: {
        userInfoModalVisible: false,
      },
    });
  };

  render() {
    const { form, currentUser, submitLoading, checkLoading, visible } = this.props;
    const { isCheckPwd } = this.state;
    // 表单布局
    const formItemLayout = {
      labelCol: {
        xs: { span: 24 },
        sm: { span: 4 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 17 },
      },
    };

    const randomString = (fieldKey) => {
      const secret = randomSecret();
      form.setFieldsValue({
        [fieldKey]: secret,
      });
    };

    const modalFooter = isCheckPwd
      ? { cancelText: '上一步', okText: '保存', onOk: this.handleCheck, onCancel: this.handlePrev }
      : {
          cancelText: '取消',
          okText: '下一步',
          onOk: this.handleNext,
          onCancel: this.handleCancel,
        };

    const getModalContent = () => {
      const { fullname, email, appKey, appToken } = this.state;
      if (isCheckPwd) {
        return (
          <div>
            <Alert
              message="为了有效保护账户安全，修改前请输入登录密码进行身份校验。"
              type="info"
              showIcon
              style={{ width: '90%', margin: '0 auto 20px' }}
            />
            <Form>
              <FormItem {...formItemLayout} label="登录密码">
                {form.getFieldDecorator('password', {
                  rules: [
                    {
                      required: true,
                      message: '请填写登录密码',
                    },
                  ],
                })(
                  <Input.Password
                    ref={(node) => {
                      this.psdInput = node;
                      this.psdInput.input.oncopy = () => false;
                    }}
                    autoComplete="new-password"
                    placeholder="请输入登录密码"
                    autoFocus
                  />,
                )}
              </FormItem>
            </Form>
          </div>
        );
      }
      return (
        <Form>
          <FormItem {...formItemLayout} label="用户名称">
            {form.getFieldDecorator('fullname', {
              initialValue: fullname,
              validateFirst: true,
              rules: [
                {
                  required: true,
                  whitespace: true,
                  message: '请填写用户名称',
                },
                {
                  pattern: userFullNameRegex,
                  message: '只允许输入数字、英文字母、下划线_和中文汉字',
                },
                {
                  max: 30,
                  message: '最长可输入30个字符',
                },
              ],
            })(<Input placeholder="请填写用户名称" />)}
          </FormItem>
          <FormItem {...formItemLayout} label="邮箱">
            {form.getFieldDecorator('email', {
              initialValue: email,
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
            })(<Input placeholder="请填写用户邮箱" />)}
          </FormItem>
          <FormItem
            {...formItemLayout}
            label="appKey"
            extra="允许输入字母、数字、中划线-、下划线_和 @ 的组合，不超过32字符"
          >
            <Row gutter={8}>
              <Col span={20}>
                {form.getFieldDecorator('appKey', {
                  initialValue: appKey,
                  validateFirst: true,
                  rules: [
                    {
                      required: currentUser && currentUser.appKey,
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
                })(<Input />)}
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
            extra="允许输入字母、数字、中划线、下划线和 @ 的组合，不超过32字符"
          >
            <Row gutter={8}>
              <Col span={20}>
                {form.getFieldDecorator('appToken', {
                  initialValue: appToken,
                  validateFirst: true,
                  rules: [
                    {
                      required: currentUser && currentUser.appToken,
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
                })(<Input.Password autoComplete="new-password" />)}
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
        </Form>
      );
    };

    return (
      <Modal
        width={800}
        destroyOnClose
        keyboard={false}
        maskClosable={false}
        confirmLoading={submitLoading || checkLoading}
        title="个人信息"
        visible={visible}
        afterClose={this.handleCancel}
        {...modalFooter}
      >
        <div style={{ height: 300 }}>{getModalContent()}</div>
      </Modal>
    );
  }
}

export default UserInfo;
