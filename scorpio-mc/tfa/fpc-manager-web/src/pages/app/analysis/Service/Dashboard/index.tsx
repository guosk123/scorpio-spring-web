import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType, getGlobalTime, globalTimeFormatText } from '@/components/GlobalTimeSelector';
import { ERealTimeStatisticsFlag } from '@/models/app';
import type { ConnectState } from '@/models/connect';
import { bytesToSize, timeFormatter } from '@/utils/utils';
import { Col, Row } from 'antd';
import moment from 'moment';
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, useParams } from 'umi';
import type { TrendChartData } from '../../components/AnalysisChart';
import CacheStateBox, { stopLoad } from '../../components/CacheStateBox';
import DashboardPreview from '../../components/DashboardPreview';
import { completeTimePoint } from '../../components/FlowAnalysis';
import MultipeSourceBar from '../../components/MultipeSourceBar';
import MultipleSourceLine from '../../components/MultipleSourceLine';
import MultipleSourceTrend from '../../components/MultipleSourceTrend';
import { queryIpConversationTop, queryL3Top } from '../../service';
import type { IMetricQueryParams, IServiceDashboardData, IUriParams } from '../../typings';
import { ESortDirection, REAL_TIME_POLLING_MS } from '../../typings';
import styles from './index.less';

interface IServiceDashboard {
  dispatch: Dispatch;
  queryLoading: boolean | undefined;
  serviceDashboardData: IServiceDashboardData;
  globalSelectedTime: Required<IGlobalTime>;
  beforeOldestPacketArea: any;
  realTimeStatisticsFlag: ERealTimeStatisticsFlag;
  alertMsgCnt: number;
}

