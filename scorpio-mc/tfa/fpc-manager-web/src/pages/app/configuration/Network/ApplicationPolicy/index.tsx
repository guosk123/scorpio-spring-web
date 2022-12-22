import type { ConnectState } from '@/models/connect';
import type { ColumnProps } from 'antd/lib/table';
import { connect } from 'dva';
import React, { useEffect, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import ConnectCmsState from '../../components/ConnectCmsState';
import type { IApplicationPolicy } from '../../ApplicationPolicy/typings';
import type { IEditableTableRefReturn, IPolicy } from '../components/EditableTable';
import EditableTable from '../components/EditableTable';
import type { INetworkPolicy } from '../typings';

interface NetworkApplicationPolicyProps {
  dispatch: Dispatch;
  updateLoading: boolean;
  queryLoading: boolean;
  networkApplicationPolicy: INetworkPolicy[];
  allApplicationPolicy: IApplicationPolicy[];
}

const NetworkApplicationPolicy: React.FC<NetworkApplicationPolicyProps> = ({
  dispatch,
  updateLoading,
  queryLoading,
  networkApplicationPolicy,
  allApplicationPolicy = [],
}) => {
  const childRef = useRef<IEditableTableRefReturn>(null);
  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);
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
    <>
      <ConnectCmsState onConnectFlag={setCmsConnectFlag} />
      <EditableTable
        ref={childRef}
        dataSource={networkApplicationPolicy}
        loading={queryLoading}
        columns={columns}
        onSave={handleSave}
        policyList={allApplicationPolicy as IPolicy[]}
        submitting={updateLoading}
        editable={!cmsConnectFlag}
      />
    </>
  );
};

export default connect(
  ({
    networkModel: { networkApplicationPolicy },
    applicationPolicyModel: { allApplicationPolicy },
    loading: { effects },
  }: ConnectState) => ({
    networkApplicationPolicy,
    allApplicationPolicy,
    queryLoading: effects['networkModel/queryNetworkApplicationPolicy'],
    updateLoading: effects['networkModel/updateNetworkApplicationPolicy'],
  }),
)(NetworkApplicationPolicy);
