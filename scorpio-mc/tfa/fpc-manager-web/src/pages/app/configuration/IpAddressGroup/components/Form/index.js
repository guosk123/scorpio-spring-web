import {
  createConfirmModal,
  ip2number,
  ipV4Regex,
  ipV6Regex,
  updateConfirmModal,
} from '@/utils/utils';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Button, Input } from 'antd';
import { connect } from 'dva';
import PropTypes from 'prop-types';
import React, { PureComponent } from 'react';
import { history } from 'umi';

const FormItem = Form.Item;
const { TextArea } = Input;

const formLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 16 },
};

const IP_MAX_NUMBER = 50;

@Form.create()
@connect(({ loading: { effects } }) => ({
  submitLoading:
    effects['ipAddressGroupModel/updateIpAddressGroup'] ||
    effects['ipAddressGroupModel/createIpAddressGroup'],
}))
class IpAddressGroupForm extends PureComponent {
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
  }

  // 提交
  handleSubmit = (e) => {
    const { form, operateType } = this.props;
    e.preventDefault();
    form.validateFieldsAndScroll((err, fieldsValue) => {
      if (err) return;

      const { ipAddress = '' } = fieldsValue;
      const values = {
        ...fieldsValue,
        ipAddress: ipAddress.split('\n').join(','),
      };
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
      dispatchType: 'ipAddressGroupModel/createIpAddressGroup',
      values,
      onOk: this.handleGoListPage,
      onCancel: this.handleReset,
    });
  };

  handleUpdate = (values) => {
    this.updateConfirmModal({
      dispatchType: 'ipAddressGroupModel/updateIpAddressGroup',
      values,
      onOk: this.handleGoListPage,
    });
  };

  /**
   * 检测每行输入的ip地址
   * 保证不重复即可
   * 不用管地址段之间是否有重叠
   */
  checkTextAreaIp = (rule, value, callback) => {
    if (value) {
      const passIpArr = []; // 已经检查通过的IP
      const valueArr = value.split('\n');

      try {
        if (Array.isArray(valueArr)) {
          if (valueArr.length > IP_MAX_NUMBER) {
            throw new Error(`最多支持${IP_MAX_NUMBER}个`);
          }

          valueArr.forEach((item, index) => {
            const lineText = `第${index + 1}行[${item}]: `;
            if (!item) {
              throw new Error(`${lineText}不能为空`);
            }

            // IP网段
            if (item.indexOf('/') > -1) {
              const [ip, mask] = item.split('/');

              if (!ipV4Regex.test(ip) && !ipV6Regex.test(ip)) {
                throw new Error(`${lineText}请输入正确的IP/IP段`);
              }

              if (ipV4Regex.test(ip) && (!mask || isNaN(mask) || mask <= 0 || mask > 32)) {
                throw new Error(`${lineText}请输入正确的IP v4网段。例，192.168.1.2/24`);
              }

              if (ipV6Regex.test(ip) && (!mask || isNaN(mask) || mask <= 0 || mask > 128)) {
                throw new Error(`${lineText}请输入正确的IP v6网段。例，2001:250:6EFA::/48`);
              }
            }

            // IP组
            else if (item.indexOf('-') > -1) {
              const ips = item.split('-');
              if (ips.length !== 2) {
                throw new Error(`${lineText}请输入正确的IP地址段。例，192.168.1.1-192.168.1.50`);
              }

              const [ip1, ip2] = ips;

              // 2个ipV4
              if (!ipV4Regex.test(ip1) && !ipV4Regex.test(ip2)) {
                throw new Error(`${lineText}请输入正确的IP地址段。例，192.168.1.1-192.168.1.50`);
              }
              // 2个都是ipV4的校验下大小关系
              if (ipV4Regex.test(ip1) && ipV4Regex.test(ip2)) {
                // 校验前后2个ip的大小关系
                const ip1Number = ip2number(ip1);
                const ip2Number = ip2number(ip2);

                // 起止地址是否符合大小要求
                if (ip1Number >= ip2Number) {
                  throw new Error(`${lineText}截止IP必须大于开始IP`);
                }
              } else if (!ipV6Regex.test(ip1) && !ipV6Regex.test(ip2)) {
                // ip v6
                throw new Error(`${lineText}请输入正确的IP地址段。例，192.168.1.1-192.168.1.50`);
              }
            } else if (!ipV4Regex.test(item) && !ipV6Regex.test(item)) {
              throw new Error(`${lineText}请输入正确的IP/IP段`);
            }

            // 是否重复了
            if (passIpArr.indexOf(item) !== -1) {
              throw new Error(`${lineText}已重复`);
            }
            passIpArr.push(item);
          });
        }
      } catch (e) {
        callback(e);
      } finally {
        callback();
      }
    } else {
      callback();
    }
  };

  render() {
    const { form, submitLoading, detail = {} } = this.props;
    return (
      <Form onSubmit={this.handleSubmit}>
        <FormItem key="id" {...formLayout} label="id" style={{ display: 'none' }}>
          {form.getFieldDecorator('id', {
            initialValue: detail.id,
          })(<Input placeholder="请输入" />)}
        </FormItem>
        <FormItem key="name" {...formLayout} label="IP地址组名称">
          {form.getFieldDecorator('name', {
            initialValue: detail.name,
            rules: [
              { required: true, whitespace: true, message: '请输入IP地址组名称' },
              {
                max: 30,
                message: '最多可输入30个字符',
              },
            ],
          })(<Input placeholder="请输入IP地址组名称" />)}
        </FormItem>
        <FormItem
          key="ipAddress"
          {...formLayout}
          label="IP/IP网段"
          extra={
            <ul style={{ paddingLeft: 20, listStyle: 'circle' }}>
              <li>每行输入一种IP地址，最多支持{IP_MAX_NUMBER}个；</li>
              <li>可以输入【A.B.C.D】格式的IP地址；</li>
              <li>或输入【A.B.C.D/掩码长度】格式的IP网段；</li>
              <li>或输入【A.B.C.D-E.F.G.H】格式的IP组，请确保 E.F.G.H &gt;= A.B.C.D。</li>
            </ul>
          }
        >
          {form.getFieldDecorator('ipAddress', {
            initialValue: detail.ipAddress && detail.ipAddress.replace(/,/g, '\n'),
            validateFirst: true,
            validateTrigger: ['onChange', 'onBlur'],
            rules: [
              { required: true, message: '请输入IP/IP网段' },
              {
                validator: this.checkTextAreaIp,
              },
            ],
          })(<Input.TextArea rows={5} placeholder="请输入IP/IP网段" />)}
        </FormItem>
        <FormItem key="description" {...formLayout} label="描述信息">
          {form.getFieldDecorator('description', {
            initialValue: detail.description || '',
            rules: [
              { required: false, message: '请输入描述信息' },
              { max: 255, message: '最多可输入255个字符' },
            ],
          })(<TextArea rows={4} placeholder="请输入描述信息" />)}
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

export default IpAddressGroupForm;
