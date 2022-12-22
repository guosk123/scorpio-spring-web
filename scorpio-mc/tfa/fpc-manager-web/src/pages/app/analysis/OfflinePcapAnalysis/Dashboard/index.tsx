import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ERealTimeStatisticsFlag } from '@/models/app';
import type { ConnectState } from '@/models/connect';
import { bytesToSize, timeFormatter } from '@/utils/utils';
import { Col, Row } from 'antd';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, useParams } from 'umi';
import DashboardPreview from '../../components/DashboardPreview';
import { completeTimePoint } from '../../components/FlowAnalysis';
import MultipeSourceBar from '../../components/MultipeSourceBar';
import MultipleSourceLine from '../../components/MultipleSourceLine';
import MultipleSourceTrend from '../../components/MultipleSourceTrend';
import { queryIpConversationTop, queryL3Top } from '../../service';
import type { IMetricQueryParams, INetworkDashboardData } from '../../typings';
import { ESortDirection, ESourceType } from '../../typings';
import type { IOfflinePcapData } from '../typing';
import styles from './index.less';

interface IDashboardProps {
  dispatch: Dispatch;
  queryLoading: boolean | undefined;
  networkDashboardData: INetworkDashboardData;
  realTimeStatisticsFlag: ERealTimeStatisticsFlag;
  currentPcpInfo: IOfflinePcapData;
  globalSelectedTime: Required<IGlobalTime>;
}

