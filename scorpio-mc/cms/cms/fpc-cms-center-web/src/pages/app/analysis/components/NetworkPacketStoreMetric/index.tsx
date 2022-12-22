import type { ConnectState } from '@/models/connect';
import { customSpin } from '@/pages/app/Home/utils';
import storage from './storage.svg';
import { bytesToSize, getMetricsValue } from '@/utils/utils';
import { Card, Divider } from 'antd';
import { connect } from 'dva';
import { useEffect } from 'react';

function NetworkPacketStoreMetric(props: any) {
  const { dispatch, metrics, queryLoading } = props;
  useEffect(() => {
    dispatch({ type: 'moitorModel/queryMetrics' });
  }, [dispatch]);
  // ==============
  // 最早报文的时间
  const dataOldestTime = getMetricsValue('data_oldest_time', metrics) || '--';
  // 近24小时流量存储比特数
  const dataLast24TotalByte = getMetricsValue('data_last24_total_byte', metrics) || 0;
  // 流量存储
  const fsDataUsedByte = getMetricsValue('fs_data_used_byte', metrics) || 0;
  const fsDataTotalByte = getMetricsValue('fs_data_total_byte', metrics) || 0;
  return (
    <Card bordered={false} bodyStyle={{ padding: 0 }}>
      <img style={{ width: 16, height: 16 }} src={storage} />
      <Divider type="vertical" />
      <span>流量存储：</span>
      <strong>
        {queryLoading
          ? customSpin
          : `${bytesToSize(fsDataUsedByte, 3)} / ${bytesToSize(fsDataTotalByte, 3)}`}
      </strong>
      <Divider type="vertical" />
      <span>最早报文时间：</span>
      <strong>{queryLoading ? customSpin : <b>{dataOldestTime}</b>}</strong>
      <Divider type="vertical" />
      <span>近24h占用：</span>
      <strong>{queryLoading ? customSpin : <b>{bytesToSize(dataLast24TotalByte, 3)}</b>}</strong>
    </Card>
  );
}
export default connect(({ moitorModel: { metrics }, loading }: ConnectState) => ({
  metrics,
  queryLoading: loading.effects['moitorModel/queryMetrics'],
}))(NetworkPacketStoreMetric);
