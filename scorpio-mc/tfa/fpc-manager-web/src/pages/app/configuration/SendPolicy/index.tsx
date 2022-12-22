import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { history } from 'umi';
import { Button, Divider, message, Popconfirm, Space, Switch, Table } from 'antd';
import type { ColumnsType } from 'antd/lib/table';
import type { ISendPolicy } from './typing';
import { changeSendPolicyState, deleteSendPolicy, querySendPolicy } from './service';
import { useEffect, useState } from 'react';
import type { ITransmitRule } from '../SendUpRules/typing';
import { queryTransmitRules } from '../SendUpRules/service';
import { queryExternalReceiver } from './component/SendPolicyForm/service';
import { queryPcapTaskInfo } from '../../analysis/OfflinePcapAnalysis/service';
import { queryAllNetworks } from '../Network/service';
import ConnectCmsState from '../components/ConnectCmsState';

export default function SendPolicy() {
  const [tableData, setTableData] = useState<ISendPolicy[]>([]);
  const [loading, setLoading] = useState<boolean>(false);

  const [sendUpRules, setSendUpRules] = useState<ITransmitRule[]>([]);
  const [externalReceivers, setExternalReceivers] = useState<Record<string, any>>({});

  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);

  const fetchData = async () => {
    setLoading(true);
    const { success, result } = await querySendPolicy();
    if (success) {
      const { result: allNetworks } = await queryAllNetworks();
      const policyList = [];
      for (const policy of result) {
        const list = policy?.quote?.split(',').filter((f: any) => f) || [];
        const res = [];
        for (const i of list) {
          const network = (allNetworks || []).find((n: any) => n?.id === i);
          if (network) {
            res.push(network?.name);
          } else {
            const { success, result } = await queryPcapTaskInfo(i);
            if (success) {
              res.push(result?.name);
            }
          }
        }
        policyList.push({
          ...policy,
          quoteText: res.filter((f) => f).join(','),
        });
      }
      setTableData(policyList);
    }
    setLoading(false);
  };

  useEffect(() => {
    (async () => {
      const { success, result } = await queryTransmitRules();
      if (success) {
        setSendUpRules(result);
      }
    })();
    (async () => {
      const { success, result } = await queryExternalReceiver();
      if (success) {
        setExternalReceivers(result);
      }
    })();
    setTimeout(() => {
      fetchData();
    });
  }, []);

  const columns: ColumnsType<ISendPolicy> = [
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
      title: '服务器',
      align: 'center',
      dataIndex: 'externalReceiverId',
      key: 'externalReceiverId',
      ellipsis: true,
      render: (_, record) => {
        for (const key in externalReceivers) {
          const receivers = externalReceivers[key];
          for (const { id, name } of receivers) {
            if (id === record.externalReceiverId) {
              return name;
            }
          }
        }
        return record?.externalReceiverId || '';
      },
    },
    {
      title: '外发规则',
      align: 'center',
      dataIndex: 'sendRuleId',
      key: 'sendRuleId',
      render: (_, record) => {
        return sendUpRules.find((r) => r?.id === record?.sendRuleId)?.name || record?.sendRuleId;
      },
    },
    {
      title: '引用',
      align: 'center',
      dataIndex: 'quoteText',
      key: 'quoteText',
    },
    {
      title: '状态',
      align: 'center',
      dataIndex: 'state',
      key: 'state',
      width: '80px',
      render: (_, record) => {
        return (
          <Switch
            disabled={cmsConnectFlag || record?.quote?.length > 0}
            checkedChildren="启用"
            unCheckedChildren="禁用"
            defaultChecked={record?.state === '1'}
            onChange={async (e) => {
              const { success } = await changeSendPolicyState(record?.id, e);
              if (!success) {
                message.error('操作失败!');
                fetchData();
              }
            }}
          />
        );
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
              history.push(`/configuration/third-party/send-policy/${record?.id}/update`);
            }}
            disabled={cmsConnectFlag}
          >
            编辑
          </Button>
          <Divider type="vertical" />
          <Popconfirm
            title={'确定删除吗?'}
            onConfirm={async () => {
              const { success } = await deleteSendPolicy(record?.id);
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
              history.push('/configuration/third-party/send-policy/create');
            }}
            disabled={cmsConnectFlag}
          >
            新建
          </Button>
        </Space>
      </div>
      <Table
        loading={loading}
        rowKey={'id'}
        size="small"
        pagination={false}
        bordered
        columns={columns}
        dataSource={tableData}
      />
    </>
  );
}