const Dashboard: React.FC<IDashboardProps> = ({
  dispatch,
  queryLoading,
  networkDashboardData,
  currentPcpInfo,
  globalSelectedTime,
}) => {
  const { pcapFileId } = useParams() as { pcapFileId: string };

  const pcapDetail = useMemo(() => {
    return currentPcpInfo;
  }, [currentPcpInfo]);

  // 计算时间
  const selectedTimeInfo = useMemo(() => {
    return timeFormatter(pcapDetail?.filterStartTime, pcapDetail?.filterEndTime);
  }, [pcapDetail]);

  const [l3TopData, setL3TopData] = useState<{
    totalBytes: { label: string; value: number }[];
    totalSessions: { label: string; value: number }[];
  }>({
    totalBytes: [],
    totalSessions: [],
  });

  const [l3TopLoading, setL3TopLoading] = useState(false);
  const [l3ConversationLoading, setL3ConversationLoading] = useState(false);

  const [l3ConversationTop, setL3ConversationTop] = useState<{
    totalBytes: { label: string; value: number }[];
    totalSessions: { label: string; value: number }[];
  }>({
    totalBytes: [],
    totalSessions: [],
  });

  const queryParams = useMemo(() => {
    const res: IMetricQueryParams = {
      sourceType: ESourceType.OFFLINE,
      packetFileId: pcapFileId,
      count: 10,
      startTime: globalSelectedTime.startTime as string,
      endTime: globalSelectedTime.endTime as string,
      interval: selectedTimeInfo.interval as number,
      dsl: `(network_id = "${pcapFileId}") | gentimes timestamp start="${globalSelectedTime.startTime}" end="${globalSelectedTime.endTime}"`,
    };
    return res;
  }, [selectedTimeInfo, pcapFileId, globalSelectedTime.startTime, globalSelectedTime.endTime]);

  // 3层top数据查询参数
  // 后续使用需要添加sortProperty 指明排序字段
  const queryL3TopDataParams = useMemo<IMetricQueryParams>(() => {
    return {
      ...queryParams,
      sortDirection: ESortDirection.DESC,
    };
  }, [queryParams]);

  // l3 主机 top
  useEffect(() => {
    setL3TopLoading(true);
    const flowTop = queryL3Top({
      ...queryL3TopDataParams,
      sortProperty: 'total_bytes',
    });
    const sessionTop = queryL3Top({
      ...queryL3TopDataParams,
      sortProperty: 'established_sessions',
    });

    Promise.all([flowTop, sessionTop]).then((results) => {
      const l3TotalBytes = results[0].success
        ? results[0].result.map((flow: { [x: string]: any; ipAddress: any }) => ({
            label: flow.ipAddress,
            value: flow.totalBytes,
          }))
        : [];
      const l3TotalSessions = results[1].success
        ? results[1].result.map((flow: { [x: string]: any; ipAddress: any }) => ({
            label: flow.ipAddress,
            value: flow.establishedSessions,
          }))
        : [];
      setL3TopData({
        totalBytes: l3TotalBytes,
        totalSessions: l3TotalSessions,
      });
      setL3TopLoading(false);
    });
  }, [queryL3TopDataParams]);

  // l3 会话对 top
  useEffect(() => {
    setL3ConversationLoading(true);
    const flowTop = queryIpConversationTop({
      ...queryL3TopDataParams,
      sortProperty: 'total_bytes',
    });
    const sessionTop = queryIpConversationTop({
      ...queryL3TopDataParams,
      sortProperty: 'established_sessions',
    });
    Promise.all([flowTop, sessionTop]).then((results) => {
      const l3TotalBytes = results[0].success
        ? results[0].result.map((flow: { [x: string]: any; ipAddress: any }) => ({
            label: `${flow.ipAAddress} ⇋ ${flow.ipBAddress}`,
            value: flow.totalBytes,
          }))
        : [];
      const l3TotalSessions = results[1].success
        ? results[1].result.map((flow: { [x: string]: any; ipAddress: any }) => ({
            label: `${flow.ipAAddress} ⇋ ${flow.ipBAddress}`,
            value: flow.establishedSessions,
          }))
        : [];

      setL3ConversationTop({
        totalBytes: l3TotalBytes,
        totalSessions: l3TotalSessions,
      });
      setL3ConversationLoading(false);
    });
  }, [queryL3TopDataParams]);

  const queryData = useCallback(() => {
    dispatch({
      type: 'npmdModel/queryNetworkDashboard',
      payload: queryParams,
    });
  }, [dispatch, queryParams]);

  useEffect(() => {
    queryData();
  }, [queryData]);

  // 计算各种数据
  const dashboardResult = useMemo(() => {
    // 三层主机 Top
    const {
      /** 以太网帧长统计 */
      tinyPackets,
      smallPackets,
      mediumPackets,
      bigPackets,
      largePackets,
      hugePackets,
      jumboPackets,
      /** IP协议包数统计 */
      tcpTotalPackets,
      udpTotalPackets,
      icmp6TotalPackets,
      icmpTotalPackets,
      otherTotalPackets,
      /** 以太网类型统计 */
      ipv4Frames,
      ipv6Frames,
      arpFrames,
      ieee8021xFrames,
      ipxFrames,
      lacpFrames,
      mplsFrames,
      stpFrames,
      otherFrames,
      /** 数据包类型统计 */
      unicastBytes,
      broadcastBytes,
      multicastBytes,
      /** DSCP 统计 */
      dscp,
      l3DevicesTop = {},
      ipConversationTop = {},
      histogram = [],
    } = networkDashboardData;

    const { totalBytes: l3TotalBytes = [], totalSessions: l3TotalSessions = [] } =
      l3DevicesTop as INetworkDashboardData['l3DevicesTop'];

    // 通信对
    const { totalBytes: ipConverTotalBytes = [], totalSessions: ipConverTotalSessions = [] } =
      ipConversationTop as INetworkDashboardData['ipConversationTop'];

    // 以太网帧长统计 占比图数据
    const frameLengthPercentage = {
      '<=64字节': tinyPackets,
      '65-127字节': smallPackets,
      '128-255字节': mediumPackets,
      '256-511字节': bigPackets,
      '512-1023字节': largePackets,
      '1024-1517字节': hugePackets,
      '>=1518字节': jumboPackets,
    };

    // IP协议包数统计 占比图数据
    const ipProtocolStatBarData = {
      TCP总数据包: tcpTotalPackets,
      UDP总数据包: udpTotalPackets,
      ICMP总数据包: icmpTotalPackets,
      ICMP6总数据包: icmp6TotalPackets,
      其他总数据包: otherTotalPackets,
    };

    // 以太网类型统计 占比图
    const frameTypeStatBarData = {
      IPv4数据包: ipv4Frames,
      IPv6数据包: ipv6Frames,
      ARP数据包: arpFrames,
      'IEEE802.1x数据包': ieee8021xFrames,
      IPX数据包: ipxFrames,
      LACP数据包: lacpFrames,
      MPLS数据包: mplsFrames,
      STP数据包: stpFrames,
      其他类型数据包: otherFrames,
    };

    // 数据包类型统计 占比图数据
    const bytesTypeStatBarData = {
      单播数据包流量: unicastBytes,
      广播数据包流量: broadcastBytes,
      多播数据包流量: multicastBytes,
    };

    const dscpStatBarData = {};
    const dscpStatLineData = {};
    if (dscp && dscp.volumn) {
      dscp.volumn.forEach((item) => {
        dscpStatBarData[item.type] = item.totalBytes;
      });
      const dscpTop10 = dscp.volumn
        .concat()
        .sort((a, b) => b.totalBytes - a.totalBytes)
        .slice(0, 10)
        .map((item) => item.type);

      dscpTop10.forEach((type) => {
        dscpStatLineData[type] = [];
      });
      dscp.histogram
        .filter((item) => {
          return dscpTop10.includes(item.type);
        })
        .forEach((item) => {
          dscpStatLineData[item.type].push([item.timestamp, item.totalBytes]);
        });
    }
    Object.keys(dscpStatLineData).forEach((key) => {
      dscpStatLineData[key] = completeTimePoint(
        dscpStatLineData[key],
        selectedTimeInfo.startTime!,
        selectedTimeInfo.endTime!,
        selectedTimeInfo.interval,
      );
    });

    // 趋势图数据
    const data: Record<string, [string, number][]> = {};
    if (histogram.length > 0) {
      Object.keys(histogram[0]).forEach((key) => {
        if (key !== 'timestamp') {
          if (data[key] === undefined) {
            data[key] = [] as unknown as [string, number][];
          }
        }
      });
      histogram.forEach((frame) => {
        Object.keys(frame).forEach((key) => {
          if (key !== 'timestamp' && data[key] !== undefined) {
            data[key].push([frame.timestamp, frame[key]]);
          }
        });
      });
    }

    return {
      // 三层主机
      l3DevicesTopData: {
        totalBytes: l3TotalBytes.map((item) => ({ label: item.ip, value: item.value })),
        totalSessions: l3TotalSessions.map((item) => ({ label: item.ip, value: item.value })),
      },
      // 三层通讯对
      ipConversationTopData: {
        totalBytes: ipConverTotalBytes.map((item) => ({
          label: `${item.ipA} ⇋ ${item.ipB}`,
          value: item.value,
        })),
        totalSessions: ipConverTotalSessions.map((item) => ({
          label: `${item.ipA} ⇋ ${item.ipB}`,
          value: item.value,
        })),
      },
      fragmentStat: [
        {
          zhName: '流量大小',
          name: 'fragmentTotalBytes',
          data: data.fragmentTotalBytes,
          unitConverter: bytesToSize,
        },
        {
          zhName: '数据包数',
          name: 'fragmentTotalPackets',
          data: data.fragmentTotalPackets,
        },
      ],
      framePacketLengthStat: {
        lineChartData: {
          '<=64字节': data.tinyPackets,
          '65-127字节': data.smallPackets,
          '128-255字节': data.mediumPackets,
          '256-511字节 ': data.bigPackets,
          '512-1023字节': data.largePackets,
          '1024-1517字节': data.hugePackets,
          '>=1518字节': data.jumboPackets,
          平均帧长: data.packetLengthAvg,
        },
        barChartData: frameLengthPercentage,
      },
      ipPacketStat: {
        lineChartData: {
          TCP总数据包: data.tcpTotalPackets,
          UDP总数据包: data.udpTotalPackets,
          ICMP总数据包: data.icmpTotalPackets,
          ICMP6总数据包: data.icmp6TotalPackets,
          其他总数据包: data.otherTotalPackets,
        },
        barChartData: ipProtocolStatBarData,
      },
      frameTypeStat: {
        lineChartData: {
          IPv4数据包: data.ipv4Frames,
          IPv6数据包: data.ipv6Frames,
          ARP数据包: data.arpFrames,
          'IEEE802.1x数据包': data.ieee8021xFrames,
          IPX数据包: data.ipxFrames,
          LACP数据包: data.lacpFrames,
          MPLS数据包: data.mplsFrames,
          STP数据包: data.stpFrames,
          其他类型数据包: data.otherFrames,
        },
        barChartData: frameTypeStatBarData,
      },
      bytesTypeStat: {
        lineChartData: {
          单播数据包流量: data.unicastBytes,
          广播数据包流量: data.broadcastBytes,
          多播数据包流量: data.multicastBytes,
        },
        barChartData: bytesTypeStatBarData,
      },
      dscpStat: {
        lineChartData: dscpStatLineData,
        barChartData: dscpStatBarData,
      },
    };
  }, [
    networkDashboardData,
    selectedTimeInfo.endTime,
    selectedTimeInfo.interval,
    selectedTimeInfo.startTime,
  ]);

  return (
    <>
      <DashboardPreview
        data={networkDashboardData}
        loading={queryLoading}
        realTimeStatisticsFlag={ERealTimeStatisticsFlag.CLOSED}
        selectedTimeInfo={selectedTimeInfo}
      />

      <section className={styles.content}>
        <Row gutter={10}>
          <Col span={12}>
            <MultipeSourceBar title="三层主机 Top" loading={l3TopLoading} data={l3TopData} />
          </Col>
          <Col span={12}>
            <MultipeSourceBar
              title="三层通讯对 Top"
              loading={l3ConversationLoading}
              data={l3ConversationTop}
            />
          </Col>
          <Col span={12}>
            <MultipleSourceTrend
              title="以太网帧长包数统计"
              percentTableLabelTitle="以太网帧长"
              percentTableValueTitle="包数"
              loading={queryLoading}
              data={dashboardResult.framePacketLengthStat}
              stacked={true}
              rightAxisName="平均帧长"
            />
          </Col>
          <Col span={12}>
            <MultipleSourceTrend
              title="IP协议包数统计"
              percentTableLabelTitle="协议包类型"
              percentTableValueTitle="包数"
              loading={queryLoading}
              data={dashboardResult.ipPacketStat}
            />
          </Col>
          <Col span={12}>
            <MultipleSourceTrend
              title="以太网类型统计"
              percentTableValueTitle="包数"
              percentTableLabelTitle="数据包类型"
              loading={queryLoading}
              data={dashboardResult.frameTypeStat}
            />
          </Col>
          <Col span={12}>
            <MultipleSourceTrend
              title="数据包类型统计"
              percentTableValueTitle="字节数"
              percentTableLabelTitle="数据包类型"
              loading={queryLoading}
              data={dashboardResult.bytesTypeStat}
              unitConverter={bytesToSize}
            />
          </Col>
          <Col span={12}>
            <MultipleSourceLine
              title="分片包统计"
              loading={queryLoading}
              dataList={dashboardResult.fragmentStat}
            />
          </Col>
          <Col span={12}>
            <MultipleSourceTrend
              percentTableValueTitle="占比"
              percentTableLabelTitle="DSCP类型"
              title="DSCP统计"
              loading={queryLoading}
              data={dashboardResult.dscpStat}
              unitConverter={bytesToSize}
            />
          </Col>
        </Row>
      </section>
    </>
  );
};

export default connect(
  ({
    loading: { effects },
    npmdModel: { networkDashboardData },
    appModel: { globalSelectedTime },
    npmdModel: { currentPcpInfo },
  }: ConnectState) => ({
    globalSelectedTime,
    networkDashboardData,
    currentPcpInfo,
    queryLoading: effects['npmdModel/queryNetworkDashboard'],
  }),
)(Dashboard);
