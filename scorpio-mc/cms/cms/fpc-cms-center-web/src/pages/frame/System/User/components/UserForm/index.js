import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { history } from 'umi';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Button, Input, Select, Row, Col, message, Modal } from 'antd';
import {
  createConfirmModal,
  updateConfirmModal,
  randomSecret,
  userFullNameRegex,
  appKeyAndTokenRegex,
  passwordRegex,
  passwordRegex_charNumberSpecialChar,
  passwordRegex_charSpecialChar,
} from '@/utils/utils';
import { ROLE_AUDIT_USER, ROLE_SYS_USER, ROLE_SERVICE_USER } from '../../index';
import { querySecuritySettings } from '../../../../../../services/frame/security';

const { Option } = Select;
const FormItem = Form.Item;
const { TextArea } = Input;

// 表单布局
const formItemLayout = {
  labelCol: {
    sm: { span: 4 },
  },
  wrapperCol: {
    sm: { span: 17 },
  },
};

const formTailLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 17, offset: 4 },
};

@Form.create()
@connect(({ rolesModel: { allRoles }, globalModel: { currentUser }, loading }) => ({
  allRoles,
  currentUser,
  queryRoleLoading: loading.effects['rolesModel/queryAllRoles'],
}))
class UserForm extends PureComponent {
  constructor(props) {
    super(props);
    this.createConfirmModal = createConfirmModal.bind(this);
    this.updateConfirmModal = updateConfirmModal.bind(this);

    this.state = {
      confirmDirty: false, // 确认密码是否通过
      passwordRules: {},
    };
  }
  
  componentWillMount() {
    querySecuritySettings().then((res) => {
      const { success, result } = res;
      // console.log(result,'result');
      if (success) {
        this.setState({ passwordRules: result });
      }
      // console.log(this.state.passwordRules, 'passwordRules');
    });
  }

  componentDidMount() {
    const { dispatch } = this.props;
    // 获取全部的权限
    dispatch({
      type: 'rolesModel/queryAllRoles',
    });
  }

  randomString = (fieldKey) => {
    const { form } = this.props;
    const secret = randomSecret();
    form.setFieldsValue({
      [fieldKey]: secret,
    });
  };

  handleSubmit = (e) => {
    const { form, operateType } = this.props;
    e.preventDefault();
    form.validateFieldsAndScroll((err, fieldsValue) => {
      if (err) return;
      // appKey 和 appToken 都存在或者是都不存在
      const { appKey, appToken } = fieldsValue;
      if (!appKey && appToken) {
        message.warning('appToken已填写，请填写appKey');
        return;
      }
      if (appKey && !appToken) {
        message.warning('appKey已填写，请填写appToken');
        return;
      }

      const values = { ...fieldsValue };
      // 删除密码确认的值
      delete values.confirm;
      // 分配的角色转为 String
      const { roleIds } = fieldsValue;
      if (typeof roleIds === 'object') {
        values.roleIds = roleIds.join(',');
      }
      if (operateType === 'CREATE') {
        this.handleCreate(values);
      } else {
        this.handleUpdate(values);
      }
    });
  };

  handleGoListPage = () => {
    history.goBack();
  };

  handleCreate = (values) => {
    this.createConfirmModal({
      dispatchType: 'usersModel/createUser',
      values,
      onOk: this.handleGoListPage,
      onCancel: this.handleReset,
    });
  };

  handleUpdate = (values) => {
    const { currentUser, dispatch } = this.props;

    if (currentUser.id === values.id) {
      Modal.confirm({
        title: '修改自己的信息后会退出登录',
        content: '您确定继续修改个人信息吗？',
        okText: '确认',
        cancelText: '取消',
        onOk: () => {
          dispatch({
            type: 'usersModel/updateUser',
            payload: values,
          }).then((success) => {
            if (success) {
              // 修改自己的账号信息成功后，主动触发退出
              history.push('/login');
            }
          });
        },
      });
    } else {
      this.updateConfirmModal({
        dispatchType: 'usersModel/updateUser',
        values,
        onOk: this.handleGoListPage,
      });
    }
  };

  handleReset = () => {
    const { form } = this.props;
    form.resetFields();
  };

  // 确认密码输入聚焦
  handleConfirmBlur = (e) => {
    const { value } = e.target;
    const { confirmDirty } = this.state;
    this.setState({ confirmDirty: confirmDirty || !!value });
  };

  // 确认密码
  compareToFirstPassword = (rule, value, callback) => {
    const { form } = this.props;
    if (value && value !== form.getFieldValue('password')) {
      callback('两次密码输入不一致!');
    } else {
      callback();
    }
  };

