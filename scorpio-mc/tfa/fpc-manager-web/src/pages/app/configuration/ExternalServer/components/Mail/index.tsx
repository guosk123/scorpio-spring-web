import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Divider, message, Popconfirm, Space, Table } from 'antd';
import type { ColumnsType } from 'antd/lib/table';
import { useEffect, useState } from 'react';
import { history } from 'umi';
import ConnectCmsState from '../../../components/ConnectCmsState';
import { deleteTransmitMail, queryTransmitMail } from './service';
import type { IMailType } from './typing';

export default function Mail() {
  const [tableData, setTableData] = useState<IMailType[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);
  const fetchData = async () => {
    setLoading(true);
    const { success, result } = await queryTransmitMail();
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
      dataIndex: 'name',
      align: 'center',
      key: 'name',
    },
    {
      title: '收件人',
      dataIndex: 'receiver',
      align: 'center',
      key: 'receiver',
    },
    {
      title: '主题',
      dataIndex: 'mailTitle',
      align: 'center',
      key: 'mailTitle',
    },
    {
      title: '抄送',
      dataIndex: 'cc',
      align: 'center',
      key: 'cc',
    },
    {
      title: '密送',
      dataIndex: 'bcc',
      align: 'center',
      key: 'bcc',
    },
    {
      title: '操作',
      key: 'action',
      width: '150px',
      align: 'center',
      render: (_: any, record: any) => (
        <>
          <Button
            type="link"
            size="small"
            onClick={() => {
              history.push(`/configuration/third-party/external-server/mail/${record?.id}/update`);
            }}
            disabled={cmsConnectFlag}
          >
            编辑
          </Button>
          <Divider type="vertical" />
          <Popconfirm
            title={'确定删除吗?'}
            onConfirm={async () => {
              const { success } = await deleteTransmitMail(record?.id);
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
              history.push('/configuration/third-party/external-server/mail/create');
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
