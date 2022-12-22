import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType, getGlobalTime, globalTimeFormatText } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import type { TrendChartData } from '@/pages/app/analysis/components/AnalysisChart';
import type {
  INetworkDashboardData,
  IMetricQueryParams,
  IUriParams,
} from '@/pages/app/analysis/typings';
import { ESortDirection } from '@/pages/app/analysis/typings';
import { bytesToSize, completeTimePoint, timeFormatter } from '@/utils/utils';
import { Col, Row } from 'antd';
import moment from 'moment';
import React, { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, useParams } from 'umi';
import DashboardPreview from './components/DashboardPreview';
import MultipleSourceLine from './components/MultipleSourceLine';
import MultipleSourceTrend from './components/MultipleSourceTrend';
import {
  queryIpConversationTop,
  queryL3Top,
  queryNetworkDashboard,
  queryServiceDashboard,
} from '../../service';
import styles from './index.less';
import MultipeSourceBar from './components/MultipeSourceBar';
import { NetworkTypeContext } from '../../Analysis/index';
import type { INetworkTreeItem } from '../../typing';
import { ENetowrkType } from '../../typing';
import ChartMarkAreaDetail from './components/ChartMarkAreaDetail';
import { ServiceContext } from '@/pages/app/analysis/Service/index';
import useAbortXhr from '../../hooks/useAbortXhr';

interface INetworkDashboard {
  dispatch: Dispatch;
  globalSelectedTime: Required<IGlobalTime>;
  isRefresh?: boolean;
  alertMsgCnt: number;
}