  // 和用户名进行比较
  validateToUserName = (rule, value, callback) => {
    const { form } = this.props;
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

  // 登录名称变化时。重新触发密码的校验
  validateToPassword = (rule, value, callback) => {
    const { form } = this.props;
    const password = form.getFieldValue('password');
    if (password) {
      form.validateFields(['password'], { force: true });
    }
    callback();
  };

  // 校验密码
  validateToNextPassword = (rule, value, callback) => {
    const { form } = this.props;
    // 校验密码强度
    // 1. 必须同时包含大写字母、小写字母和数字，三种组合
    // if (value) {
    //   if (!passwordRegex.test(value)) {
    //     callback('密码必须同时包含大写字母、小写字母和数字');
    //   }
    //   if (value.length < 6 || value.length > 30) {
    //     callback('密码长度6-30位');
    //   }
    // }
    const { passwordRules } = this.state;
    if (value) {
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
    }

    const { confirmDirty } = this.state;
    if (value && confirmDirty) {
      form.validateFields(['confirm'], { force: true });
    }
    callback();
  };

  render() {
    const {
      operateType, // CREATE-新建 | UPDATE-编辑
      form,
      form: { getFieldDecorator },
      loading,
      allRoles, // 所有的权限列表
      queryRoleLoading,
      detail = {}, // 当前编辑的用户
    } = this.props;

    let currentRoleIds = [];
    if (Array.isArray(detail.userRoles)) {
      currentRoleIds = detail.userRoles.map((item) => item.id);
    }

    return (
      <Form onSubmit={this.handleSubmit}>
        <FormItem {...formItemLayout} label="id" style={{ display: 'none' }}>
          {form.getFieldDecorator('id', {
            initialValue: detail.id,
          })(<Input type="hidden" />)}
        </FormItem>
        <FormItem
          {...formItemLayout}
          label="登录名称"
          extra="只允许输入数字、英文字母和下划线,长度限制为6-30个字符"
        >
          {form.getFieldDecorator('name', {
            initialValue: detail.name,
            validateFirst: true,
            rules: !detail.id && [
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
                validator: this.validateToPassword,
              },
            ],
          })(<Input disabled={operateType !== 'CREATE'} placeholder="请填写登录名称" />)}
        </FormItem>
        <FormItem {...formItemLayout} label="用户名称">
          {form.getFieldDecorator('fullname', {
            initialValue: detail.fullname,
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
          })(<Input placeholder="请填写用户名称" />)}
        </FormItem>
        <FormItem {...formItemLayout} label="邮箱">
          {form.getFieldDecorator('email', {
            initialValue: detail.email,
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
                required: operateType === 'CREATE',
                message: '请设置用户密码',
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
                this.psdInput = node;
                this.psdInput.input.oncopy = () => false;
              }}
              autoComplete="new-password"
              placeholder={operateType === 'CREATE' ? '请设置密码' : '不填,即默认不更改密码'}
            />,
          )}
        </FormItem>
        <FormItem {...formItemLayout} label="确认密码">
          {form.getFieldDecorator('confirm', {
            validateFirst: true,
            rules: [
              {
                required: operateType === 'CREATE',
                message: '请确认密码',
              },
              {
                validator: this.compareToFirstPassword,
              },
            ],
          })(
            <Input.Password
              ref={(node) => {
                this.psdInput = node;
                this.psdInput.input.oncopy = () => false;
              }}
              autoComplete="new-password"
              placeholder={operateType === 'CREATE' ? '请确认密码' : '不填,即默认不更改密码'}
              onBlur={this.handleConfirmBlur}
            />,
          )}
        </FormItem>
        <FormItem {...formItemLayout} label="分配角色">
          {form.getFieldDecorator('roleIds', {
            initialValue: currentRoleIds,
            validateFirst: true,
            rules: [
              {
                required: true,
                message: '角色不能为空,请分配角色',
              },
            ],
          })(
            <Select
              placeholder="请选择角色"
              mode="multiple"
              loading={queryRoleLoading}
              disabled={
                detail.userRoles &&
                detail.userRoles.length === 1 &&
                (detail.userRoles[0].nameEn === ROLE_SYS_USER ||
                  detail.userRoles[0].nameEn === ROLE_AUDIT_USER ||
                  detail.userRoles[0].nameEn === ROLE_SERVICE_USER)
              }
            >
              {allRoles.map((role) => {
                return (
                  <Option
                    disabled={
                      role.nameEn === ROLE_SYS_USER ||
                      role.nameEn === ROLE_AUDIT_USER ||
                      role.nameEn === ROLE_SERVICE_USER
                    }
                    value={role.id}
                  >
                    {role.nameZh}
                  </Option>
                );
              })}
            </Select>,
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
                initialValue: detail.appKey || '',
                validateFirst: true,
                rules: [
                  {
                    required: detail.appKey && 1,
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
                onClick={() => this.randomString('appKey')}
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
          extra="允许输入字母、数字、中划线-、下划线_和 @ 的组合，不超过32字符"
        >
          <Row gutter={8}>
            <Col span={20}>
              {getFieldDecorator('appToken', {
                initialValue: detail.appToken || '',
                validateFirst: true,
                rules: [
                  {
                    required: detail.appToken && 1,
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
              })(<Input.Password placeholder="请填写 appToken" />)}
            </Col>
            <Col span={4}>
              <Button
                type="primary"
                onClick={() => this.randomString('appToken')}
                style={{ width: '100%' }}
              >
                随机生成
              </Button>
            </Col>
          </Row>
        </FormItem>
        <FormItem {...formItemLayout} label="备注">
          {form.getFieldDecorator('description', {
            initialValue: detail.description || '',
            rules: [
              {
                max: 255,
                message: '最长可输入255个字符',
              },
            ],
          })(<TextArea rows={4} placeholder="请填写备注信息，最长限制255字符" />)}
        </FormItem>
        <FormItem {...formTailLayout}>
          <Button className="mr-10" type="primary" htmlType="submit" loading={loading}>
            保存
          </Button>
          <Button onClick={() => history.goBack()}>返回</Button>
        </FormItem>
      </Form>
    );
  }
}

export default UserForm;
