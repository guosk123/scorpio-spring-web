import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { jumpToSericeAnalysisTab } from '@/pages/app/analysis/Service/constant';
import { ServiceAnalysisContext } from '@/pages/app/analysis/Service/index';
import { EServiceTabs } from '@/pages/app/analysis/Service/typing';
import type { IUriParams } from '@/pages/app/analysis/typings';
import { ESourceType, REAL_TIME_POLLING_MS } from '@/pages/app/analysis/typings';
import type { INetworkMap } from '@/pages/app/Configuration/Network/typings';
import { bytesToSize, convertBandwidth } from '@/utils/utils';
import { SettingOutlined } from '@ant-design/icons';
import { Button, Col, Row } from 'antd';
import type { LineSeriesOption } from 'echarts/charts';
import moment from 'moment';
import numeral from 'numeral';
import React, { useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, ERealTimeStatisticsFlag, useParams } from 'umi';
import { AnalysisContext, NetworkTypeContext } from '../../Analysis';
import { ServiceContext } from '@/pages/app/analysis/Service/index';
import { jumpToAnalysisTabNew } from '../../Analysis/constant';
import { queryPayloadHistogram } from '../../service';
import type { INetworkTreeItem } from '../../typing';
import { ENetowrkType, ENetworkTabs } from '../../typing';
import WidgetChart, { EFormatterType } from '../WidgetChart';
import ChartMarkAreaDetail from '../Dashboard/components/ChartMarkAreaDetail';
import useAbortXhr from '../../hooks/useAbortXhr';

