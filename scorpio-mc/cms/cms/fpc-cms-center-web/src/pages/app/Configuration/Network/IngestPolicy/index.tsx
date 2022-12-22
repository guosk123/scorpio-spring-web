import type { ConnectState } from '@/models/connect';
import type { ColumnProps } from 'antd/lib/table';
import { Button } from 'antd';
import { connect } from 'dva';
import type { FC } from 'react';
import { useEffect, useRef } from 'react';
import type { Dispatch } from 'umi';
import type { IIngestPolicy } from '../../IngestPolicy/typings';
import type { IEditableTableRefReturn, IPolicy } from '../components/EditableTable';
import EditableTable from '../components/EditableTable';
import type { INetworkPolicy, INetworkSensorMap } from '../typings';
import { ESensorStatus } from '../typings';
import { history } from 'umi';
import { PlusOutlined } from '@ant-design/icons';

interface NetworkApplicationPolicyProps {
  dispatch: Dispatch;
  updateLoading?: boolean;
  queryLoading?: boolean;
  networkIngestPolicy: INetworkPolicy[];
  allIngestPolicy: IIngestPolicy[];
  allNetworkSensorMap: INetworkSensorMap;
}

const NetworkApplicationPolicy: FC<NetworkApplicationPolicyProps> = ({
  dispatch,
  updateLoading = false,
  queryLoading = false,
  networkIngestPolicy,
  allIngestPolicy = [],
  allNetworkSensorMap,
}) => {
  const childRef = useRef<IEditableTableRefReturn>(null);

  useEffect(() => {
    queryData();
  }, []);

  const queryData = () => {
    if (dispatch) {
      dispatch({
        type: 'networkModel/queryNetworkIngestPolicy',
      });

      dispatch({
        type: 'ingestPolicyModel/queryAllIngestPolicies',
      });

      dispatch({
        type: 'networkModel/queryAllNetworkSensor',
      });
    }
  };

  const handleSave = (record: INetworkPolicy) => {
    (
      dispatch({
        type: 'networkModel/updateNetworkIngestPolicy',
        payload: {
          networkId: record.networkId,
          policyId: record.policyId,
          id: record.id,
        },
      }) as unknown as Promise<any>
    ).then((success) => {
      if (success) {
        // 成功了要重置子组件
        childRef?.current?.reset();
      }
    });
  };

  const columns: ColumnProps<INetworkPolicy>[] = [
    {
      title: '网络',
      dataIndex: 'networkName',
      ellipsis: true,
      render: (_, record) => {
        const { networkId } = record;
        if (!networkId) {
          return '';
        }
        const { status, name } = allNetworkSensorMap[networkId] || {};
        return `${name}${status === ESensorStatus.OFFLINE ? '(离线)' : ''}`;
      },
    },
    {
      title: '规则名称',
      dataIndex: 'policyId',
      ellipsis: true,
      // @ts-ignore
      editable: true,
      render: (_, record) => record.policyName,
    },
  ];

  return (
    <div>
      <Button
        type="primary"
        icon={<PlusOutlined />}
        style={{ marginLeft: '10px', float: 'right', marginBottom: '10px' }}
        onClick={() => {
          history.push('/configuration/netflow/ingest/network-ingest-policy/create');
        }}
      >
        新建
      </Button>
      <EditableTable
        ref={childRef}
        dataSource={networkIngestPolicy}
        loading={queryLoading}
        columns={columns}
        onSave={handleSave}
        policyList={allIngestPolicy as IPolicy[]}
        submitting={updateLoading}
      />
    </div>
  );
};

export default connect(
  ({
    networkModel: { networkIngestPolicy, allNetworkSensorMap },
    ingestPolicyModel: { allIngestPolicy },
    loading: { effects },
  }: ConnectState) => ({
    networkIngestPolicy,
    allIngestPolicy,
    allNetworkSensorMap,
    queryLoading: effects['networkModel/queryNetworkIngestPolicy'],
    updateLoading: effects['networkModel/updateNetworkIngestPolicy'],
  }),
)(NetworkApplicationPolicy);
