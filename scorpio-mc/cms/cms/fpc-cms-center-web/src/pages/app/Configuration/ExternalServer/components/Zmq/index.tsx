import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Divider, message, Popconfirm, Space, Table } from 'antd';
import type { ColumnsType } from 'antd/lib/table';
import { useEffect, useState } from 'react';
import { history } from 'umi';
import { deleteTransmitZmq, queryTransmitZmq } from './service';
import type { IMailType } from './typing';

export default function Zmq() {
  const [tableData, setTableData] = useState<IMailType[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const fetchData = async () => {
    setLoading(true);
    const { success, result } = await queryTransmitZmq();
    if (success) {
      setTableData(result);
    }
    setLoading(false);
  };

  const columns: ColumnsType<IMailType> = [
    {
      title: '#',
      align: 'center',
      dataIndex: 'index',
      width: 60,
      fixed: 'left',
      render: (text, record, index) => {
        return <>{index + 1}</>;
      },
    },
    {
      title: '名称',
      align: 'center',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '协议',
      align: 'center',
      dataIndex: 'protocol',
      key: 'protocol',
    },
    {
      title: 'IP',
      align: 'center',
      dataIndex: 'zmqServerIpAddress',
      key: 'zmqServerIpAddress',
    },
    {
      title: '端口',
      align: 'center',
      dataIndex: 'zmqServerPort',
      key: 'zmqServerPort',
    },
    {
      title: '操作',
      align: 'center',
      key: 'action',
      width: '150px',
      render: (_: any, record: any) => (
        <>
          <Button
            type="link"
            size="small"
            onClick={() => {
              history.push(`/configuration/transmit/external-server/zmq/${record?.id}/update`);
            }}
          >
            编辑
          </Button>
          <Divider type='vertical'/>
          <Popconfirm
            title={'确定删除吗?'}
            onConfirm={async () => {
              const { success } = await deleteTransmitZmq(record?.id);
              if (success) {
                message.success('删除成功!');
                fetchData();
                return;
              }
              message.error('删除失败!');
            }}
          >
            <Button type="link" size="small" style={{ color: 'red' }}>
              删除
            </Button>
          </Popconfirm>
        </>
      ),
    },
  ];

  useEffect(() => {
    fetchData();
  }, []);

  return (
    <>
      <div style={{ marginBottom: 10, textAlign: 'right' }}>
        <Space>
          <Button
            key="create"
            type="primary"
            icon={<ReloadOutlined />}
            onClick={() => {
              fetchData();
            }}
            loading={loading}
          >
            刷新
          </Button>

          <Button
            key="create"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              history.push('/configuration/transmit/external-server/zmq/create');
            }}
          >
            新建
          </Button>
        </Space>
      </div>
      <Table
        loading={loading}
        size="small"
        pagination={false}
        bordered
        columns={columns}
        dataSource={tableData}
      />
    </>
  );
}
