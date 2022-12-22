import type { ConnectState } from '@/models/connect';
import { LoadingOutlined, SoundFilled } from '@ant-design/icons';
import { Card, Divider, Spin } from 'antd';
import { connect } from 'dva';
import { useEffect } from 'react';
import type { Dispatch } from 'umi';

const customSpin = <Spin indicator={<LoadingOutlined spin />} />;

interface IProps {
  dispatch: Dispatch;
  queryRuntimeLoading?: boolean;
  systemUptimeText: string;
  systemTime?: string;
}
function ManagerRuntimeEnvironment({
  dispatch,
  queryRuntimeLoading,
  systemUptimeText,
  systemTime,
}: IProps) {
  useEffect(() => {
    dispatch({
      type: 'globalModel/queryRuntimeEnvironments',
    });
  }, [dispatch]);

  return (
    <Card bordered={false} style={{ display: 'inline-block' }} bodyStyle={{ padding: 0 }}>
      <SoundFilled />
      <Divider type="vertical" />
      <span>运行时间：</span>
      <strong>{queryRuntimeLoading ? customSpin : <span>{systemUptimeText}</span>}</strong>
      <Divider type="vertical" />
      <span>服务器时间：</span>
      <strong>{queryRuntimeLoading ? customSpin : <b>{systemTime}</b>}</strong>
    </Card>
  );
}
export default connect(
  ({ loading, globalModel: { systemTime, systemUptimeText } }: ConnectState) => ({
    systemTime,
    systemUptimeText,
    queryRuntimeLoading: loading.effects['globalModel/queryRuntimeEnvironments'],
  }),
)(ManagerRuntimeEnvironment);