const NetworkDashboard: React.FC<INetworkDashboard> = (props) => {
  const { dispatch, globalSelectedTime, isRefresh = true, alertMsgCnt } = props;
  const { serviceId, networkId } = useParams<IUriParams>();
  const getUrlParams = () => {
    const tmpNetworkId = networkId || '';
    if (tmpNetworkId.includes('^')) {
      return [serviceId, tmpNetworkId.split('^')[1]];
    }
    return [serviceId, networkId];
  };

  const [l3TopData, setL3TopData] = useState<{
    totalBytes: { label: string; value: number }[];
    totalSessions: { label: string; value: number }[];
  }>({
    totalBytes: [],
    totalSessions: [],
  });

  const [, networkDataSet] = useContext<[ENetowrkType, INetworkTreeItem[]]>(NetworkTypeContext);
  const [networkType] = useContext<[ENetowrkType, INetworkTreeItem[]] | any>(
    serviceId ? ServiceContext : NetworkTypeContext,
  );
  const [l3TopLoading, setL3TopLoading] = useState(false);
  const [l3ConversationLoading, setL3ConversationLoading] = useState(false);

  const [l3ConversationTop, setL3ConversationTop] = useState<{
    totalBytes: { label: string; value: number }[];
    totalSessions: { label: string; value: number }[];
  }>({
    totalBytes: [],
    totalSessions: [],
  });

  const payload = useMemo(() => {
    const [tmpServiceId, tmpNetworkId] = getUrlParams();
    const data = {
      count: 10,
      startTime: globalSelectedTime.startTime,
      endTime: globalSelectedTime.endTime,
      interval: globalSelectedTime.interval,
      serviceId: tmpServiceId,
      dsl: `${
        networkType === ENetowrkType.NETWORK
          ? `(network_id= "${tmpNetworkId}" ${
              tmpServiceId ? `and service_id = "${tmpServiceId}"` : ''
            })`
          : ''
      } | gentimes timestamp start="${globalSelectedTime.startTime}" end="${
        globalSelectedTime.endTime
      }"`,
    };
    data[networkType === ENetowrkType.NETWORK ? 'networkId' : 'networkGroupId'] = tmpNetworkId;
    return data;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    globalSelectedTime.endTime,
    globalSelectedTime.interval,
    globalSelectedTime.startTime,
    networkType,
  ]);

  const [networkDashboardData, setNetworkDashboardData] = useState<INetworkDashboardData>(
    {} as INetworkDashboardData,
  );

  const [queryLoading, setQueryLoading] = useState(false);

  useEffect(() => {
    const [tmpServiceId, tmpNetworkId] = getUrlParams();
    if (isRefresh) {
      setQueryLoading(true);
      if (tmpServiceId) {
        queryServiceDashboard(payload).then((res) => {
          const { success, result } = res;
          if (success) {
            setNetworkDashboardData(result);
          }
          setQueryLoading(false);
        });
      } else {
        queryNetworkDashboard(payload).then((res) => {
          const { success, result } = res;
          if (success) {
            setNetworkDashboardData(result);
          }
          setQueryLoading(false);
        });
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [payload, isRefresh]);

  // 3层top数据查询参数
  // 后续使用需要添加sortProperty 指明排序字段
  const queryL3TopDataParams = useMemo<IMetricQueryParams>(() => {
    const [tmpServiceId, tmpNetworkId] = getUrlParams();
    return {
      ...(() => {
        if (networkType === ENetowrkType.NETWORK) {
          return {
            networkId: tmpNetworkId,
          };
        }
        if (networkType === ENetowrkType.NETWORK_GROUP) {
          return {
            networkGroupId: tmpNetworkId,
          };
        }
        return {};
      })(),
      serviceId: tmpServiceId,
      startTime: globalSelectedTime.startTime,
      endTime: globalSelectedTime.endTime,
      interval: globalSelectedTime.interval,
      sortDirection: ESortDirection.DESC,
      count: 10,
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    globalSelectedTime.endTime,
    globalSelectedTime.interval,
    globalSelectedTime.startTime,
    networkType,
  ]);

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

  useEffect(() => {
    dispatch({
      type: 'npmdModel/queryAlertMsgCnt',
      payload: {
        ...(() => {
          if (networkType === ENetowrkType.NETWORK) {
            return { networkId: networkId };
          } else if (networkType === ENetowrkType.NETWORK_GROUP) {
            return { networkGroupId: networkId };
          }
          return {};
        })(),
        serviceId,
        startTime: globalSelectedTime.originStartTime,
        endTime: globalSelectedTime.originEndTime,
      },
    });
  }, [
    dispatch,
    networkId,
    serviceId,
    networkType,
    globalSelectedTime.originStartTime,
    globalSelectedTime.originEndTime,
  ]);

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

  // 实时统计时图表不显示loading
  const loading = useMemo(() => {
    return queryLoading;
  }, [queryLoading]);

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
        globalSelectedTime.startTime!,
        globalSelectedTime.endTime!,
        globalSelectedTime.interval,
      );
    });

    // 趋势图数据
    const data: Record<string, TrendChartData> = {};
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

    // 图表补点
    Object.keys(data).forEach((metric) => {
      data[metric] = completeTimePoint(
        data[metric],
        globalSelectedTime.startTime!,
        globalSelectedTime.endTime!,
        globalSelectedTime.interval,
      );
    });

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
    globalSelectedTime.endTime,
    globalSelectedTime.interval,
    globalSelectedTime.startTime,
  ]);

  const updateGlobalTime = useCallback(
    (from: number, to: number) => {
      let timeObj;
      if ((to - from) / 1000 < 120) {
        const diffSeconds = 120 - (to - from) / 1000;
        const offset = diffSeconds / 2;
        timeObj = timeFormatter(from - offset * 1000, to + offset * 1000);
      } else {
        timeObj = timeFormatter(from, to);
      }

      dispatch({
        type: 'appModel/updateGlobalTime',
        payload: getGlobalTime({
          relative: false,
          type: ETimeType.CUSTOM,
          custom: [
            moment(timeObj.startTime, globalTimeFormatText),
            moment(timeObj.endTime, globalTimeFormatText),
          ],
        }),
      });
    },
    [dispatch],
  );
  const handleChartBrush = useMemo(() => {
    return updateGlobalTime;
  }, [updateGlobalTime]);

  useAbortXhr({
    cancelUrls: [
      '/central/packet-oldest-time',
      '/metric/l3-devices/as-count',
      '/metric/networks/dashboard',
      '/metric/l3-devices/as-count',
      '/appliance/alert-messages/as-count',
      '/metric/ip-conversations/as-count',
      '/metric/ip-conversations/as-count',
    ],
  });

  const onTimeInfoChange = () => {
    setQueryLoading(true);
  };

  const [markArea, setMarkArea] = useState<any>({});

  const markAreaProps = useMemo(() => {
    const [, tmpNetworkId] = getUrlParams();
    if (networkType === ENetowrkType.NETWORK) {
      return { networkId: tmpNetworkId };
    } else {
      return { networkGroupId: tmpNetworkId };
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [networkType]);

  return (
    <>
      <ChartMarkAreaDetail
        {...markAreaProps}
        markAreaDetail={setMarkArea}
        globalSelectedTime={globalSelectedTime}
      />
      <DashboardPreview
        data={{ ...networkDashboardData, alertCounts: alertMsgCnt }}
        loading={loading}
        selectedTimeInfo={globalSelectedTime}
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
          {!serviceId ? (
            <>
              <Col span={12}>
                <MultipleSourceTrend
                  title="以太网帧长包数统计"
                  percentTableLabelTitle="以太网帧长"
                  percentTableValueTitle="包数"
                  loading={loading}
                  data={dashboardResult.framePacketLengthStat}
                  networkId={networkId}
                  brushMenus={[{ text: '数据包', key: 'packet' }]}
                  onBrush={handleChartBrush}
                  onChangeTime={onTimeInfoChange}
                  stacked={true}
                  rightAxisName="平均帧长"
                  markArea={markArea}
                />
              </Col>
              <Col span={12}>
                <MultipleSourceTrend
                  title="IP协议包数统计"
                  percentTableLabelTitle="协议包类型"
                  percentTableValueTitle="包数"
                  loading={loading}
                  data={dashboardResult.ipPacketStat}
                  brushMenus={[{ text: '数据包', key: 'packet' }]}
                  networkId={networkId}
                  onBrush={handleChartBrush}
                  onChangeTime={onTimeInfoChange}
                  markArea={markArea}
                />
              </Col>
              <Col span={12}>
                <MultipleSourceTrend
                  title="以太网类型统计"
                  percentTableValueTitle="包数"
                  percentTableLabelTitle="数据包类型"
                  loading={loading}
                  data={dashboardResult.frameTypeStat}
                  brushMenus={[{ text: '数据包', key: 'packet' }]}
                  networkId={networkId}
                  onBrush={handleChartBrush}
                  onChangeTime={onTimeInfoChange}
                  markArea={markArea}
                />
              </Col>
              <Col span={12}>
                <MultipleSourceTrend
                  title="数据包类型统计"
                  percentTableValueTitle="字节数"
                  percentTableLabelTitle="数据包类型"
                  loading={loading}
                  data={dashboardResult.bytesTypeStat}
                  unitConverter={bytesToSize}
                  brushMenus={[{ text: '数据包', key: 'packet' }]}
                  networkId={networkId}
                  onBrush={handleChartBrush}
                  onChangeTime={onTimeInfoChange}
                  markArea={markArea}
                />
              </Col>
            </>
          ) : (
            ''
          )}
          <Col span={12}>
            <MultipleSourceLine
              title="分片包统计"
              loading={loading}
              dataList={dashboardResult.fragmentStat}
              brushMenus={[{ text: '数据包', key: 'packet' }]}
              networkId={networkId}
              onBrush={handleChartBrush}
              onChangeTime={onTimeInfoChange}
              markArea={markArea}
            />
          </Col>
          <Col span={12}>
            <MultipleSourceTrend
              percentTableValueTitle="占比"
              percentTableLabelTitle="DSCP类型"
              title="DSCP统计"
              loading={loading}
              data={dashboardResult.dscpStat}
              unitConverter={bytesToSize}
              brushMenus={[{ text: '数据包', key: 'packet' }]}
              networkId={networkId}
              onBrush={handleChartBrush}
              onChangeTime={onTimeInfoChange}
              markArea={markArea}
            />
          </Col>
        </Row>
      </section>
    </>
  );
};

export default connect(
  ({ appModel: { globalSelectedTime }, npmdModel: { alertMsgCnt } }: ConnectState) => ({
    globalSelectedTime,
    alertMsgCnt,
  }),
)(NetworkDashboard);
