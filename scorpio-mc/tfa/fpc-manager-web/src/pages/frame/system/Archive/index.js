import { ALARM_ARCHIVE_PATH, LOG_ARCHIVE_PATH } from '@/common/applicationConfig';
import { BOOL_NO, BOOL_YES } from '@/common/dict';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { CopyOutlined } from '@ant-design/icons';
import { Button, Card, InputNumber, message, Modal, Skeleton, Switch, Tooltip } from 'antd';
import { connect } from 'dva';
import React, { PureComponent } from 'react';
import { CopyToClipboard } from 'react-copy-to-clipboard';

@Form.create()
@connect(({ archiveAndBackupModel: { archiveSettings }, loading }) => ({
  archiveSettings,
  queryLoading: loading.effects['archiveAndBackupModel/queryArchiveSettings'] || false,
  updateLoading: loading.effects['archiveAndBackupModel/updateArchiveSettings'] || false,
}))
class Archive extends PureComponent {
  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'archiveAndBackupModel/queryArchiveSettings',
    });
  }

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
            type: 'archiveAndBackupModel/updateArchiveSettings',
            payload: {
              ...values,
              archiveSettingState: values.archiveSettingState ? BOOL_YES : BOOL_NO,
            },
          });
        },
      });
    });
  };

  render() {
    const { form, archiveSettings, queryLoading, updateLoading } = this.props;
    const { getFieldDecorator, getFieldValue } = form;
    const formItemLayout = {
      labelCol: { span: 4 },
      wrapperCol: { span: 14 },
    };

    return (
      <Card bordered={false}>
        <Skeleton active loading={queryLoading}>
          <Form {...formItemLayout} onSubmit={this.handleSubmit}>
            <Form.Item label="是否启用归档" style={{ marginBottom: 10 }}>
              {getFieldDecorator('archiveSettingState', {
                valuePropName: 'checked',
                initialValue: archiveSettings.archiveSettingState === BOOL_YES,
              })(<Switch checkedChildren="启用" unCheckedChildren="关闭" />)}
            </Form.Item>
            <Form.Item
              style={{ display: getFieldValue('archiveSettingState') ? 'flex' : 'none' }}
              label="数据归档时间"
              extra={
                <ul style={{ paddingLeft: 20, listStyle: 'decimal' }}>
                  <li>{getFieldValue('archiveDays')}天前的数据将会进行归档处理</li>
                  <li>每天生成一个归档 csv 文件，存放在归档路径下</li>
                  <li>已归档的数据将不会出现在日志中心/告警中心中</li>
                  <li>最小可设置为7天，最大可设置为180天，默认为90天</li>
                </ul>
              }
            >
              {getFieldDecorator('archiveDays', {
                initialValue: archiveSettings.archiveDays,
                rules: [
                  {
                    required: true,
                    message: '请填写数据存活时间',
                  },
                ],
              })(<InputNumber precision={0} min={7} max={180} />)}
              <span className="ant-form-text"> 天</span>
            </Form.Item>
            <Form.Item label="日志归档路径" style={{ marginBottom: 10 }}>
              <span className="ant-form-text">
                <code>{LOG_ARCHIVE_PATH}</code>
              </span>
              <span className="ant-form-text">
                {' '}
                <CopyToClipboard text={LOG_ARCHIVE_PATH} onCopy={() => message.success('复制成功')}>
                  <Tooltip title="复制">
                    <CopyOutlined />
                  </Tooltip>
                </CopyToClipboard>
              </span>
            </Form.Item>
            <Form.Item label="告警归档路径" style={{ marginBottom: 10 }}>
              <span className="ant-form-text">
                <code>{ALARM_ARCHIVE_PATH}</code>
              </span>
              <span className="ant-form-text">
                {' '}
                <CopyToClipboard
                  text={ALARM_ARCHIVE_PATH}
                  onCopy={() => message.success('复制成功')}
                >
                  <Tooltip title="复制">
                    <CopyOutlined />
                  </Tooltip>
                </CopyToClipboard>
              </span>
            </Form.Item>
            <Form.Item wrapperCol={{ span: 12, offset: 4 }}>
              <Button
                type="primary"
                htmlType="submit"
                style={{ marginRight: 10 }}
                loading={updateLoading}
              >
                保存
              </Button>
            </Form.Item>
          </Form>
        </Skeleton>
      </Card>
    );
  }
}

export default Archive;
