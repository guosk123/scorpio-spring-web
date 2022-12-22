import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Divider, message, Popconfirm, Space, Table } from 'antd';
import type { ColumnsType } from 'antd/lib/table';
import { useEffect, useState } from 'react';
import { history } from 'umi';
import ConnectCmsState from '../components/ConnectCmsState';
import { deleteSendUpRules, queryTransmitRules } from './service';
import type { ITransmitRule } from './typing';

export default function TransmitRules() {
  const [tableData, setTableData] = useState<ITransmitRule[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);
  const fetchData = async () => {
    setLoading(true);
    const { success, result } = await queryTransmitRules();
    if (success) {
      setTableData(result);
    }
    setLoading(false);
  };

  const columns: ColumnsType<ITransmitRule> = [
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
      title: '描述',
      align: 'center',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '发送方式',
      align: 'center',
      dataIndex: 'sendingMethod',
      key: 'sendingMethod',
      width: '100px',
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
              history.push(`/configuration/third-party/sendup-rules/${record?.id}/update`);
            }}
            disabled={cmsConnectFlag}
          >
            编辑
          </Button>
          <Divider type="vertical" />
          <Popconfirm
            title={'确定删除吗?'}
            onConfirm={async () => {
              const { success } = await deleteSendUpRules(record?.id);
              if (success) {
                message.success('删除成功!');
                fetchData();
                return;
              }
              message.error('删除失败!');
            }}
            disabled={cmsConnectFlag}
          >
            <Button
              disabled={cmsConnectFlag}
              type="link"
              size="small"
              style={{ color: cmsConnectFlag ? 'rgba(0, 0, 0, 0.25)' : 'red' }}
            >
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
      <ConnectCmsState onConnectFlag={setCmsConnectFlag} />
      <div style={{ marginBottom: 10, textAlign: 'right' }}>
        <Space>
          <Button
            key="create"
            type="primary"
            icon={<ReloadOutlined />}
            loading={loading}
            onClick={() => {
              fetchData();
            }}
          >
            刷新
          </Button>

          <Button
            key="create"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              history.push('/configuration/third-party/sendup-rules/create');
            }}
            disabled={cmsConnectFlag}
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
