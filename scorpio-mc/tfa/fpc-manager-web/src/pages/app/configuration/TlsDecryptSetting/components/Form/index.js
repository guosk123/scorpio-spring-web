import React, { PureComponent } from 'react';
import { UploadOutlined } from '@ant-design/icons';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Input, Button, Select, InputNumber, Upload, message } from 'antd';
import { history } from 'umi';
import { connect } from 'dva';
import PropTypes from 'prop-types';
import { createConfirmModal, updateConfirmModal, checkIPv4AndIPv6 } from '@/utils/utils';

const FormItem = Form.Item;

const formLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 16 },
};

/**
 * 协议
 */
//  'MYSQL', 'TNS'
export const TLS_PROTOCOL_LIST = ['HTTP', 'SMTP', 'IMAP', 'POP3'];

@Form.create()
@connect(({ loading }) => ({
  submitLoading:
    loading.effects['tlsDecryptSettingModel/createTlsDecryptSetting'] ||
    loading.effects['tlsDecryptSettingModel/updateTlsDecryptSetting'],
}))
class TlsDecryptSettingForm extends PureComponent {
  static propTypes = {
    detail: PropTypes.object,
    operateType: PropTypes.oneOf(['CREATE', 'UPDATE']).isRequired,
  };

  static defaultProps = {
    detail: {},
  };

  constructor(props) {
    super(props);
    this.createConfirmModal = createConfirmModal.bind(this);
    this.updateConfirmModal = updateConfirmModal.bind(this);

    const { detail } = props;
    this.state = {
      fileList: detail.certHash
        ? [{ uid: detail.certHash, name: `私钥文件: ${detail.certHash}`, oldFile: true }]
        : [],
    };
  }

  // 提交
  handleSubmit = (e) => {
    const { form } = this.props;
    e.preventDefault();
    form.validateFieldsAndScroll((err, fieldsValue) => {
      if (err) return;

      const { id = '', ipAddress, port, protocol } = fieldsValue;
      const { fileList } = this.state;
      const formData = new FormData();
      // 如果用户没有再上传新的文件，就不用更新了
      if (!fileList[0].oldFile) {
        formData.append('file', fileList[0]);
      }

      formData.append('ipAddress', ipAddress);
      formData.append('port', port);
      formData.append('protocol', protocol);

      if (!id) {
        this.handleCreate({ formData });
      } else {
        formData.append('_method', 'PUT');
        this.handleUpdate({ id, formData });
      }
    });
  };

  handleGoListPage = () => {
    history.goBack();
  };

  handleReset = () => {
    const { form } = this.props;
    form.resetFields();
    this.setState({
      fileList: [],
    });
  };

  handleCreate = (values) => {
    this.createConfirmModal({
      dispatchType: 'tlsDecryptSettingModel/createTlsDecryptSetting',
      values,
      onOk: this.handleGoListPage,
      onCancel: this.handleReset,
    });
  };

  handleUpdate = (values) => {
    this.updateConfirmModal({
      dispatchType: 'tlsDecryptSettingModel/updateTlsDecryptSetting',
      values,
      onOk: this.handleGoListPage,
    });
  };

  checkFile = (rule, value, callback) => {
    const { fileList } = this.state;
    if (fileList.length === 0) {
      callback('请上传密钥文件');
      return;
    }

    callback();
  };

  handleBeforeUpload = (file) => {
    const fileSplit = file.name.split('.');
    const fileType = fileSplit.pop();

    // 文件名后缀转大写
    const fileType2UpperCase = fileType.toLocaleUpperCase();
    // 1. 后缀校验：仅支持key
    if (fileType2UpperCase !== 'KEY') {
      message.error('文件后缀不支持');
      return false;
    }

    // 2. 校验文件大小
    if (file.size <= 0) {
      message.error('文件内容不能为空');
      return false;
    }

    const isLt100M = file.size / 1024 / 1024 <= 1;
    if (!isLt100M) {
      message.error('文件大小不能超过1MB');
      return false;
    }

    this.setState(
      {
        fileList: [file],
      },
      () => {
        // 重新触发校验
        const { form } = this.props;
        form.validateFields(['file'], { force: true });
      },
    );
    return false;
  };

  handleRemoveFile = (file) => {
    const { fileList } = this.state;
    const index = fileList.indexOf(file);
    const newFileList = fileList.slice();
    newFileList.splice(index, 1);
    this.setState(
      {
        fileList: newFileList,
      },
      () => {
        // 重新触发校验
        const { form } = this.props;
        form.validateFields(['file'], { force: true });
      },
    );
  };

  render() {
    const { fileList } = this.state;
    const { form, submitLoading, detail = {} } = this.props;

    const uploadProps = {
      disabled: submitLoading,
      beforeUpload: this.handleBeforeUpload,
      onRemove: this.handleRemoveFile,
      accept: '.key',
      fileList,
    };

    return (
      <Form onSubmit={this.handleSubmit}>
        <FormItem key="id" {...formLayout} label="id" style={{ display: 'none' }}>
          {form.getFieldDecorator('id', {
            initialValue: detail.id,
          })(<Input placeholder="请输入" />)}
        </FormItem>
        <FormItem key="ipAddress" {...formLayout} label="IP地址" extra="允许输入IPv4或IPv6">
          {form.getFieldDecorator('ipAddress', {
            initialValue: detail.ipAddress,
            validateFirst: true,
            rules: [
              { required: true, whitespace: true, message: '请输入IP地址' },
              { validator: checkIPv4AndIPv6 },
            ],
          })(<Input placeholder="请输入IP地址" />)}
        </FormItem>
        <FormItem key="protocol" {...formLayout} label="协议">
          {form.getFieldDecorator('protocol', {
            initialValue: detail.protocol,
            validateFirst: true,
            rules: [{ required: true, message: '请选择协议' }],
          })(
            <Select placeholder="请选择协议">
              {TLS_PROTOCOL_LIST.map((item) => (
                <Select.Option value={item.toLocaleLowerCase()}>{item}</Select.Option>
              ))}
            </Select>,
          )}
        </FormItem>
        <FormItem key="port" {...formLayout} label="端口" extra="标准端口范围是: 1 ~ 65535">
          {form.getFieldDecorator('port', {
            initialValue: detail.port,
            validateFirst: true,
            rules: [{ required: true, message: '请输入端口' }],
          })(<InputNumber style={{ width: 200 }} min={1} max={65535} placeholder="请输入端口" />)}
        </FormItem>
        <FormItem key="file" {...formLayout} label="私钥文件" required>
          {form.getFieldDecorator('file', {
            validateFirst: true,
            rules: [{ validator: this.checkFile }],
          })(
            <Upload {...uploadProps}>
              <Button>
                <UploadOutlined /> 上传
              </Button>
            </Upload>,
          )}
          <span className="ant-form-text">
            文件后缀为 <b>key</b>，且文件大小不能超过 <b>1MB</b>。
          </span>
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

export default TlsDecryptSettingForm;