interface IPayloadAnalysisProps {
  dispatch: Dispatch;
  globalSelectedTime: Required<IGlobalTime>;
  realTimeStatisticsFlag?: ERealTimeStatisticsFlag;
  allNetworkMap?: INetworkMap;
}
const PayloadAnalysis: React.FC<IPayloadAnalysisProps> = ({
  globalSelectedTime,
  realTimeStatisticsFlag,
}) => {
  const realTimePollingRef = useRef<number | undefined>(undefined);
  const urlIds = useParams<IUriParams>();
  const { serviceId, networkId } = useMemo(() => {
    const tmpNetworkId = urlIds.networkId || '';
    if (tmpNetworkId.includes('^')) {
      return {
        serviceId: urlIds.serviceId,
        networkId: tmpNetworkId.split('^')[1],
      };
    }
    return { serviceId: urlIds.serviceId, networkId: urlIds.networkId };
  }, [urlIds.networkId, urlIds.serviceId]);

  const selectedTimeInfo = useMemo(() => {
    return globalSelectedTime;
  }, [globalSelectedTime]);

  const [payloadHistogramData, setPayloadHistogramData] = useState([]);
  const [networkType] = useContext<any>(serviceId ? ServiceContext : NetworkTypeContext) as [
    ENetowrkType,
  ];

  const sourceType: ESourceType = useMemo(() => {
    if (serviceId) {
      return ESourceType.SERVICE;
    }
    return ESourceType.NETWORK;
  }, [serviceId]);

  const [queryLoading, setQueryLoading] = useState(true);

  const onTimeInfoChange = () => {
    setQueryLoading(true);
  };

  // 实时统计时图表不显示loading
  const loading = useMemo(() => {
    if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.OPEN) {
      return false;
    }
    return queryLoading;
  }, [queryLoading, realTimeStatisticsFlag]);

  const queryDataFn = useCallback(async () => {
    setQueryLoading(true);
    // 组装DSL条件
    let dsl = '';
    dsl += `${
      networkType === ENetowrkType.NETWORK
        ? ` (network_id = "${networkId}" )${serviceId ? ` AND (service_id = "${serviceId}" )` : ''}`
        : ''
    } | gentimes timestamp start="${selectedTimeInfo.startTime}" end="${selectedTimeInfo.endTime}"`;
    const queryData = {
      sourceType,
      serviceId,
      startTime: selectedTimeInfo.startTime,
      endTime: selectedTimeInfo.endTime,
      interval: selectedTimeInfo.interval,
      dsl,
      realTime: realTimeStatisticsFlag,
    };
    queryData[networkType === ENetowrkType.NETWORK ? 'networkId' : 'networkGroupId'] = networkId;
    const { success, result } = await queryPayloadHistogram(queryData);
    setQueryLoading(false);
    if (!success) {
      return;
    }
    setPayloadHistogramData(result);
  }, [selectedTimeInfo, networkId, serviceId, realTimeStatisticsFlag, sourceType, networkType]);

  useEffect(() => {
    queryDataFn();
  }, [queryDataFn]);

  // 判断实时统计
  useEffect(() => {
    if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.CLOSED) {
      window.clearInterval(realTimePollingRef.current);
    } else {
      realTimePollingRef.current = window.setInterval(() => {
        queryDataFn();
      }, REAL_TIME_POLLING_MS);
    }

    return () => {
      window.clearInterval(realTimePollingRef.current);
    };
  }, [realTimeStatisticsFlag, queryDataFn]);

  // 计算数据
  const computeSeriesData = useMemo(() => {
    const tableData = [];

    // -----带宽曲线图-------
    // 带宽
    const bandwidthSeriesData = [];
    // 上周同期带宽
    const lastWeekSamePeriodBandwidthSeriesData = [];
    // 带宽基线
    const baselineBandwidthSeriesData = [];
    // 峰值带宽
    const peakBandwidthSeriesData = [];
    // 不捕获带宽
    const filterDiscardBandwidthSeriesData = [];
    // 超规格带宽
    const overloadDiscardBandwidthSeriesData = [];
    // 去重带宽
    const deduplicationBandwidthSeriesData = [];
    // 上行带宽
    const upstreamBandwidthSeriesData = [];
    // 下行带宽
    const downstreamBandwidthSeriesData = [];

    // -----流量曲线图-------
    // 总流量
    const totalByteSeriesData = [];
    // 上行流量
    const upstreamBytesSeriesData = [];
    // 下行流量
    const downstreamBytesSeriesData = [];
    // 上周同期流量
    const lastWeekSamePeriodTotalBytesSeriesData = [];
    // 基线流量
    const baselineTotalBytesSeriesData = [];
    // 不捕获流量
    const filterDiscardBytesSeriesData = [];
    // 超规格流量
    const overloadDiscardBytesSeriesData = [];
    // 去重流量
    const deduplicationBytesSeriesData = [];

    // -----并发量曲线图-------
    // 并发
    const concurrentSessionsSeriesData = [];
    // 新建
    const establishedSessionsSeriesData = [];
    // 独立用户数
    const uniqueIpCountsSeriesData = [];

    // -----数据包曲线图-------
    // 总包数
    const totalPacketsSeriesData = [];
    // 上行数据包
    const upstreamPacketsSeriesData = [];
    // 下行数据包
    const downstreamPacketsSeriesData = [];
    // 上周同期包数
    const lastWeekSamePeriodTotalPacketSeriesData = [];
    //  数据包基线
    const baselineTotalPacketsSeriesData = [];

    for (let index = 0; index < payloadHistogramData.length; index += 1) {
      const row = payloadHistogramData[index];
      const {
        timestamp,
        bandwidth = 0,
        upstreamBandwidth = 0,
        downstreamBandwidth = 0,
        lastWeekSamePeriodBandwidth = 0,
        baselineBandwidth = 0,
        filterDiscardBandwidth = 0,
        overloadDiscardBandwidth = 0,
        deduplicationBandwidth = 0,

        totalBytes = 0,
        upstreamBytes = 0,
        downstreamBytes = 0,
        bytepsPeak = 0,
        lastWeekSamePeriodTotalBytes = 0,
        baselineTotalBytes = 0,
        filterDiscardBytes = 0,
        overloadDiscardBytes = 0,
        deduplicationBytes = 0,

        totalPackets = 0,
        upstreamPackets = 0,
        downstreamPackets = 0,
        lastWeekSamePeriodTotalPackets = 0,
        baselineTotalPackets = 0,

        concurrentSessions = 0,
        establishedSessions = 0,
        uniqueIpCounts = 0,
      } = row;
      const timeText = moment(timestamp).format('YYYY-MM-DD HH:mm:ss');
      const timestampNum = new Date(timestamp).valueOf();

      const currentBandwidth =
        realTimeStatisticsFlag === ERealTimeStatisticsFlag.OPEN ? totalBytes * 8 : bandwidth;

      tableData.push({
        timestamp: timeText,
        bandwidth: convertBandwidth(currentBandwidth),
        upstreamBandwidth: convertBandwidth(upstreamBandwidth),
        downstreamBandwidth: convertBandwidth(downstreamBandwidth),
        lastWeekSamePeriodBandwidth: convertBandwidth(lastWeekSamePeriodBandwidth),
        baselineBandwidth: convertBandwidth(baselineBandwidth),
        filterDiscardBandWidth: convertBandwidth(filterDiscardBandwidth),
        overloadDiscardBandWidth: convertBandwidth(overloadDiscardBandwidth),
        deduplicationBandWidth: convertBandwidth(deduplicationBandwidth),
        bytepsPeakByBandWidth: convertBandwidth(bytepsPeak * 8),

        totalBytes: bytesToSize(totalBytes),
        upstreamBytes: bytesToSize(upstreamBytes),
        downstreamBytes: bytesToSize(downstreamBytes),
        lastWeekSamePeriodTotalBytes: bytesToSize(lastWeekSamePeriodTotalBytes),
        baselineTotalBytes: bytesToSize(baselineTotalBytes),
        filterDiscardBytes: bytesToSize(filterDiscardBytes),
        overloadDiscardBytes: bytesToSize(overloadDiscardBytes),
        deduplicationBytes: bytesToSize(deduplicationBytes),

        totalPackets: numeral(totalPackets).format('0,0'),
        upstreamPackets: numeral(upstreamPackets).format('0,0'),
        downstreamPackets: numeral(downstreamPackets).format('0,0'),
        lastWeekSamePeriodTotalPackets: numeral(lastWeekSamePeriodTotalPackets).format('0,0'),
        baselineTotalPackets: numeral(baselineTotalPackets).format('0,0'),

        concurrentSessions: numeral(concurrentSessions).format('0,0'),
        establishedSessions: numeral(establishedSessions).format('0,0'),
        uniqueIpCounts: numeral(uniqueIpCounts).format('0,0'),
      });

      // 带宽
      bandwidthSeriesData.push([timestampNum, currentBandwidth]);
      upstreamBandwidthSeriesData.push([timestampNum, upstreamBandwidth]);
      downstreamBandwidthSeriesData.push([timestampNum, downstreamBandwidth]);
      lastWeekSamePeriodBandwidthSeriesData.push([timestampNum, lastWeekSamePeriodBandwidth]);
      baselineBandwidthSeriesData.push([timestampNum, baselineBandwidth]);
      peakBandwidthSeriesData.push([timestampNum, bytepsPeak * 8]);
      filterDiscardBandwidthSeriesData.push([timestampNum, filterDiscardBandwidth]);
      overloadDiscardBandwidthSeriesData.push([timestampNum, overloadDiscardBandwidth]);
      deduplicationBandwidthSeriesData.push([timestampNum, deduplicationBandwidth]);
      // 流量
      totalByteSeriesData.push([timestampNum, totalBytes]);
      upstreamBytesSeriesData.push([timestampNum, upstreamBytes]);
      downstreamBytesSeriesData.push([timestampNum, downstreamBytes]);
      lastWeekSamePeriodTotalBytesSeriesData.push([timestampNum, lastWeekSamePeriodTotalBytes]);
      baselineTotalBytesSeriesData.push([timestampNum, baselineTotalBytes]);
      filterDiscardBytesSeriesData.push([timestampNum, filterDiscardBytes]);
      overloadDiscardBytesSeriesData.push([timestampNum, overloadDiscardBytes]);
      deduplicationBytesSeriesData.push([timestampNum, deduplicationBytes]);
      // 并发量图
      concurrentSessionsSeriesData.push([timestampNum, concurrentSessions]);
      establishedSessionsSeriesData.push([timestampNum, establishedSessions]);
      uniqueIpCountsSeriesData.push([timestampNum, uniqueIpCounts]);
      // 数据包图
      totalPacketsSeriesData.push([timestampNum, totalPackets]);
      upstreamPacketsSeriesData.push([timestampNum, upstreamPackets]);
      downstreamPacketsSeriesData.push([timestampNum, downstreamPackets]);
      lastWeekSamePeriodTotalPacketSeriesData.push([timestampNum, lastWeekSamePeriodTotalPackets]);
      baselineTotalPacketsSeriesData.push([timestampNum, baselineTotalPackets]);
    }

    return {
      tableData,

      bandwidthSeriesData,
      lastWeekSamePeriodBandwidthSeriesData,
      baselineBandwidthSeriesData,
      peakBandwidthSeriesData,
      filterDiscardBandwidthSeriesData,
      overloadDiscardBandwidthSeriesData,
      deduplicationBandwidthSeriesData,
      upstreamBandwidthSeriesData,
      downstreamBandwidthSeriesData,

      totalByteSeriesData,
      upstreamBytesSeriesData,
      downstreamBytesSeriesData,
      lastWeekSamePeriodTotalBytesSeriesData,
      baselineTotalBytesSeriesData,
      filterDiscardBytesSeriesData,
      overloadDiscardBytesSeriesData,
      deduplicationBytesSeriesData,

      concurrentSessionsSeriesData,
      establishedSessionsSeriesData,
      uniqueIpCountsSeriesData,

      totalPacketsSeriesData,
      upstreamPacketsSeriesData,
      downstreamPacketsSeriesData,
      lastWeekSamePeriodTotalPacketSeriesData,
      baselineTotalPacketsSeriesData,
    };
  }, [payloadHistogramData, realTimeStatisticsFlag]);

  // 带宽曲线图
  const bandwidthDimension = useMemo(() => {
    const chartSeries: LineSeriesOption[] = [
      {
        name: '总带宽',
        type: 'line',
        areaStyle: {},
        data: computeSeriesData.bandwidthSeriesData,
      },
    ];
    const tableColumns = [
      {
        title: '时间',
        dataIndex: 'timestamp',
      },
      {
        title: '总带宽',
        dataIndex: 'bandwidth',
      },
    ];

    // 只有网络且非实时统计
    if (networkId && !serviceId && realTimeStatisticsFlag === ERealTimeStatisticsFlag.CLOSED) {
      chartSeries.push(
        {
          name: '上行带宽',
          type: 'line',
          areaStyle: {},
          data: computeSeriesData.upstreamBandwidthSeriesData,
        },
        {
          name: '下行带宽',
          type: 'line',
          areaStyle: {},
          data: computeSeriesData.downstreamBandwidthSeriesData,
        },
      );
      tableColumns.push(
        {
          title: '上行带宽',
          dataIndex: 'upstreamBandwidth',
        },
        {
          title: '下行带宽',
          dataIndex: 'downstreamBandwidth',
        },
      );
    }

    // 网络或业务
    if (networkId || serviceId) {
      chartSeries.push(
        {
          name: '上周同期带宽',
          type: 'line',
          areaStyle: {},
          data: computeSeriesData.lastWeekSamePeriodBandwidthSeriesData,
        },
        {
          name: '带宽基线',
          type: 'line',
          data: computeSeriesData.baselineBandwidthSeriesData,
        },
      );
      tableColumns.push(
        {
          title: '上周同期带宽',
          dataIndex: 'lastWeekSamePeriodBandwidth',
        },
        {
          title: '带宽基线',
          dataIndex: 'baselineBandwidth',
        },
      );
      if (networkType === ENetowrkType.NETWORK) {
        /** 仅在选择网路时展示带宽峰值 */
        chartSeries.push({
          name: '带宽峰值',
          type: 'line',
          data: computeSeriesData.peakBandwidthSeriesData,
        });
        tableColumns.push({
          title: '带宽峰值',
          dataIndex: 'bytepsPeakByBandWidth',
        });
      }
    }
    // 仅主网络时需要添加这三个统计项
    // if (allNetworkMap[networkId] && !serviceId) {
    //   chartSeries.push(
    //     {
    //       name: '不捕获带宽',
    //       type: 'line',
    //       data: computeSeriesData.filterDiscardBandwidthSeriesData,
    //     },
    //     {
    //       name: '超规格带宽',
    //       type: 'line',
    //       data: computeSeriesData.overloadDiscardBandwidthSeriesData,
    //     },
    //     {
    //       name: '去重带宽',
    //       type: 'line',
    //       data: computeSeriesData.deduplicationBandwidthSeriesData,
    //     },
    //   );
    //   tableColumns.push(
    //     {
    //       title: '不捕获带宽',
    //       dataIndex: 'filterDiscardBandWidth',
    //     },
    //     {
    //       title: '超规格带宽',
    //       dataIndex: 'overloadDiscardBandWidth',
    //     },
    //     {
    //       title: '去重带宽',
    //       dataIndex: 'deduplicationBandWidth',
    //     },
    //   );
    // }

    return {
      chartSeries,
      tableColumns,
    };
  }, [
    computeSeriesData.bandwidthSeriesData,
    computeSeriesData.baselineBandwidthSeriesData,
    computeSeriesData.downstreamBandwidthSeriesData,
    computeSeriesData.lastWeekSamePeriodBandwidthSeriesData,
    // computeSeriesData.peakBandwidthSeriesData,
    computeSeriesData.upstreamBandwidthSeriesData,
    networkId,
    realTimeStatisticsFlag,
    serviceId,
  ]);

  // 流量曲线图
  const flowDimension = useMemo(() => {
    const chartSeries: LineSeriesOption[] = [
      {
        name: '总流量',
        type: 'line',
        areaStyle: {},
        data: computeSeriesData.totalByteSeriesData,
      },
    ];
    const tableColumns = [
      {
        title: '时间',
        dataIndex: 'timestamp',
      },
      {
        title: '总流量',
        dataIndex: 'totalBytes',
      },
    ];

    // 网络
    if (networkId && !serviceId) {
      chartSeries.push(
        {
          name: '上行流量',
          type: 'line',
          areaStyle: {},
          data: computeSeriesData.upstreamBytesSeriesData,
        },
        {
          name: '下行流量',
          type: 'line',
          areaStyle: {},
          data: computeSeriesData.downstreamBytesSeriesData,
        },
      );
      tableColumns.push(
        {
          title: '上行流量',
          dataIndex: 'upstreamBytes',
        },
        {
          title: '下行流量',
          dataIndex: 'downstreamBytes',
        },
      );
    }

    // 主网络
    // if (allNetworkMap[networkId] && !serviceId) {
    //   chartSeries.push(
    //     {
    //       name: '不捕获流量',
    //       type: 'line',
    //       data: computeSeriesData.filterDiscardBytesSeriesData,
    //     },
    //     {
    //       name: '超规格流量',
    //       type: 'line',
    //       data: computeSeriesData.overloadDiscardBytesSeriesData,
    //     },
    //     {
    //       name: '去重流量',
    //       type: 'line',
    //       data: computeSeriesData.deduplicationBytesSeriesData,
    //     },
    //   );
    //   tableColumns.push(
    //     {
    //       title: '不捕获流量',
    //       dataIndex: 'filterDiscardBytes',
    //     },
    //     {
    //       title: '超规格流量',
    //       dataIndex: 'overloadDiscardBytes',
    //     },
    //     {
    //       title: '去重流量',
    //       dataIndex: 'deduplicationBytes',
    //     },
    //   );
    // } else {
    //   // 子网络或业务
    // }

    // 网络或业务
    if (networkId || serviceId) {
      chartSeries.push(
        {
          name: '上周同期流量',
          type: 'line',
          areaStyle: {},
          data: computeSeriesData.lastWeekSamePeriodTotalBytesSeriesData,
        },
        {
          name: '流量基线',
          type: 'line',
          data: computeSeriesData.baselineTotalBytesSeriesData,
        },
      );
      tableColumns.push(
        {
          title: '上周同期流量',
          dataIndex: 'lastWeekSamePeriodTotalBytes',
        },
        {
          title: '流量基线',
          dataIndex: 'baselineTotalBytes',
        },
      );
    }

    return {
      chartSeries,
      tableColumns,
    };
  }, [
    computeSeriesData.baselineTotalBytesSeriesData,
    computeSeriesData.downstreamBytesSeriesData,
    computeSeriesData.lastWeekSamePeriodTotalBytesSeriesData,
    computeSeriesData.totalByteSeriesData,
    computeSeriesData.upstreamBytesSeriesData,
    networkId,
    serviceId,
  ]);
  // 并发量曲线图
  const sessionDimension = useMemo(() => {
    const chartSeries: LineSeriesOption[] = [
      {
        name: '并发',
        type: 'line',
        data: computeSeriesData.concurrentSessionsSeriesData,
      },
      {
        name: '新建',
        type: 'line',
        data: computeSeriesData.establishedSessionsSeriesData,
      },
      {
        name: '独立用户数',
        type: 'line',
        data: computeSeriesData.uniqueIpCountsSeriesData,
      },
    ];
    const tableColumns = [
      {
        title: '时间',
        dataIndex: 'timestamp',
      },
      {
        title: '并发',
        dataIndex: 'concurrentSessions',
      },
      {
        title: '新建',
        dataIndex: 'establishedSessions',
      },
      {
        title: '独立用户数',
        dataIndex: 'uniqueIpCounts',
      },
    ];

    return {
      chartSeries,
      tableColumns,
    };
  }, [
    computeSeriesData.concurrentSessionsSeriesData,
    computeSeriesData.establishedSessionsSeriesData,
    computeSeriesData.uniqueIpCountsSeriesData,
  ]);
  // 数据包曲线图
  const packetDimension = useMemo(() => {
    const chartSeries: LineSeriesOption[] = [
      {
        name: '总数据包数',
        type: 'line',
        areaStyle: {},
        data: computeSeriesData.totalPacketsSeriesData,
      },
    ];
    const tableColumns = [
      {
        title: '时间',
        dataIndex: 'timestamp',
      },
      {
        title: '总数据包数',
        dataIndex: 'totalPackets',
      },
    ];
    // 网络
    if (networkId && !serviceId) {
      chartSeries.push(
        {
          name: '上行数据包',
          type: 'line',
          areaStyle: {},
          data: computeSeriesData.upstreamPacketsSeriesData,
        },
        {
          name: '下行数据包',
          type: 'line',
          areaStyle: {},
          data: computeSeriesData.downstreamPacketsSeriesData,
        },
      );
      tableColumns.push(
        {
          title: '上行数据包',
          dataIndex: 'upstreamPackets',
        },
        {
          title: '下行数据包',
          dataIndex: 'downstreamPackets',
        },
      );
    }

    if (networkId || serviceId) {
      chartSeries.push(
        {
          name: '上周同期包数',
          type: 'line',
          areaStyle: {},
          data: computeSeriesData.lastWeekSamePeriodTotalPacketSeriesData,
        },
        {
          name: '数据包基线',
          type: 'line',
          data: computeSeriesData.baselineTotalPacketsSeriesData,
        },
      );
      tableColumns.push(
        {
          title: '上周同期包数',
          dataIndex: 'lastWeekSamePeriodTotalPackets',
        },
        {
          title: '数据包基线',
          dataIndex: 'baselineTotalPackets',
        },
      );
    }
    return {
      chartSeries,
      tableColumns,
    };
  }, [
    computeSeriesData.baselineTotalPacketsSeriesData,
    computeSeriesData.downstreamPacketsSeriesData,
    computeSeriesData.lastWeekSamePeriodTotalPacketSeriesData,
    computeSeriesData.totalPacketsSeriesData,
    computeSeriesData.upstreamPacketsSeriesData,
    networkId,
    serviceId,
  ]);

  const [state, contextDispatch] = useContext(serviceId ? ServiceAnalysisContext : AnalysisContext);

  const [markArea, setMarkArea] = useState<any>({});

  const markAreaProps = useMemo(() => {
    if (networkType === ENetowrkType.NETWORK) {
      return { networkId };
    } else {
      return { networkGroupId: networkId };
    }
  }, [networkId, networkType]);

  useAbortXhr({
    cancelUrls: ['/central/packet-oldest-time', '/metric/networks/payload'],
  });

  return (
    <>
      <ChartMarkAreaDetail
        {...markAreaProps}
        markAreaDetail={setMarkArea}
        globalSelectedTime={globalSelectedTime}
      />
      <div style={{ textAlign: 'right', marginBottom: 10 }}>
        <Button
          type="primary"
          icon={<SettingOutlined />}
          onClick={() => {
            jumpToAnalysisTabNew(state, contextDispatch, ENetworkTabs.BASELINE);
          }}
        >
          负载量配置
        </Button>
      </div>
      <Row gutter={10}>
        <Col span={12} style={{ marginBottom: 10 }} key="带宽曲线图">
          <WidgetChart
            title="带宽曲线图"
            selectedTime={selectedTimeInfo}
            tableColumns={bandwidthDimension.tableColumns}
            loading={loading}
            tableData={computeSeriesData.tableData}
            chartSeries={bandwidthDimension.chartSeries}
            formatterType={EFormatterType.bps}
            onChangeTime={onTimeInfoChange}
            markArea={markArea}
          />
        </Col>
        <Col span={12} style={{ marginBottom: 10 }} key="流量曲线图">
          <WidgetChart
            title="流量曲线图"
            selectedTime={selectedTimeInfo}
            tableColumns={flowDimension.tableColumns}
            loading={loading}
            tableData={computeSeriesData.tableData}
            chartSeries={flowDimension.chartSeries}
            formatterType={EFormatterType.bytes}
            onChangeTime={onTimeInfoChange}
            markArea={markArea}
          />
        </Col>
        <Col span={12} style={{ marginBottom: 10 }} key="并发量曲线图">
          <WidgetChart
            title="并发量曲线图"
            selectedTime={selectedTimeInfo}
            tableColumns={sessionDimension.tableColumns}
            loading={loading}
            tableData={computeSeriesData.tableData}
            chartSeries={sessionDimension.chartSeries}
            formatterType={EFormatterType.count}
            onChangeTime={onTimeInfoChange}
            markArea={markArea}
          />
        </Col>
        <Col span={12} style={{ marginBottom: 10 }} key="数据包数线图">
          <WidgetChart
            title="数据包数曲线图"
            selectedTime={selectedTimeInfo}
            tableColumns={packetDimension.tableColumns}
            loading={loading}
            tableData={computeSeriesData.tableData}
            chartSeries={packetDimension.chartSeries}
            formatterType={EFormatterType.count}
            onChangeTime={onTimeInfoChange}
            markArea={markArea}
          />
        </Col>
      </Row>
    </>
  );
};

export default connect(
  ({
    loading: { effects },
    networkModel: { allNetworkMap },
    appModel: { globalSelectedTime, realTimeStatisticsFlag },
  }: ConnectState) => ({
    globalSelectedTime,
    realTimeStatisticsFlag,
    allNetworkMap,
    queryLoading: effects['npmdModel/queryPayloadHistogram'],
  }),
)(PayloadAnalysis);
