import React, { PureComponent, Fragment } from 'react';
import { history } from 'umi';
import { connect } from 'dva';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Input, Button, Checkbox, Select } from 'antd';
import { ipV4Regex, ipV6Regex, createConfirmModal, updateConfirmModal } from '@/utils/utils';
import styles from './index.less';
import { METADATA_COLLECT_LEVEL_MAP } from '../../typings';

const FormItem = Form.Item;
const CheckboxGroup = Checkbox.Group;

/**
 * 校验IP、IP段
 * @param {*} rule
 * @param {*} value
 * @param {*} callback
 *
 * @example 192.168.1.1
 * @example 192.168.1.2/24
 */
function checkCollectIp(rule, value, callback) {
  console.log(value, typeof value, 'value');
  if (!value) {
    callback();
    return;
  }
  // 如果有 - ，直接判错
  if (value.indexOf('-') > -1) {
    callback('请输入正确的IP/IP段');
  } else if (value.indexOf('/') > -1) {
    const ips = value.split('/');
    // 校验第一个 ip
    if (!ipV4Regex.test(ips[0])) {
      if (!ipV6Regex.test(ips[0])) {
        callback('请输入正确的IP/IP段');
        return;
      }
    }
    // 校验子网掩码
    // eslint-disable-next-line no-restricted-globals
    if (!ips[1] || isNaN(ips[1])) {
      callback('请输入网络号');
      return;
    }
    // 这里把 0 排除掉
    if ((ips[1] <= 0 || ips[1] > 32) && ipV4Regex.test(ips[0])) {
      callback('子网掩码范围是(0,32]。例，192.168.1.2/24');
      return;
    }
    if ((ips[1] <= 0 || ips[1] > 128) && ipV6Regex.test(ips[0])) {
      callback('子网掩码范围是(0,128]');
      return;
    }
  } else if (!ipV4Regex.test(value)) {
    if (!ipV6Regex.test(value)) {
      callback('请输入正确的IP/IP段');
      return;
    }
  }
  callback();
}

@Form.create()
@connect(({ metadataModel: { metadataProtocolsList }, loading }) => ({
  metadataProtocolsList,
  queryAllProtocolsLoading: loading.effects['metadataModel/queryAllProtocols'],
}))
class MetadataCollectPolicyForm extends PureComponent {
  constructor(props) {
    super(props);
    this.createConfirmModal = createConfirmModal.bind(this);
    this.updateConfirmModal = updateConfirmModal.bind(this);
  }

  handleReset = () => {
    const { form } = this.props;
    form.resetFields();
  };

  handleGoListPage = () => {
    history.goBack();
  };

  handleCreate = (values) => {
    this.createConfirmModal({
      dispatchType: 'metadatCollectPolicyModel/create',
      values,
      onOk: this.handleGoListPage,
      onCancel: () => {
        this.handleReset();
      },
    });
  };

  handleUpdate = (values) => {
    this.updateConfirmModal({
      dispatchType: 'metadatCollectPolicyModel/update',
      values,
      onOk: this.handleGoListPage,
    });
  };

  onCheckAllChange = (e) => {
    const { form, metadataProtocolsList } = this.props;
    form.setFieldsValue({
      l7ProtocolId: e.target.checked ? metadataProtocolsList.map((item) => item.protocolId) : [],
    });
  };

  render() {
    const {
      form,
      form: { getFieldDecorator, getFieldValue },
      type = 'CREATE', // CREATE-新建 | UPDATE-编辑
      values = {}, // 初始值
      loading,
      metadataProtocolsList,
    } = this.props;
    const formItemLayout = {
      labelCol: {
        xs: { span: 24 },
        sm: { span: 4 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 18 },
      },
    };

    const okHandle = (e) => {
      e.preventDefault();
      form.validateFields((err, fieldsValue) => {
        if (err) return;
        const submitData = {
          ...fieldsValue,
          l7ProtocolId: fieldsValue.l7ProtocolId.join(','),
          ipAddress: fieldsValue.ipAddress || '',
        };
        if (type === 'CREATE') {
          this.handleCreate(submitData);
        } else {
          this.handleUpdate(submitData);
        }
      });
    };

    const checkedBoxLength = (getFieldValue('l7ProtocolId') || []).length;

    return (
      <Form onSubmit={okHandle}>
        <FormItem {...formItemLayout} label="id" style={{ display: 'none' }}>
          {getFieldDecorator('id', {
            initialValue: values.id,
          })(<Input />)}
        </FormItem>
        {type === 'UPDATE' && (
          <FormItem {...formItemLayout} label="序号" style={{ marginBottom: 0 }}>
            <span className="ant-form-text">{values.orderNo}</span>
          </FormItem>
        )}
        <FormItem {...formItemLayout} label="名称">
          {getFieldDecorator('name', {
            initialValue: values.name,
            validateFirst: true,
            rules: [
              {
                required: true,
                message: '请填写采集策略名称',
              },
              {
                max: 32,
                message: '最多限制32个字符',
              },
            ],
          })(<Input placeholder="请填写采集策略名称" />)}
        </FormItem>
        <FormItem
          {...formItemLayout}
          label="IP/IP段"
          extra={
            <ul style={{ paddingLeft: 20, listStyle: 'decimal' }}>
              <li>输入 A.B.C.D 格式或者 A:B:C:D:E:F:G:H 的IP地址</li>
              <li>或 A.B.C.D/子网掩码 A:B:C:D:E:F:G:H/网络号 格式的IP网段</li>
              <li>不填写即表示所有IP</li>
            </ul>
          }
        >
          {getFieldDecorator('ipAddress', {
            initialValue: values.ipAddress,
            validateFirst: true,
            rules: [
              {
                required: false,
                message: '请填写IP/IP段',
              },
              {
                validator: checkCollectIp,
              },
            ],
          })(<Input placeholder="请填写IP/IP段" />)}
        </FormItem>
        <FormItem {...formItemLayout} label="协议">
          <Fragment>
            <Checkbox
              indeterminate={
                checkedBoxLength > 0 && checkedBoxLength < metadataProtocolsList.length
              }
              onChange={this.onCheckAllChange}
              checked={checkedBoxLength === metadataProtocolsList.length}
            >
              全选
            </Checkbox>
            {getFieldDecorator('l7ProtocolId', {
              initialValue: values.l7ProtocolId ? values.l7ProtocolId.split(',') : [],
              rules: [
                {
                  required: true,
                  message: '请至少勾选一种协议',
                },
              ],
            })(
              <CheckboxGroup
                className={styles.checkboxGroup}
                options={metadataProtocolsList.map((el) => ({
                  value: el.protocolId,
                  label: el.nameText,
                }))}
              />,
            )}
          </Fragment>
        </FormItem>
        <FormItem {...formItemLayout} label="级别">
          {getFieldDecorator('level', {
            initialValue: values.level,
            rules: [
              {
                required: true,
                message: '请选择级别',
              },
            ],
          })(
            <Select placeholder="请选择级别">
              {Object.keys(METADATA_COLLECT_LEVEL_MAP).map((key) => (
                <Select.Option key={key} value={key}>
                  {METADATA_COLLECT_LEVEL_MAP[key]}
                </Select.Option>
              ))}
            </Select>,
          )}
        </FormItem>
        <FormItem wrapperCol={{ span: 12, offset: 4 }}>
          <Button className="mr-10" type="primary" htmlType="submit" loading={loading}>
            保存
          </Button>
          <Button onClick={() => this.handleGoListPage()}>取消</Button>
        </FormItem>
      </Form>
    );
  }
}

export default MetadataCollectPolicyForm;
