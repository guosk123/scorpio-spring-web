import type { ConnectState } from '@/models/connect';
import type { ColumnProps } from 'antd/lib/table';
import { connect } from 'dva';
import type { FC} from 'react';
import { useState } from 'react';
import { useEffect, useRef } from 'react';
import type { Dispatch } from 'umi';
import ConnectCmsState from '../../components/ConnectCmsState';
import type { IIngestPolicy } from '../../IngestPolicy/typings';
import type { IEditableTableRefReturn, IPolicy } from '../components/EditableTable';
import EditableTable from '../components/EditableTable';
import type { INetworkPolicy } from '../typings';

interface NetworkApplicationPolicyProps {
  dispatch: Dispatch;
  updateLoading?: boolean;
  queryLoading?: boolean;
  networkIngestPolicy: INetworkPolicy[];
  allIngestPolicy: IIngestPolicy[];
}

const NetworkApplicationPolicy: FC<NetworkApplicationPolicyProps> = ({
  dispatch,
  updateLoading = false,
  queryLoading = false,
  networkIngestPolicy,
  allIngestPolicy = [],
}) => {
  const childRef = useRef<IEditableTableRefReturn>(null);
  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);
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
    }
  };

  const handleSave = (record: INetworkPolicy) => {
    (
      dispatch({
        type: 'networkModel/updateNetworkIngestPolicy',
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
        dataSource={networkIngestPolicy}
        loading={queryLoading}
        columns={columns}
        onSave={handleSave}
        policyList={allIngestPolicy as IPolicy[]}
        submitting={updateLoading}
        editable={!cmsConnectFlag}
      />
    </>
  );
};

export default connect(
  ({
    networkModel: { networkIngestPolicy },
    ingestPolicyModel: { allIngestPolicy },
    loading: { effects },
  }: ConnectState) => ({
    networkIngestPolicy,
    allIngestPolicy,
    queryLoading: effects['networkModel/queryNetworkIngestPolicy'],
    updateLoading: effects['networkModel/updateNetworkIngestPolicy'],
  }),
)(NetworkApplicationPolicy);
