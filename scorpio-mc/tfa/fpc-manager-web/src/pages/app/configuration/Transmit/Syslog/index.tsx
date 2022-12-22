import EllipsisCom from '@/components/EllipsisCom';
import type { ConnectState } from '@/models/connect';
import { DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Popconfirm, Table } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import type { Dispatch } from 'dva';
import { connect } from 'dva';
import { useEffect, useState } from 'react';
import { history } from 'umi';
import type { ITransmitSyslog } from '../typings';
import { ESyslogTransmitName } from '../typings';

const Index: React.FC<{
  dispatch: Dispatch;
  allTransmitSyslog: ITransmitSyslog[];
  loading: boolean;
}> = ({ allTransmitSyslog, dispatch, loading }) => {
  // 表格列定义
  const tableColumns: ColumnProps<ITransmitSyslog>[] = [
    {
      title: '#',
      dataIndex: 'index',
      align: 'center',
      width: 60,
      render: (text, record, index) => {
        return <EllipsisCom>{index + 1}</EllipsisCom>;
      },
    },
    {
      title: '规则名称',
      dataIndex: 'name',
      render: (text) => {
        return <EllipsisCom>{text}</EllipsisCom>;
      },
    },
    {
      title: 'Syslog地址',
      dataIndex: 'syslogServerAddress',
      ellipsis: true,
      render: (text) => {
        return <EllipsisCom>{text}</EllipsisCom>;
      },
    },
    {
      title: '发送方式',
      dataIndex: 'sendType',
      align: 'center',
      render: (value) => {
        return <EllipsisCom>{ESyslogTransmitName[value]}</EllipsisCom>;
      },
    },
    {
      title: '发送内容',
      dataIndex: 'transmitContent',
      align: 'center',
      render: (_, record) => {
        const contentList = [];
        if (record.networkAlertContent !== '') {
          contentList.push('网络告警');
        }
        if (record.serviceAlertContent !== '') {
          contentList.push('业务告警');
        }
        return <EllipsisCom>{contentList.join(',')}</EllipsisCom>;
      },
    },
    {
      title: '操作',
      align: 'center',
      dataIndex: 'operate',
      fixed: 'right',
      render: (_, record) => {
        return (
          <>
            <Button
              type="link"
              size="small"
              onClick={() => {
                history.push(`/configuration/third-party/syslog/${record.id}/update`);
              }}
            >
              <EllipsisCom>编辑</EllipsisCom>
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
  }, [dispatch]);

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
            history.push('/configuration/third-party/syslog/create');
          }}
        >
          新建
        </Button>
      </div>
      <Table
        rowKey="id"
        loading={loading}
        columns={tableColumns}
        size="small"
        bordered
        dataSource={allTransmitSyslog}
        pagination={false}
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
