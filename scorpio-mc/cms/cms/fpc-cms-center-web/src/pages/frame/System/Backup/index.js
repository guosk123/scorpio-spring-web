import config from '@/common/applicationConfig';
import { BOOL_NO, BOOL_YES, SYSTEM_LOG_CATEGORY_SYSTEM_BACKUP } from '@/common/dict';
import { PAGE_DEFAULT_SIZE } from '@/common/app';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { CopyOutlined } from '@ant-design/icons';
import { Button, Card, InputNumber, message, Modal, Skeleton, Switch, Tooltip, Table } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import React, { PureComponent } from 'react';
import { CopyToClipboard } from 'react-copy-to-clipboard';

const logColumns = [
  {
    title: '时间',
    dataIndex: 'ariseTime',
    align: 'center',
    render: (ariseTime) => moment(ariseTime).format('YYYY-MM-DD HH:mm:ss'),
  },
  {
    title: '描述',
    dataIndex: 'content',
    ellipsis: true,
  },
];

@Form.create()
@connect(
  ({
    archiveAndBackupModel: { backupSettings },
    logModel: { backupLogs, pagination },
    loading,
  }) => ({
    backupSettings,
    backupLogs,
    pagination,
    logLoading: loading.effects['logModel/queryBackupLogs'] || false,
    queryLoading: loading.effects['archiveAndBackupModel/queryBackupSettings'] || false,
    updateLoading: loading.effects['archiveAndBackupModel/updateBackupSettings'] || false,
  }),
)
class Backup extends PureComponent {
  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'archiveAndBackupModel/queryBackupSettings',
    });

    this.getLogs();
  }

  getLogs = (page = 1, pageSize = PAGE_DEFAULT_SIZE) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'logModel/queryBackupLogs',
      payload: {
        page,
        pageSize,
        category: SYSTEM_LOG_CATEGORY_SYSTEM_BACKUP,
      },
    });
  };

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
            type: 'archiveAndBackupModel/updateBackupSettings',
            payload: {
              ...values,
              backupSettingState: values.backupSettingState ? BOOL_YES : BOOL_NO,
            },
          });
        },
      });
    });
  };

  handleTableChange = (pageNumber, pageSize) => {
    this.getLogs(pageNumber, pageSize);
  };

  render() {
    const {
      form,
      backupSettings,
      queryLoading,
      updateLoading,
      logLoading,
      backupLogs,
      pagination,
    } = this.props;
    const { getFieldDecorator, getFieldValue } = form;
    const formItemLayout = {
      labelCol: { span: 4 },
      wrapperCol: { span: 14 },
    };

    return (
      <Card bordered={false}>
        <Skeleton active loading={queryLoading}>
          <Form {...formItemLayout} onSubmit={this.handleSubmit}>
            <Form.Item label="自动备份" style={{ marginBottom: 10 }}>
              {getFieldDecorator('backupSettingState', {
                valuePropName: 'checked',
                initialValue: backupSettings.backupSettingState === BOOL_YES,
                rules: [
                  {
                    required: false,
                    message: '请选择是否自动备份',
                  },
                ],
              })(<Switch checkedChildren="启用" unCheckedChildren="关闭" />)}
            </Form.Item>
            <Form.Item
              label="保留备份文件数"
              extra={
                <span>
                  文件系统中将保留最新的{getFieldValue('backupFilesNumber')}
                  个备份文件
                </span>
              }
              style={{
                display: getFieldValue('backupSettingState') ? 'flex' : 'none',
              }}
            >
              {getFieldDecorator('backupFilesNumber', {
                initialValue: backupSettings.backupFilesNumber,
                rules: [
                  {
                    required: true,
                    message: '请填写保留备份文件数',
                  },
                ],
              })(<InputNumber precision={0} min={1} max={10} />)}
              <span className="ant-form-text"> 个（最多保留10个）</span>
            </Form.Item>
            <Form.Item label="备份路径" style={{ marginBottom: 10 }}>
              <span className="ant-form-text">
                <code>{config.BACKUP_PATH}</code>
              </span>
              <span className="ant-form-text">
                {' '}
                <CopyToClipboard
                  text={config.BACKUP_PATH}
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

        {/* 备份日志 */}
        <Table
          style={{ width: '80%', margin: '0 auto' }}
          size="small"
          bordered
          title={() => '备份日志'}
          rowKey="id"
          loading={logLoading}
          columns={logColumns}
          data={{ list: backupLogs, pagination }}
          onChange={this.handleTableChange}
        />
      </Card>
    );
  }
}

export default Backup;
