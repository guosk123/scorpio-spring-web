import {
  passwordRegex,
  passwordRegex_charNumberSpecialChar,
  passwordRegex_charSpecialChar,
} from '@/utils/utils';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Alert, Button, Input, Modal } from 'antd';
import { connect } from 'dva';
import React, { PureComponent } from 'react';
import {querySecuritySettings} from '../../../../services/frame/security';

const FormItem = Form.Item;

@connect(({ loading: { effects } }) => ({
  submitLoading: effects['globalModel/updateCurrentUserPassword'],
}))
@Form.create()
class ChangePassword extends PureComponent {
  state = {
    confirmDirty: false, // 确认密码是否通过
    passwordRules: {},
  };
  componentWillMount() {
    querySecuritySettings().then((res) => {
      const { success, result } = res;
      // console.log(result,'result');
      if (success) {
        this.setState({ passwordRules: result });
      }
      console.log(this.state.passwordRules, 'passwordRules');
    });
  }
  componentDidMount() {}

  // 确认密码输入聚焦
  handleConfirmBlur = (e) => {
    const { value } = e.target;
    const { confirmDirty } = this.state;
    this.setState({ confirmDirty: confirmDirty || !!value });
  };

  // 确认密码
  validatePassword = (rule, value, callback) => {
    const { form } = this.props;
    const newPass = form.getFieldValue('password');
    if (!value && newPass) {
      callback('先输入原始密码');
    }
    callback();
  };

  // 确认密码
  compareToNewPassword = (rule, value, callback) => {
    const { form } = this.props;
    const newPass = form.getFieldValue('password');
    if (!value && newPass) {
      callback('请再次输入新密码');
    }
    if (value && value !== newPass) {
      callback('两次密码输入不一致');
    } else {
      callback();
    }
  };

