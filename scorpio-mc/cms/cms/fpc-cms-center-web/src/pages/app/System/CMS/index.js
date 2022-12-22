import React, { Component } from 'react';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Input, Button, Spin, Modal, Badge, Switch } from 'antd';
import { connect } from 'dva';
import { ipV4Regex } from '@/utils/utils';
import { BOOL_YES, BOOL_NO } from '@/common/dict';
import styles from './index.less';

const CONNECT_STATUS_OK = '0';
const CONNECT_STATUS_OK_COLOR = '#52c41a';

const CONNECT_STATUS_FAILED = '1';
const CONNECT_STATUS_FAILED_COLOR = '#f5222d';

@Form.create()
@connect(({ cmsModel: { settings }, loading }) => ({
  settings,
  queryLoading: loading.effects['cmsModel/queryCmsSettings'] || false,
  updateLoading: loading.effects['cmsModel/updateCmsSettings'] || false,
}))
class CMSSetting extends Component {
  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'cmsModel/queryCmsSettings',
    });
  }

  componentWillUnmount() {}

  handleSubmit = (e) => {
    const { dispatch } = this.props;
    e.preventDefault();
    const { form } = this.props;
    form.validateFields((err, values) => {
      if (err) return;

      Modal.confirm({
        title: '确定保存吗？',
        maskClosable: false,
        destroyOnClose: true,
        keyboard: false,
        autoFocusButton: true,
        onOk: () => {
          dispatch({
            type: 'cmsModel/updateCmsSettings',
            payload: { ...values, state: values.state ? BOOL_YES : BOOL_NO },
          });
        },
      });
    });
  };

  render() {
    const { form, settings, queryLoading, updateLoading } = this.props;
    const { getFieldDecorator } = form;
    const formItemLayout = {
      labelCol: { span: 6 },
      wrapperCol: { span: 14 },
    };

    return (
      <Spin spinning={queryLoading || updateLoading}>
        <Form {...formItemLayout} onSubmit={this.handleSubmit}>
          <Form.Item label="集群管理CMS开启状态">
            {getFieldDecorator('state', {
              valuePropName: 'checked',
              initialValue: settings.state === BOOL_YES,
              rules: [
                {
                  required: true,
                },
              ],
            })(<Switch checkedChildren="开启" unCheckedChildren="关闭" />)}
          </Form.Item>
          <Form.Item label="集群管理CMS IP">
            {getFieldDecorator('cmsIp', {
              initialValue: settings.cmsIp,
              rules: [
                {
                  required: true,
                  message: '集群管理CMS IP地址',
                },
                {
                  pattern: ipV4Regex,
                  message: '请填写正确的IP地址',
                },
              ],
            })(<Input placeholder="点分十进制IP地址" />)}
          </Form.Item>
          <Form.Item label="集群管理CMS Token">
            {getFieldDecorator('cmsToken', {
              initialValue: settings.cmsToken,
              rules: [
                {
                  required: true,
                  whitespace: true,
                  message: '集群管理CMS Token',
                },
              ],
            })(<Input />)}
          </Form.Item>
          <Form.Item
            label="管理CMS节点连接状态"
            className={styles.badge}
            style={{ display: settings.state === BOOL_YES ? 'block' : 'none' }}
          >
            {!settings.connectStatus && <Badge status="default" text="尚未连接" />}
            {settings.connectStatus === CONNECT_STATUS_OK && (
              <Badge
                status="success"
                text={<span style={{ color: CONNECT_STATUS_OK_COLOR }}>连接成功</span>}
              />
            )}
            {settings.connectStatus === CONNECT_STATUS_FAILED && (
              <Badge
                status="error"
                text={<span style={{ color: CONNECT_STATUS_FAILED_COLOR }}>连接异常</span>}
              />
            )}
          </Form.Item>
          <Form.Item wrapperCol={{ span: 12, offset: 6 }}>
            <Button type="primary" htmlType="submit" style={{ marginRight: 10 }}>
              保存
            </Button>
          </Form.Item>
        </Form>
      </Spin>
    );
  }
}

export default CMSSetting;
