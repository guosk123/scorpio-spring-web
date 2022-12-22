import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Divider, message, Popconfirm, Space, Tag } from 'antd';
import type { ColumnsType } from 'antd/lib/table';
import Table from 'antd/lib/table';
import { history } from 'umi';
import type { IKafkaType } from '../Kafka/typing';
import { ECertification } from '../Kafka/typing';
import { useEffect, useState } from 'react';
import { deleteTransmitKafka, queryTransmitkafka } from './service';
import ConnectCmsState from '../../../components/ConnectCmsState';

export default function Kafka() {
  const [tableData, setTableData] = useState<IKafkaType[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);
  const fetchData = async () => {
    setLoading(true);
    const { success, result } = await queryTransmitkafka();
    if (success) {
      setTableData(result);
    }
    setLoading(false);
  };

  const columns: ColumnsType<IKafkaType> = [
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
      title: '认证方式',
      dataIndex: 'kerberosCertification',
      key: 'kerberosCertification',
      align: 'center',
      render: (_: any, record: any) => {
        return record?.kerberosCertification === ECertification.KERBEROS ? 'KERBEROS' : '无';
      },
    },
    {
      title: '地址',
      dataIndex: 'kafkaServerAddress',
      key: 'kafkaServerAddress',
      align: 'center',
      render: (_: any, record: any) => {
        return record?.kafkaServerAddress?.split(',').map((address: string) => {
          return <Tag key={address}>{address}</Tag>;
        });
      },
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
              history.push(`/configuration/third-party/external-server/kafka/${record?.id}/update`);
            }}
            disabled={cmsConnectFlag}
          >
            编辑
          </Button>
          <Divider type="vertical" />
          <Popconfirm
            title={'确定删除吗?'}
            disabled={cmsConnectFlag}
            onConfirm={async () => {
              const { success } = await deleteTransmitKafka(record?.id);
              if (success) {
                message.success('删除成功!');
                fetchData();
                return;
              }
              message.error('删除失败!');
            }}
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
              history.push('/configuration/third-party/external-server/kafka/create');
            }}
            disabled={cmsConnectFlag}
          >
            新建
          </Button>
        </Space>
      </div>
      <Table loading={loading} bordered columns={columns} dataSource={tableData} size="small" />
    </>
  );
}
