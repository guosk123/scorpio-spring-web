import React, { PureComponent } from 'react';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Input, Button, Select, InputNumber } from 'antd';
import { history } from 'umi';
import { connect } from 'dva';
import PropTypes from 'prop-types';
import { createConfirmModal, updateConfirmModal } from '@/utils/utils';

const FormItem = Form.Item;

const formLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 16 },
};

/**
 * 传输层协议
 */
const IP_PROTOCOL_LIST = ['TCP', 'UDP'];

/**
 * 系统内置
 */
export const STANDARD_PROTOCOL_SOURCE_DEFAULT = '0';
/**
 * 手工配置
 */
export const STANDARD_PROTOCOL_SOURCE_CUSTOM = '1';

export const STANDARD_PROTOCOL_SOURCE_LIST = [
  {
    value: STANDARD_PROTOCOL_SOURCE_DEFAULT,
    label: '系统内置',
  },
  {
    value: STANDARD_PROTOCOL_SOURCE_CUSTOM,
    label: '手工配置',
  },
];

@Form.create()
@connect(({ metadataModel: { allL7ProtocolsList }, loading }) => ({
  allL7ProtocolsList,
  queryAllProtocolsLoading: loading.effects['metadataModel/queryAllProtocols'],
}))
class StandardProtocolForm extends PureComponent {
  static propTypes = {
    detail: PropTypes.object,
    submitLoading: PropTypes.bool,
    operateType: PropTypes.oneOf(['CREATE', 'UPDATE']).isRequired,
  };

  static defaultProps = {
    detail: {},
    submitLoading: false,
    // operateType: '',
  };

  constructor(props) {
    super(props);
    this.createConfirmModal = createConfirmModal.bind(this);
    this.updateConfirmModal = updateConfirmModal.bind(this);
  }

  // 提交
  handleSubmit = (e) => {
    const { form, operateType } = this.props;
    e.preventDefault();
    form.validateFieldsAndScroll((err, fieldsValue) => {
      if (err) return;

      const values = { ...fieldsValue };
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

  handleReset = () => {
    const { form } = this.props;
    form.resetFields();
  };

  handleCreate = (values) => {
    this.createConfirmModal({
      dispatchType: 'standardProtocolModel/createStandardProtocol',
      values,
      onOk: this.handleGoListPage,
      onCancel: this.handleReset,
    });
  };

  handleUpdate = (values) => {
    this.updateConfirmModal({
      dispatchType: 'standardProtocolModel/updateStandardProtocol',
      values,
      onOk: this.handleGoListPage,
    });
  };

  render() {
    const {
      form,
      submitLoading,
      detail = {},
      allL7ProtocolsList = [],
      queryAllProtocolsLoading = false,
    } = this.props;
    return (
      <Form onSubmit={this.handleSubmit}>
        <FormItem key="id" {...formLayout} label="id" style={{ display: 'none' }}>
          {form.getFieldDecorator('id', {
            initialValue: detail.id,
          })(<Input placeholder="请输入" />)}
        </FormItem>
        <FormItem key="l7ProtocolId" {...formLayout} label="协议">
          {form.getFieldDecorator('l7ProtocolId', {
            initialValue: detail.l7ProtocolId,
            rules: [{ required: true, message: '请选择协议' }],
          })(
            <Select
              showSearch
              optionFilterProp="title"
              placeholder="请选择协议"
              loading={queryAllProtocolsLoading}
            >
              {allL7ProtocolsList?.map((item) => (
                <Select.Option value={item?.protocolId} title={item?.nameText}>
                  {item?.nameText}
                </Select.Option>
              ))}
            </Select>,
          )}
        </FormItem>
        <FormItem key="ipProtocol" {...formLayout} label="传输层协议">
          {form.getFieldDecorator('ipProtocol', {
            initialValue: detail.ipProtocol,
            rules: [{ required: true, message: '请选择传输层协议' }],
          })(
            <Select placeholder="请选择传输层协议">
              {IP_PROTOCOL_LIST.map((item) => (
                <Select.Option value={item.toLocaleLowerCase()}>{item}</Select.Option>
              ))}
            </Select>,
          )}
        </FormItem>
        <FormItem key="port" {...formLayout} label="端口" extra="标准端口范围是: 0 ~ 65535">
          {form.getFieldDecorator('port', {
            initialValue: detail.port,
            validateFirst: true,
            rules: [{ required: true, message: '请输入端口' }],
          })(<InputNumber style={{ width: 200 }} min={1} max={65535} placeholder="请输入端口" />)}
        </FormItem>
        <FormItem key="source" {...formLayout} label="来源" style={{ display: 'none' }}>
          {form.getFieldDecorator('source', {
            initialValue: STANDARD_PROTOCOL_SOURCE_CUSTOM,
            rules: [{ required: true }],
          })(<Input />)}
        </FormItem>
        <FormItem wrapperCol={{ span: 12, offset: 4 }}>
          <Button
            style={{ marginRight: 10 }}
            type="primary"
            htmlType="submit"
            loading={submitLoading}
          >
            保存
          </Button>
          <Button onClick={() => this.handleGoListPage()}>返回</Button>
        </FormItem>
      </Form>
    );
  }
}

export default StandardProtocolForm;
