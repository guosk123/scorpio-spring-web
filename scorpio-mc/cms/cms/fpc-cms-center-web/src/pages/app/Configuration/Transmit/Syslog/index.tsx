import type { ConnectState } from '@/models/connect';
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import { Button, Popconfirm } from 'antd';
import type { Dispatch } from 'dva';
import type { ColumnProps } from 'antd/lib/table';
import type { ITransmitSyslog } from '../typings';
import { ESyslogTransmitName } from '../typings';
import { connect } from 'dva';
import { useEffect, useState } from 'react';
import StandardTable from '@/components/StandardTable';
import { history } from 'umi';

const Index: React.FC<{
  dispatch: Dispatch;
  allTransmitSyslog: ITransmitSyslog[];
  loading: boolean;
}> = ({ allTransmitSyslog, dispatch, loading }) => {
  // 表格列定义
  const tableColumns: ColumnProps<ITransmitSyslog>[] = [
    {
      title: '规则名称',
      dataIndex: 'name',
      ellipsis: true,
    },
    {
      title: 'Syslog地址',
      dataIndex: 'syslogServerAddress',
      ellipsis: true,
    },
    {
      title: '发送方式',
      dataIndex: 'sendType',
      align: 'center',
      ellipsis: true,
      render: (value) => {
        return ESyslogTransmitName[value];
      },
    },
    {
      title: '发送内容',
      dataIndex: 'transmitContent',
      align: 'center',
      ellipsis: true,
      render: (_, record) => {
        const contentList = [];
        if (record.networkAlertContent !== '') {
          contentList.push('网络告警');
        }
        if (record.serviceAlertContent !== '') {
          contentList.push('业务告警');
        }
        return contentList.join(',');
      },
    },
    {
      title: '操作',
      align: 'center',
      dataIndex: 'operate',
      ellipsis: true,
      fixed: 'right',
      render: (_, record) => {
        return (
          <>
            <Button
              type="link"
              size="small"
              onClick={() => {
                history.push(`/configuration/transmit/syslog/${record.id}/update`);
              }}
            >
              编辑
            </Button>
          </>
        );
      },
    },
  ];
  /** 批量选择 */
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);
  /** 批量删除 */
  const batchDelete = () => {
    setSelectedRowKeys([]);
    dispatch({
      type: 'transmitModel/batchDeleteTransmitSyslog',
      payload: selectedRowKeys.join(','),
    });
  };
  /** 初始化表格 */
  useEffect(() => {
    dispatch({
      type: 'transmitModel/queryAllTransmitSyslog',
    });
  }, []);

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'flex-end', paddingBottom: 10 }}>
        <Popconfirm
          title="是否确定删除?"
          onConfirm={batchDelete}
          disabled={selectedRowKeys.length === 0}
        >
          <Button
            type="primary"
            icon={<DeleteOutlined />}
            danger
            style={{ marginRight: '10px' }}
            disabled={selectedRowKeys.length === 0}
          >
            删除
          </Button>
        </Popconfirm>
        <Button
          icon={<PlusOutlined />}
          type="primary"
          onClick={() => {
            history.push('/configuration/transmit/syslog/create');
          }}
        >
          新建
        </Button>
      </div>
      <StandardTable
        rowKey="id"
        loading={loading}
        columns={tableColumns}
        data={{ list: allTransmitSyslog, pagination: false }}
        rowSelection={
          {
            selectedRowKeys,
            onChange: (values: any) => {
              setSelectedRowKeys(values);
            },
          } as any
        }
      />
    </div>
  );
};

export default connect(({ transmitModel: { allTransmitSyslog }, loading }: ConnectState) => ({
  allTransmitSyslog,
  loading: loading.effects['transmitModel/queryAllTransmitSyslog'],
}))(Index as any);