const ServiceDashboard: React.FC<IServiceDashboard> = ({
  dispatch,
  queryLoading,
  serviceDashboardData,
  globalSelectedTime,
  beforeOldestPacketArea,
  realTimeStatisticsFlag,
  alertMsgCnt,
}) => {
  const realTimePollingRef = useRef<number | undefined>(undefined);
  const { networkId, serviceId = '' }: IUriParams = useParams();

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

  // 3层top数据查询参数
  // 后续使用需要添加sortProperty 指明排序字段
  const queryL3TopDataParams = useMemo<IMetricQueryParams>(() => {
    return {
      networkId,
      serviceId,
      startTime: globalSelectedTime.startTime,
      endTime: globalSelectedTime.endTime,
      interval: globalSelectedTime.interval,
      sortDirection: ESortDirection.DESC,
      count: 10,
    };
  }, [
    globalSelectedTime.endTime,
    globalSelectedTime.interval,
    globalSelectedTime.startTime,
    networkId,
    serviceId,
  ]);

  // l3 主机 top
  useEffect(() => {
    if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.CLOSED) {
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
    }
  }, [queryL3TopDataParams, realTimeStatisticsFlag]);

  // l3 会话对 top
  useEffect(() => {
    if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.CLOSED) {
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
    }
  }, [queryL3TopDataParams, realTimeStatisticsFlag]);

  const queryData = useCallback(() => {
    dispatch({
      type: 'npmdModel/queryServiceDashboard',
      payload: {
        networkId,
        serviceId,
        count: 10,
        startTime: globalSelectedTime.startTime,
        endTime: globalSelectedTime.endTime,
        interval: globalSelectedTime.interval,
        dsl: `(network_id="${networkId}" and service_id="${serviceId}") | gentimes timestamp start="${globalSelectedTime.startTime}" end="${globalSelectedTime.endTime}"`,
        realTime: realTimeStatisticsFlag,
      } as IMetricQueryParams,
    });
  }, [
    dispatch,
    globalSelectedTime.originStartTime,
    globalSelectedTime.originEndTime,
    networkId,
    serviceId,
    realTimeStatisticsFlag,
  ]);

  useEffect(() => {
    queryData();
  }, [queryData]);

  useEffect(() => {
    dispatch({
      type: 'npmdModel/queryAlertMsgCnt',
      payload: {
        serviceId,
        networkId,
        startTime: globalSelectedTime.originStartTime,
        endTime: globalSelectedTime.originEndTime,
      },
    });
  }, [
    dispatch,
    globalSelectedTime.originStartTime,
    globalSelectedTime.originEndTime,
    serviceId,
    networkId,
  ]);

  useEffect(() => {
    if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.CLOSED) {
      window.clearInterval(realTimePollingRef.current);
    } else {
      realTimePollingRef.current = window.setInterval(() => {
        queryData();
      }, REAL_TIME_POLLING_MS);
    }

    return () => {
      window.clearInterval(realTimePollingRef.current);
    };
  }, [realTimeStatisticsFlag, queryData]);

  const [cacheModelFlag, setCacheModelFlag] = useState(false);

  // 实时统计时图表不显示loading
  const loading = useMemo(() => {
    if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.OPEN) {
      return false;
    }
    return stopLoad(cacheModelFlag, queryLoading || false);
  }, [cacheModelFlag, queryLoading, realTimeStatisticsFlag]);

  // 计算各种数据
  const dashboardResult = useMemo(() => {
    // 三层主机 Top
    const {
      /** DSCP 统计 */
      dscp,
      l3DevicesTop = {},
      ipConversationTop = {},

      histogram = [],
    } = serviceDashboardData;

    const { totalBytes: l3TotalBytes = [], totalSessions: l3TotalSessions = [] } =
      l3DevicesTop as IServiceDashboardData['l3DevicesTop'];

    // 通信对
    const { totalBytes: ipConverTotalBytes = [], totalSessions: ipConverTotalSessions = [] } =
      ipConversationTop as IServiceDashboardData['ipConversationTop'];

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

    // dscp 补点
    if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.CLOSED) {
      Object.keys(dscpStatLineData).forEach((key) => {
        dscpStatLineData[key] = completeTimePoint(
          dscpStatLineData[key],
          globalSelectedTime.startTime!,
          globalSelectedTime.endTime!,
          globalSelectedTime.interval,
        );
      });
    }

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
    if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.CLOSED) {
      Object.keys(data).forEach((metric) => {
        data[metric] = completeTimePoint(
          data[metric],
          globalSelectedTime.startTime!,
          globalSelectedTime.endTime!,
          globalSelectedTime.interval,
        );
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
      dscpStat: {
        lineChartData: dscpStatLineData,
        barChartData: dscpStatBarData,
      },
    };
  }, [
    realTimeStatisticsFlag,
    globalSelectedTime.endTime,
    globalSelectedTime.interval,
    globalSelectedTime.startTime,
    serviceDashboardData,
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
    if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.OPEN && !cacheModelFlag) {
      return undefined;
    }
    return updateGlobalTime;
  }, [realTimeStatisticsFlag, cacheModelFlag, updateGlobalTime]);

  const cacheState = useMemo(() => {
    return {
      serviceDashboardData,
      l3TopData:
        realTimeStatisticsFlag === ERealTimeStatisticsFlag.CLOSED
          ? l3TopData
          : dashboardResult.l3DevicesTopData,
      l3Conversation:
        realTimeStatisticsFlag === ERealTimeStatisticsFlag.CLOSED
          ? l3ConversationTop
          : dashboardResult.ipConversationTopData,
      dashboardResult,
    };
  }, [dashboardResult, l3ConversationTop, l3TopData, realTimeStatisticsFlag, serviceDashboardData]);
  const [dashboardState, setDashboardState] = useState<any>(cacheState);

  return (
    <>
      <CacheStateBox
        cacheState={cacheState}
        onNewState={setDashboardState}
        onCacheModelFlag={setCacheModelFlag}
      />
      <DashboardPreview
        data={{
          ...dashboardState.serviceDashboardData,
          alertCounts: alertMsgCnt,
        }}
        loading={loading}
        realTimeStatisticsFlag={realTimeStatisticsFlag}
        selectedTimeInfo={globalSelectedTime}
      />

      <section className={styles.content}>
        <Row gutter={10}>
          <Col span={12}>
            <MultipeSourceBar
              title="三层主机 Top"
              loading={l3TopLoading}
              data={dashboardState.l3TopData}
            />
          </Col>
          <Col span={12}>
            <MultipeSourceBar
              title="三层通讯对 Top"
              loading={l3ConversationLoading}
              data={dashboardState.l3Conversation}
            />
          </Col>
          <Col span={12}>
            <MultipleSourceLine
              title="分片包统计"
              loading={loading}
              dataList={dashboardState.dashboardResult.fragmentStat}
              brushMenus={[{ text: '数据包', key: 'packet' }]}
              networkId={networkId}
              serviceId={serviceId}
              disableChangeTime={cacheModelFlag}
              onBrush={handleChartBrush}
              markArea={beforeOldestPacketArea}
            />
          </Col>
          <Col span={12}>
            <MultipleSourceTrend
              percentTableValueTitle="占比"
              percentTableLabelTitle="DSCP类型"
              title="DSCP统计"
              loading={loading}
              data={dashboardState.dashboardResult.dscpStat}
              unitConverter={bytesToSize}
              brushMenus={[{ text: '数据包', key: 'packet' }]}
              networkId={networkId}
              serviceId={serviceId}
              disableChangeTime={cacheModelFlag}
              onBrush={handleChartBrush}
              markArea={beforeOldestPacketArea}
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
    npmdModel: { serviceDashboardData, beforeOldestPacketArea, alertMsgCnt },
    appModel: { globalSelectedTime, realTimeStatisticsFlag },
  }: ConnectState) => ({
    serviceDashboardData,
    globalSelectedTime,
    realTimeStatisticsFlag,
    beforeOldestPacketArea,
    queryLoading: effects['npmdModel/queryServiceDashboard'],
    alertMsgCnt,
  }),
)(ServiceDashboard);
