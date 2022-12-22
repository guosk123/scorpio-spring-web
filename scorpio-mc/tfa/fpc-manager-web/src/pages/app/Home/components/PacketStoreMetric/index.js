import React, { PureComponent, Fragment } from 'react';
import { connect } from 'dva';
import { SyncOutlined } from '@ant-design/icons';
import { Divider, Tooltip } from 'antd';
import { ChartCard } from '@/components/Charts';
import { getMetricsValue, bytesToSize } from '@/utils/utils';
import storeIcon from '../../assets/store.svg';
import { customSpin } from '../../utils';

/**
 * 流量存储统计
 */
@connect(({ moitorModel: { metrics }, loading }) => ({
  metrics,
  queryLoading: loading.effects['moitorModel/queryMetrics'],
}))
class PacketStoreMetric extends PureComponent {
  componentDidMount() {
    this.queryMetrics();
  }

  queryMetrics = () => {
    const { dispatch } = this.props;
    // 获取统计
    dispatch({ type: 'moitorModel/queryMetrics' });
  };

  render() {
    // 统计
    const { metrics, chartProps, queryLoading } = this.props;

    // ==============
    // 最早报文的时间
    const dataOldestTime = getMetricsValue('data_oldest_time', metrics) || '--';
    // 近24小时流量存储比特数
    const dataLast24TotalByte = getMetricsValue('data_last24_total_byte', metrics) || 0;
    // 流量存储
    const fsDataUsedByte = getMetricsValue('fs_data_used_byte', metrics) || 0;
    const fsDataTotalByte = getMetricsValue('fs_data_total_byte', metrics) || 0;

    return (
      <ChartCard
        title="流量存储"
        bordered={false}
        {...chartProps}
        avatar={<img alt="indicator" style={{ width: 56, height: 56 }} src={storeIcon} />}
        total={
          <Fragment>
            {queryLoading
              ? customSpin
              : `${bytesToSize(fsDataUsedByte, 3)} / ${bytesToSize(fsDataTotalByte, 3)}`}
          </Fragment>
        }
        footer={
          <div style={{ height: 24 }}>
            最早报文时间：{queryLoading ? customSpin : <b>{dataOldestTime}</b>}
            <Divider type="vertical" />
            近24h占用：
            {queryLoading ? customSpin : <b>{bytesToSize(dataLast24TotalByte, 3)}</b>}
          </div>
        }
        action={
          <Tooltip title="点击刷新" onClick={this.queryMetrics}>
            <SyncOutlined style={{ fontSize: 16 }} />
          </Tooltip>
        }
      />
    );
  }
}

export default PacketStoreMetric;