  // 和用户名进行比较
  validateToUserName = (rule, value, callback) => {
    const { username: userName } = this.props.currentUser;
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
  validateToNextPassword = (rule, value, callback) => {
    const { form } = this.props;
    // 校验密码强度
    // 1. 必须同时包含大写字母、小写字母和数字，三种组合
    // 2. 长度在6-30之间
    const { passwordRules } = this.state;
    if (value) {
      // if (!passwordRegex.test(value)) {
      //   callback('密码必须是同时包含大写字母、小写字母和数字的非空字符的组合');
      // }
      // if (value.length < 6 || value.length > 30) {
      //   callback('密码长度6-30位');
      // }
      if (passwordRules.passwordComplexity == 'case_sensitivity_letter_number_char') {
        if (
          !passwordRegex_charNumberSpecialChar.test(value) ||
          value.length < passwordRules.passwordMinLength ||
          value.length > passwordRules.passwordMaxLength
        ) {
          callback(
            `密码必须同时包含大写字母、小写字母和数字和特殊字符，密码长度${passwordRules.passwordMinLength}-${passwordRules.passwordMaxLength}位`,
          );
        }
      }
      if (passwordRules.passwordComplexity == 'case_sensitivity_letter_number') {
        if (
          !passwordRegex.test(value) ||
          value.length < passwordRules.passwordMinLength ||
          value.length > passwordRules.passwordMaxLength
        ) {
          callback(
            `密码必须同时包含大写字母、小写字母和数字，密码长度${passwordRules.passwordMinLength}-${passwordRules.passwordMaxLength}位`,
          );
        }
      }
      if (passwordRules.passwordComplexity == 'case_sensitivity_letter_char') {
        if (
          !passwordRegex_charSpecialChar.test(value) ||
          value.length < passwordRules.passwordMinLength ||
          value.length > passwordRules.passwordMaxLength
        ) {
          callback(
            `密码必须同时包含大写字母、小写字母和特殊字符，密码长度${passwordRules.passwordMinLength}-${passwordRules.passwordMaxLength}位`,
          );
        }
      }
      if (
        value.length < passwordRules.passwordMinLength ||
        value.length > passwordRules.passwordMaxLength
      ) {
        callback(`密码长度${passwordRules.passwordMinLength}-${passwordRules.passwordMaxLength}位`);
      }
      // 与旧密码匹配，不能相同
      const oldPass = form.getFieldValue('oldPassword');
      if (value === oldPass) {
        callback('新密码不能和原始密码相同');
      }
    }

    const { confirmDirty } = this.state;
    if (value && confirmDirty) {
      form.validateFields(['confirmPassword'], { force: true });
    }
    callback();
  };

  handleOk = () => {
    const { form, dispatch } = this.props;
    form.validateFields((err, fieldsValue) => {
      if (err) return;
      const { oldPassword, password } = fieldsValue;
      dispatch({
        type: 'globalModel/updateCurrentUserPassword',
        payload: {
          oldPassword,
          password,
        },
      }).then((success) => {
        if (success) {
          this.handleCancel();
        }
      });
    });
  };

  handleCancel = () => {
    const { dispatch } = this.props;
    dispatch({
      type: 'globalModel/updateState',
      payload: {
        changePwdModalVisible: false,
      },
    });
  };

  render() {
    const { form, currentUser, visible, submitLoading } = this.props;
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

    const { userPasswordNonReliable } = currentUser;

    return (
      <Modal
        width={800}
        destroyOnClose
        keyboard={false}
        maskClosable={false}
        confirmLoading={submitLoading}
        title="修改密码"
        visible={visible}
        onOk={() => this.handleOk()}
        onCancel={this.handleCancel}
        afterClose={this.handleCancel}
        closable={!userPasswordNonReliable}
        footer={[
          !userPasswordNonReliable && (
            <Button key="back" onClick={this.handleCancel}>
              取消
            </Button>
          ),
          <Button
            key="submit"
            type="primary"
            loading={submitLoading}
            onClick={() => this.handleOk()}
          >
            确定
          </Button>,
        ]}
      >
        <Form>
          {userPasswordNonReliable && (
            <Alert
              message="当前密码为初始密码，为了提高账户安全性，请尽快修改密码。"
              type="warning"
              showIcon
              style={{ marginBottom: 6 }}
            />
          )}
          <FormItem {...formItemLayout} label="登录名称" style={{ marginBottom: 6 }}>
            {currentUser.username}
          </FormItem>
          <FormItem {...formItemLayout} label="用户名称" style={{ marginBottom: 6 }}>
            {currentUser.fullname}
          </FormItem>
          <FormItem {...formItemLayout} label="邮箱" style={{ marginBottom: 6 }}>
            {currentUser.email}
          </FormItem>
          <FormItem {...formItemLayout} label="原始密码">
            {form.getFieldDecorator('oldPassword', {
              validateFirst: true,
              rules: [
                {
                  required: true,
                  message: '请填写原始密码',
                },
                {
                  validator: this.validatePassword,
                },
              ],
            })(
              <Input.Password
                ref={(node) => {
                  this.psdInput1 = node;
                  this.psdInput1.input.oncopy = () => false;
                }}
                autoComplete="new-password"
                placeholder="请输入原始密码"
              />,
            )}
          </FormItem>
          <FormItem {...formItemLayout} label="新密码">
            {form.getFieldDecorator('password', {
              validateFirst: true, // 当某一规则校验不通过时，是否停止剩下的规则的
              rules: [
                {
                  required: true,
                  message: '请设置新密码',
                },
                {
                  validator: this.validateToUserName,
                },
                {
                  validator: this.validateToNextPassword,
                },
              ],
            })(
              <Input.Password
                ref={(node) => {
                  this.psdInput2 = node;
                  this.psdInput2.input.oncopy = () => false;
                }}
                autoComplete="new-password"
                placeholder="请设置新密码"
              />,
            )}
          </FormItem>
          <FormItem {...formItemLayout} label="确认密码">
            {form.getFieldDecorator('confirmPassword', {
              validateFirst: true, // 当某一规则校验不通过时，是否停止剩下的规则的
              rules: [
                {
                  required: true,
                  message: '请再次输入新密码',
                },
                {
                  validator: this.compareToNewPassword,
                },
              ],
            })(
              <Input.Password
                ref={(node) => {
                  this.psdInput3 = node;
                  this.psdInput3.input.oncopy = () => false;
                }}
                autoComplete="new-password"
                placeholder="请再次输入新密码"
                onBlur={this.handleConfirmBlur}
              />,
            )}
          </FormItem>
        </Form>
      </Modal>
    );
  }
}

export default ChangePassword;
