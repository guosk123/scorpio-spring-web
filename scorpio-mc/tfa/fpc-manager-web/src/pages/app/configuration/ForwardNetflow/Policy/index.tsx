import { getTablePaginationDefaultSettings } from '@/common/app';
import type { ConnectState } from '@/models/connect';
import { queryDeviceNetifs } from '@/services/app/deviceNetif';
import { convertBandwidth, parseArrayJson } from '@/utils/utils';
import { useSafeState } from 'ahooks';
import type { TableColumnProps } from 'antd';
import { Button, Card, Space, Table } from 'antd';
import { useEffect, useState } from 'react';
import { history, useSelector } from 'umi';
import { ENetifCategory } from '../../DeviceNetif/typings';
import type { INetif } from '../../DeviceNetif/typings';
import type { INetworkMap } from '../../Network/typings';
import PolicyStat from '../components/PolicyStat';
import {
  deleteForwardPolicy,
  disableForwardPolicy,
  enableForwardPolicy,
  queryForwardPolicies,
  queryForwardRulesList,
} from '../service';
import type { IForwardPolicy, IForwardRule } from '../typings';
import { EForwardPolicyIPTunnelMode, EForwardPolicyState } from '../typings';

const PolicyList = () => {
  const [tableData, setTableData] = useState<IForwardPolicy[]>([]);
  const [tableLoading, setTableLoading] = useState(false);
  const networkMap = useSelector<ConnectState, INetworkMap>(
    (state) => state.networkModel.allNetworkMap,
  );
  const [ruleMap, setRuleMap] = useState<Record<string, IForwardRule>>({});
  const [selectedPolicy, setSelectedPolicy] = useState<IForwardPolicy>();
  const [netifs, setNetifs] = useSafeState<INetif[]>();

  // 用了刷新数据
  const [refresh, setRefresh] = useState(0);

  useEffect(() => {
    queryForwardRulesList().then(({ success, result }) => {
      if (success) {
        setRuleMap(
          result.reduce((total, current) => {
            return {
              ...total,
              [current.id]: current,
            };
          }, {}),
        );
      }
    });
    queryDeviceNetifs().then(({ success, result }) => {
      if (success) {
        // 配置转发策略时，只能从重放口中选择
        setNetifs((result as INetif[]).filter((item) => item.category === ENetifCategory.REPLAY));
      }
    });
  }, [setNetifs]);

  const columns: TableColumnProps<IForwardPolicy>[] = [
    {
      dataIndex: 'name',
      title: '策略名称',
      align: 'center',
    },
    {
      dataIndex: 'ruleId',
      title: '规则名称',
      align: 'center',
      render: (dom, record) => {
        const { ruleId } = record;
        return ruleMap[ruleId]?.name;
      },
    },
    {
      dataIndex: 'networkId',
      title: '作用网络',
      align: 'center',
      render: (dom, record) => {
        const { networkId } = record;

        return parseArrayJson(networkId as string)
          .map((id: string) => networkMap[id]?.name)
          .join(',');
      },
    },
    {
      dataIndex: 'netifName',
      title: '转发接口',
      align: 'center',
      render: (dom, record) => {
        const { netifName } = record;
        if (netifs !== undefined) {
          const allNetifNames = netifs.map((item) => item.name);
          return parseArrayJson(netifName)
            .filter((nif: string) => allNetifNames.includes(nif))
            .join(',');
        }
        return parseArrayJson(netifName).join(',');
      },
    },
    {
      dataIndex: 'ipTunnel',
      title: '封装模式',
      align: 'center',
      render: (dom, record) => {
        const { ipTunnel } = record;
        return Object.keys(EForwardPolicyIPTunnelMode).find(
          (key) => EForwardPolicyIPTunnelMode[key] === JSON.parse(ipTunnel).mode,
        );
      },
    },
    {
      dataIndex: 'totalBandWidth',
      title: '带宽',
      align: 'center',
      render: (dom, record) => {
        const { totalBandWidth } = record;
        return (
          <span
            className="link"
            onClick={() => {
              setSelectedPolicy((prev) => {
                if (prev?.id === record.id) {
                  return undefined;
                }
                return record;
              });
            }}
          >
            {convertBandwidth(totalBandWidth || 0)}
          </span>
        );
      },
    },
    {
      dataIndex: 'state',
      title: '状态',
      align: 'center',
      render: (dom, record) => {
        const { state } = record;
        return state === EForwardPolicyState.启用 ? '启用' : '停用';
      },
    },
    {
      dataIndex: 'operation',
      title: '操作',
      align: 'center',
      render: (dom, record) => {
        const { id, state } = record;

        return (
          <Space>
            <span
              className="link"
              onClick={() => {
                history.push(`/configuration/netflow/forward-policy/update/${id}`);
              }}
            >
              编辑
            </span>
            <span
              className={state === EForwardPolicyState.停用 ? 'link' : 'disabled'}
              onClick={() => {
                if (state === EForwardPolicyState.停用) {
                  enableForwardPolicy(id).then(() => {
                    setRefresh((prev) => prev + 1);
                  });
                }
              }}
            >
              启用
            </span>
            <span
              className={state === EForwardPolicyState.启用 ? 'link' : 'disabled'}
              onClick={() => {
                if (state === EForwardPolicyState.启用) {
                  disableForwardPolicy(id).then(() => {
                    setRefresh((prev) => prev + 1);
                  });
                }
              }}
            >
              关闭
            </span>
            <span
              className="link"
              onClick={() => {
                deleteForwardPolicy(id).then(() => {
                  setRefresh((prev) => prev + 1);
                });
              }}
            >
              删除
            </span>
          </Space>
        );
      },
    },
  ];

  useEffect(() => {
    setTableLoading(true);
    queryForwardPolicies({ page: 0, pageSize: 20 }).then(({ success, result }) => {
      if (success) {
        const { content } = result;
        setTableData(content);
      }
      setTableLoading(false);
    });
  }, [refresh]);

  return (
    <>
      <Card
        size="small"
        loading={tableLoading}
        extra={
          <Space>
            <Button
              onClick={() => {
                setRefresh((prev) => prev + 1);
              }}
            >
              刷新
            </Button>
            <Button
              type="primary"
              onClick={() => {
                history.push('/configuration/netflow/forward-policy/create');
              }}
            >
              新建
            </Button>
          </Space>
        }
      >
        <Table<IForwardPolicy>
          size="small"
          rowKey={'id'}
          columns={columns}
          dataSource={tableData}
          pagination={getTablePaginationDefaultSettings()}
        />
      </Card>
      {selectedPolicy && (
        <PolicyStat
          policyName={selectedPolicy.name}
          policyId={selectedPolicy.id}
          networkIds={parseArrayJson(selectedPolicy.networkId).map((id: string) => ({
            label: networkMap[id]?.name,
            value: id,
          }))}
          netifNames={parseArrayJson(selectedPolicy.netifName).map((item: string) => ({
            label: item,
            value: item,
          }))}
        />
      )}
    </>
  );
};

export default PolicyList;
