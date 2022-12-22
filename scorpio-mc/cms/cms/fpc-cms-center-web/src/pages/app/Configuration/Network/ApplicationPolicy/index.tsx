import type { ConnectState } from '@/models/connect';
import { PlusOutlined } from '@ant-design/icons';
import { Button } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import { connect } from 'dva';
import React, { useEffect, useRef } from 'react';
import type { Dispatch } from 'umi';
import type { IApplicationPolicy } from '../../ApplicationPolicy/typings';
import type { IEditableTableRefReturn, IPolicy } from '../components/EditableTable';
import EditableTable from '../components/EditableTable';
import { history } from 'umi';
import { ESensorStatus, INetworkPolicy, INetworkSensorMap } from '../typings';

interface NetworkApplicationPolicyProps {
  dispatch: Dispatch;
  updateLoading: boolean;
  queryLoading: boolean;
  networkApplicationPolicy: INetworkPolicy[];
  allApplicationPolicy: IApplicationPolicy[];
  allNetworkSensorMap: INetworkSensorMap;
}

const NetworkApplicationPolicy: React.FC<NetworkApplicationPolicyProps> = ({
  dispatch,
  updateLoading,
  queryLoading,
  networkApplicationPolicy,
  allApplicationPolicy = [],
  allNetworkSensorMap,
}) => {
  const childRef = useRef<IEditableTableRefReturn>(null);

  useEffect(() => {
    queryData();
  }, []);

  const queryData = () => {
    if (dispatch) {
      dispatch({
        type: 'networkModel/queryNetworkApplicationPolicy',
      });

      dispatch({
        type: 'applicationPolicyModel/queryAllApplicationPolicies',
      });
    }
  };

  const handleSave = (record: INetworkPolicy) => {
    (
      dispatch({
        type: 'networkModel/updateNetworkApplicationPolicy',
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
        const { status, name } = allNetworkSensorMap[networkId];
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
          history.push('/configuration/netflow/network-application-policy/create');
        }}
      >
        新建
      </Button>
      <EditableTable
        ref={childRef}
        dataSource={networkApplicationPolicy}
        loading={queryLoading}
        columns={columns}
        onSave={handleSave}
        policyList={allApplicationPolicy as IPolicy[]}
        submitting={updateLoading}
      />
    </div>
  );
};

export default connect(
  ({
    networkModel: { networkApplicationPolicy, allNetworkSensorMap },
    applicationPolicyModel: { allApplicationPolicy },
    loading: { effects },
  }: ConnectState) => ({
    networkApplicationPolicy,
    allApplicationPolicy,
    allNetworkSensorMap,
    queryLoading: effects['networkModel/queryNetworkApplicationPolicy'],
    updateLoading: effects['networkModel/updateNetworkApplicationPolicy'],
  }),
)(NetworkApplicationPolicy as any);
