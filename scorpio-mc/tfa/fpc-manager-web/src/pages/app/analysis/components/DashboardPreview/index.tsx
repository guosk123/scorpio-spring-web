import { ChartCard } from '@/components/Charts';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ERealTimeStatisticsFlag } from '@/models/app';
import { bytesToSize, convertBandwidth } from '@/utils/utils';
import { Col, Row, Spin } from 'antd';
import numeral from 'numeral';
import type { INetworkDashboardData, IServiceDashboardData } from '../../typings';
import styles from './index.less';

interface IDashboardPreviewProps {
  data: INetworkDashboardData | IServiceDashboardData;
  loading: boolean | undefined;
  /** 实时统计标志 */
  realTimeStatisticsFlag: ERealTimeStatisticsFlag;
  /** 格式化好的全局时间 */
  selectedTimeInfo: Partial<IGlobalTime>;
}
/** 概览页统计卡片 */
const DashboardPreview = ({
  data,
  loading,
  realTimeStatisticsFlag,
  selectedTimeInfo,
}: IDashboardPreviewProps) => {
  /** 带宽均值 */
  let bpsAvg = 0;
  /** 带宽峰值 */
  let bpsPeak = 0;
  /** 上行带宽均值 */
  let upstreamAverageData = '0';
  /** 下行带宽均值 */
  let downstreamAverageData = '0';
  // 非实时统计
  if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.CLOSED) {
    bpsAvg = ((data.totalBytes || 0) * 8) / selectedTimeInfo.totalSeconds!;
    bpsPeak = (data.bytepsPeak || 0) * 8;
    upstreamAverageData = convertBandwidth(
      ((data.upstreamBytes || 0) * 8) / selectedTimeInfo.totalSeconds!,
    );
    downstreamAverageData = convertBandwidth(
      ((data.downstreamBytes || 0) * 8) / selectedTimeInfo.totalSeconds!,
    );
  } else {
    // 实时统计时，总流量=均值带宽=峰值带宽
    bpsAvg = (data.totalBytes || 0) * 8;
    bpsPeak = bpsAvg;
    upstreamAverageData = convertBandwidth((data.upstreamBytes || 0) * 8);
    downstreamAverageData = convertBandwidth((data.downstreamBytes || 0) * 8);
  }

  return (
    <section className={styles.preview}>
      <Spin spinning={loading}>
        <Row gutter={10}>
          {[
            { title: '总流量', value: bytesToSize(data.totalBytes || 0) },
            { title: '总包数', value: numeral(data.totalPackets).format('0,0') },
            {
              title: '带宽均值',
              value: convertBandwidth(bpsAvg),
            },
            {
              title: '带宽峰值',
              value: convertBandwidth(bpsPeak),
            },
            {
              title: '上行带宽均值',
              value: upstreamAverageData,
            },
            {
              title: '下行带宽均值',
              value: downstreamAverageData,
            },
            {
              title: '数据包重传率',
              value: `${((data.tcpRetransmissionRate || 0) * 100).toFixed(2)}%`,
            },
            {
              title: '客户端重传率',
              value: `${((data.tcpClientRetransmissionRate || 0) * 100).toFixed(2)}%`,
            },
            {
              title: '服务端重传率',
              value: `${((data.tcpServerRetransmissionRate || 0) * 100).toFixed(2)}%`,
            },
            {
              title: '告警数量',
              value: numeral(data.alertCounts || 0).format('0,0'),
            },
            {
              title: '客户端网络时延均值',
              value: `${numeral(data.tcpClientNetworkLatencyAvg || 0).format('0,0')}ms`,
            },
            {
              title: '服务端网络时延均值',
              value: `${numeral(data.tcpServerNetworkLatencyAvg || 0).format('0,0')}ms`,
            },
          ].map(({ title, value }) => (
            <Col key={title} lg={4} md={6}>
              <ChartCard
                title={title}
                total={<span style={{ fontSize: 22 }}>{value}</span>}
                bodyStyle={{ padding: '8px 10px 4px' }}
                contentHeight={134}
              />
            </Col>
          ))}
        </Row>
      </Spin>
    </section>
  );
};

export default DashboardPreview;
