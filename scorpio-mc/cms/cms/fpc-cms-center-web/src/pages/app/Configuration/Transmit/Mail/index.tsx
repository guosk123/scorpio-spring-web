import type { ConnectState } from '@/models/connect';
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import { Button, Popconfirm } from 'antd';
import type { Dispatch } from 'dva';
import type { ColumnProps } from 'antd/lib/table';
import type { ITransmitMail } from '../typings';
import { connect } from 'dva';
import { useEffect, useState } from 'react';
import StandardTable from '@/components/StandardTable';
import { history } from 'umi';

const Index: React.FC<{
  dispatch: Dispatch;
  allTransmitMail: ITransmitMail[];
  loading: boolean;
}> = ({ allTransmitMail, dispatch, loading }) => {
  // 表格列定义
  const tableColumns: ColumnProps<ITransmitMail>[] = [
    {
      title: '邮件主题',
      dataIndex: 'mailTitle',
      ellipsis: true,
      width: 300,
    },
    {
      title: '收件人',
      dataIndex: 'receiver',
      ellipsis: true,
    },
    {
      title: '抄送人',
      dataIndex: 'cc',
      align: 'center',
      ellipsis: true,
    },
    {
      title: '操作',
      align: 'center',
      dataIndex: 'operate',
      ellipsis: true,
      width: 150,
      render: (_, record) => {
        return (
          <>
            <Button
              type="link"
              size="small"
              onClick={() => {
                history.push(`/configuration/transmit/mail/${record.id}/update`);
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
      type: 'transmitModel/batchDeleteTransmitMail',
      payload: selectedRowKeys.join(','),
    });
  };
  useEffect(() => {
    dispatch({
      type: 'transmitModel/queryAllTransmitMail',
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
            history.push('/configuration/transmit/mail/create');
          }}
        >
          新建
        </Button>
      </div>
      <StandardTable
        rowKey="id"
        loading={loading}
        columns={tableColumns}
        data={{ list: allTransmitMail || [], pagination: false }}
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

export default connect(({ transmitModel: { allTransmitMail }, loading }: ConnectState) => ({
  allTransmitMail,
  loading: loading.effects['transmitModel/queryAllTransmitMail'],
}))(Index as any);
