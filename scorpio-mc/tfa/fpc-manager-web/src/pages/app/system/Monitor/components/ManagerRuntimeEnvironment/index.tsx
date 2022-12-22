import type { ConnectState } from '@/models/connect';
import { customSpin } from '@/pages/app/Home/utils';
import { SoundFilled } from '@ant-design/icons';
import { Card, Divider } from 'antd';
import { connect } from 'dva';
import { useEffect } from 'react';
import type { Dispatch } from 'umi';

function ManagerRuntimeEnvironment(
  props: {
    dispatch: Dispatch;
    queryRuntimeLoading?: boolean;
  } & Pick<ConnectState['appModel'], 'systemTime' | 'systemUptimeText'>,
) {
  const { dispatch, systemTime, systemUptimeText, queryRuntimeLoading } = props;
  useEffect(() => {
    dispatch({
      type: 'appModel/queryRuntimeEnvironments',
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
export default connect(({ appModel: { systemUptimeText, systemTime }, loading }: ConnectState) => ({
  systemUptimeText,
  systemTime,
  queryRuntimeLoading: loading.effects['appModel/queryRuntimeEnvironments'],
}))(ManagerRuntimeEnvironment);
