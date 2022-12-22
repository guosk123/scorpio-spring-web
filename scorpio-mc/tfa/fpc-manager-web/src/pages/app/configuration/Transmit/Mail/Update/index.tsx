import TransmitForm from '../components/TransmitForm';
import type { ConnectState } from '@/models/connect';
import type { Dispatch } from 'umi';
import { connect } from 'umi';
import { useEffect } from 'react';
import { Card, Empty, Skeleton } from 'antd';
import { useParams } from 'umi';
import type { ITransmitMail } from '../../typings';

const Index: React.FC<{
  dispatch: Dispatch;
  transmitMailDetail: ITransmitMail;
  queryLoading: boolean;
}> = ({ dispatch, queryLoading, transmitMailDetail }) => {
  const urlParmas = useParams<{ id: string }>();

  useEffect(() => {
    dispatch({
      type: 'transmitModel/queryTransmitMailDetail',
      payload: urlParmas.id,
    });
  }, []);
  // @ts-ignore
  return (
    <Card bordered={false}>
      <Skeleton active loading={queryLoading}>
        {transmitMailDetail.id ? (
          // @ts-ignore
          <TransmitForm details={transmitMailDetail} />
        ) : (
          <Empty description="邮件外发配置不存在或已被删除" />
        )}
      </Skeleton>
    </Card>
  );
};

export default connect(
  ({ loading: { effects }, transmitModel: { transmitMailDetail } }: ConnectState) => ({
    queryLoading: effects['transmitModel/queryTransmitMailDetail'],
    transmitMailDetail,
  }),
)(Index as any);
