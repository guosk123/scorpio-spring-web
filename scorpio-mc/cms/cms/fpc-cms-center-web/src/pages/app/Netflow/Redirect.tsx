import PageLoading from '@/components/PageLoading';
import type { AppModelState } from '@/models/app/index';
import type { ConnectState } from '@/models/connect';
import { Button, Result } from 'antd';
import { useEffect, useState } from 'react';
import { connect, history } from 'umi';
import { querySources } from './service';

const NetflowRedirect = ({ globalSelectedTime }: AppModelState) => {
  // 是否有netflow源标记
  const [hasSource, setHasSource] = useState<boolean>(true);
  // 查询设备列表，并且获得第一条数据为默认
  useEffect(() => {
    // 直接点击标签进入时，查询设备列表第一条数据
    const { originStartTime, originEndTime } = globalSelectedTime;
    if (originStartTime && originEndTime) {
      querySources({
        pageSize: 1,
        pageNumber: 0,
        startTime: originStartTime,
        endTime: originEndTime,
      }).then((res) => {
        if (!res.success) {
          return;
        }
        const { content } = res.result;
        if (content && content.length > 0) {
          const { deviceName, netifNo } = content.pop();
          if (deviceName) {
            if (netifNo) {
              history.push(`/netflow/device/${deviceName}/netif/${netifNo}/dashboard`);
            } else {
              history.push(`/netflow/device/${deviceName}/dashboard`);
            }
            setHasSource(true);
            return;
          }
        }
        setHasSource(false);
      });
    }
  }, [globalSelectedTime]);

  if (!hasSource) {
    return (
      <Result
        status="info"
        title="无Netflow源"
        extra={
          <Button type="primary" onClick={() => history.push('/netflow/device/list')}>
            配置源
          </Button>
        }
      />
    );
  }

  return <PageLoading />;
};

export default connect(({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
}))(NetflowRedirect);
