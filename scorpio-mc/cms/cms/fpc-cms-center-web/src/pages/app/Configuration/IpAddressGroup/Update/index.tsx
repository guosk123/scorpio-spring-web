import { useCallback, useEffect } from 'react';
import type { Dispatch } from 'umi';
import { connect } from 'dva';
import type { ConnectState } from '@/models/connect';
import { Card, Empty } from 'antd';
import type { IpAddressGroup } from '../typings';
import IpAddressGroupForm from '../components/Form';

interface Props {
  dispatch: Dispatch;
  match: any;
  queryDetailLoading: boolean | undefined;
  detail: IpAddressGroup;
}

function UpdateIpAddressGroup(props: Props) {
  const { dispatch, match, queryDetailLoading, detail } = props;

  const querySingleIpIpAddressGroup = useCallback(() => {
    const { hostgroupId } = match.params;
    dispatch({
      type: 'ipAddressGroupModel/queryIpAddressGroupDetail',
      payload: {
        id: hostgroupId,
      },
    });
  }, [dispatch, match.params]);

  useEffect(() => {
    querySingleIpIpAddressGroup();
  }, [querySingleIpIpAddressGroup]);

  return (
    <Card bordered={false} loading={queryDetailLoading}>
      {detail.id ? (
        <IpAddressGroupForm detail={detail} operateType="UPDATE" />
      ) : (
        <Empty description="地址组不存在或已被删除" />
      )}
    </Card>
  );
}
export default connect(
  ({ loading: { effects }, ipAddressGroupModel: { ipAddressGroupDetail } }: ConnectState) => ({
    queryDetailLoading: effects['ipAddressGroupModel/queryIpAddressGroupDetail'],
    detail: ipAddressGroupDetail,
  }),
)(UpdateIpAddressGroup);
