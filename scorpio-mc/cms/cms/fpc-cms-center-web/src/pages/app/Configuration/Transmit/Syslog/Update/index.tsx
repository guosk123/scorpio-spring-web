import TransmitForm from '../components/TransmitForm';
import type { ConnectState } from '@/models/connect';
import { connect, Dispatch } from 'umi';
import { useEffect } from 'react';
import { Card, Empty, Skeleton } from 'antd';
import { useParams } from 'umi';
import { ITransmitSyslog } from '../../typings';

const Index: React.FC<{
  dispatch: Dispatch;
  transmitSyslogDetail: ITransmitSyslog;
  queryLoading: boolean;
}> = ({ dispatch, queryLoading, transmitSyslogDetail }) => {
  const urlParmas = useParams<{ id: string }>();

  useEffect(() => {
    dispatch({
      type: 'transmitModel/queryTransmitSyslogDetail',
      payload: urlParmas.id,
    });
  }, []);
  // @ts-ignore
  return (
    <Card bordered={false}>
      <Skeleton active loading={queryLoading}>
        {transmitSyslogDetail.id ? (
          // @ts-ignore
          <TransmitForm details={transmitSyslogDetail} />
        ) : (
          <Empty description="Syslog外发配置不存在或已被删除" />
        )}
      </Skeleton>
    </Card>
  );
};

export default connect(
  ({ loading: { effects }, transmitModel: { transmitSyslogDetail } }: ConnectState) => ({
    queryLoading: effects['transmitModel/queryTransmitSyslogDetail'],
    transmitSyslogDetail,
  }),
)(Index as any);
