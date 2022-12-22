import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { SettingOutlined } from '@ant-design/icons';
import type { RadioChangeEvent } from 'antd';
import { Button, Col, Radio, Row, Space } from 'antd';
import type { BarSeriesOption } from 'echarts';
import type { LineSeriesOption } from 'echarts/charts';
import moment from 'moment';
import numeral from 'numeral';
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, ERealTimeStatisticsFlag, history, useParams } from 'umi';
import CacheStateBox, { stopLoad } from '../components/CacheStateBox';
import WidgetChart, { EFormatterType } from '../components/WidgetChart';
import { stopRefreshPageFlag } from '../Network/Dashboard';
import type { IMetricQueryParams, IPerformanceStatData, IUriParams } from '../typings';
import { ESourceType, REAL_TIME_POLLING_MS } from '../typings';

enum NetworkType {
  TOTAL = 'total',
  INSIDE = 'insideService',
  OUTSIDE = 'outsideService',
}

const NETWORK_NAME = {
  [NetworkType.TOTAL]: '全部',
  [NetworkType.INSIDE]: '内网',
  [NetworkType.OUTSIDE]: '外网',
};

const NETWORK_METRIC_SUFFIX = {
  [NetworkType.TOTAL]: '',
  [NetworkType.INSIDE]: 'InsideService',
  [NetworkType.OUTSIDE]: 'OutsideService',
};

interface IPerformanceAnalysisProps {
  dispatch: Dispatch;
  globalSelectedTime: Required<IGlobalTime>;
  realTimeStatisticsFlag: ERealTimeStatisticsFlag;
  performanceHistogramData: IPerformanceStatData[];
  location: {
    pathname: string;
  };
  queryLoading: boolean | undefined;
  beforeOldestPacketArea?: any;
}

const PerformanceAnalysis: React.FC<IPerformanceAnalysisProps> = ({
  location: { pathname },
  dispatch,
  globalSelectedTime,
  realTimeStatisticsFlag,
  performanceHistogramData = [],
  queryLoading = false,
  beforeOldestPacketArea,
}) => {
  const realTimePollingRef = useRef<number | undefined>(undefined);
  const { networkId, serviceId, pcapFileId }: IUriParams = useParams();

  const [networkType, setNetworkType] = useState<NetworkType>(NetworkType.TOTAL);

  const sourceType: ESourceType = useMemo(() => {
    if (serviceId) {
      return ESourceType.SERVICE;
    }
    if (networkId) {
      return ESourceType.NETWORK;
    }
    return ESourceType.OFFLINE;
  }, [serviceId, networkId]);

  const selectedTimeInfo = useMemo(() => {
    // if (pcapFileId) {
    //   return timeFormatter(pcapDetail?.filterStartTime, pcapDetail?.filterEndTime);
    // }
    return globalSelectedTime;
  }, [globalSelectedTime]);

  const queryData = useCallback(() => {
    // 组装DSL条件
    let dsl = '';
    if (!pcapFileId) {
      dsl += '(';
      if (networkId) {
        dsl += `network_id="${networkId}"`;
      }
      if (serviceId) {
        dsl += ` AND service_id="${serviceId}"`;
      }
      dsl += ')';
    } else {
      dsl = `(network_id="${pcapFileId}")`;
    }

    dsl += ` | gentimes timestamp start="${selectedTimeInfo.startTime}" end="${selectedTimeInfo.endTime}"`;

    dispatch({
      type: 'npmdModel/queryPerformanceHistogram',
      payload: {
        sourceType,
        networkId,
        serviceId,
        packetFileId: pcapFileId,
        startTime: selectedTimeInfo.startTime,
        endTime: selectedTimeInfo.endTime,
        interval: selectedTimeInfo.interval,
        dsl,
        realTime: realTimeStatisticsFlag,
      } as IMetricQueryParams,
    });
  }, [
    selectedTimeInfo.startTime,
    selectedTimeInfo.endTime,
    selectedTimeInfo.interval,
    dispatch,
    sourceType,
    serviceId,
    realTimeStatisticsFlag,
    networkId,
    pcapFileId,
  ]);

  useEffect(() => {
    queryData();
  }, [queryData]);

  // 实时统计
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

  // 计算数据
  const computeSeriesData = useMemo(() => {
    const tableData = [];

    // 服务器响应时间曲线图
    const serverLatencyChartData: (LineSeriesOption | BarSeriesOption)[] = [
      {
        name: '平均响应时间',
        type: 'bar',
        barGap: 0,
        data: [],
      },
      {
        name: '峰值响应时间',
        type: 'bar',
        barGap: 0,
        data: [],
      },
    ];

    if (networkId || serviceId) {
      serverLatencyChartData.push(
        {
          name: '上周同期响应时间',
          type: 'bar',
          barGap: 0,
          data: [],
        },
        {
          name: '响应时间基线',
          type: 'line',
          data: [],
        },
      );
    }

    // 服务器响应时间分布曲线图
    const responseSpeedChartData: BarSeriesOption[] = [
      {
        name: '迅速响应个数',
        type: 'bar',
        stack: '总量',
        data: [],
      },
      {
        name: '正常响应个数',
        type: 'bar',
        stack: '总量',
        data: [],
      },
      {
        name: '超时响应个数',
        type: 'bar',
        stack: '总量',
        data: [],
      },
    ];
    // 客户端和服务器网络时延
    const latencyChartData: LineSeriesOption[] = [
      {
        name: '客户端时延',
        type: 'line',
        data: [],
      },
      {
        name: '服务器时延',
        type: 'line',
        data: [],
      },
    ];

    const retransPacketChartData: BarSeriesOption[] = [
      {
        name: '客户端重传',
        type: 'bar',
        barGap: 0,
        data: [],
      },
      {
        name: '服务器重传',
        type: 'bar',
        barGap: 0,
        data: [],
      },
    ];

    const suffix = NETWORK_METRIC_SUFFIX[networkType];

    for (let index = 0; index < performanceHistogramData.length; index += 1) {
      const row = performanceHistogramData[index];

      const serverResponseLatencyAvg = row[`serverResponseLatencyAvg${suffix}`] || 0;
      const lastWeekSamePeriodServerResponseLatencyAvg =
        row[`lastWeekSamePeriodServerResponseLatencyAvg${suffix}`] || 0;
      const serverResponseLatencyPeak = row[`serverResponseLatencyPeak${suffix}`] || 0;

      const serverResponseFastCounts = row[`serverResponseFastCounts${suffix}`] || 0;
      const serverResponseNormalCounts = row[`serverResponseNormalCounts${suffix}`] || 0;
      const serverResponseTimeoutCounts = row[`serverResponseTimeoutCounts${suffix}`] || 0;

      const tcpClientNetworkLatencyAvg = row[`tcpClientNetworkLatencyAvg${suffix}`] || 0;
      const tcpServerNetworkLatencyAvg = row[`tcpServerNetworkLatencyAvg${suffix}`] || 0;

      const tcpClientRetransmissionPackets = row[`tcpClientRetransmissionPackets${suffix}`] || 0;
      const tcpServerRetransmissionPackets = row[`tcpServerRetransmissionPackets${suffix}`] || 0;

      const { timestamp, baselineServerResponseLatencyAvg } = row;
      const timeText = moment(timestamp).format('YYYY-MM-DD HH:mm:ss');
      const timestampNum = new Date(timestamp).valueOf();
      tableData.push({
        timestamp: timeText,
        serverResponseLatencyAvg: `${numeral(serverResponseLatencyAvg).format('0,0')}ms`,
        lastWeekSamePeriodServerResponseLatencyAvg: `${numeral(
          lastWeekSamePeriodServerResponseLatencyAvg,
        ).format('0,0')}ms`,
        serverResponseLatencyPeak: `${numeral(serverResponseLatencyPeak).format('0,0')}ms`,
        baselineServerResponseLatencyAvg: `${numeral(baselineServerResponseLatencyAvg).format(
          '0,0',
        )}ms`,

        serverResponseFastCounts: numeral(serverResponseFastCounts).format('0,0'),
        serverResponseNormalCounts: numeral(serverResponseNormalCounts).format('0,0'),
        serverResponseTimeoutCounts: numeral(serverResponseTimeoutCounts).format('0,0'),

        tcpClientNetworkLatencyAvg: `${numeral(tcpClientNetworkLatencyAvg).format('0,0')}ms`,
        tcpServerNetworkLatencyAvg: `${numeral(tcpServerNetworkLatencyAvg).format('0,0')}ms`,

        tcpClientRetransmissionPackets: numeral(tcpClientRetransmissionPackets).format('0,0'),
        tcpServerRetransmissionPackets: numeral(tcpServerRetransmissionPackets).format('0,0'),
      });

      // 响应时间
      serverLatencyChartData[0]?.data!.push([timestampNum, serverResponseLatencyAvg]);
      serverLatencyChartData[1]?.data!.push([timestampNum, serverResponseLatencyPeak]);

      // 查看网络和业务时
      if (networkId || serviceId) {
        serverLatencyChartData[2]?.data!.push([
          timestampNum,
          lastWeekSamePeriodServerResponseLatencyAvg,
        ]);
        serverLatencyChartData[3]?.data!.push([timestampNum, baselineServerResponseLatencyAvg]);
      }

      // 响应时间分布
      responseSpeedChartData[0]?.data!.push([timestampNum, serverResponseFastCounts]);
      responseSpeedChartData[1]?.data!.push([timestampNum, serverResponseNormalCounts]);
      responseSpeedChartData[2]?.data!.push([timestampNum, serverResponseTimeoutCounts]);
      // 网络时延
      latencyChartData[0]?.data!.push([timestampNum, tcpClientNetworkLatencyAvg]);
      latencyChartData[1]?.data!.push([timestampNum, tcpServerNetworkLatencyAvg]);
      // 网络重传量
      retransPacketChartData[0]?.data!.push([timestampNum, tcpClientRetransmissionPackets]);
      retransPacketChartData[1]?.data!.push([timestampNum, tcpServerRetransmissionPackets]);
    }

    return {
      tableData,
      serverLatencyChartData,
      responseSpeedChartData,
      latencyChartData,
      retransPacketChartData,
    };
  }, [networkId, networkType, performanceHistogramData, serviceId]);

  const cacheState = useMemo(() => {
    return {
      computeSeriesData,
    };
  }, [computeSeriesData]);

  const [payloadState, setPayloadState] = useState(cacheState);

  const isRefreshPage = stopRefreshPageFlag(pathname);

  const handleNetworkTypeChange = (e: RadioChangeEvent) => {
    setNetworkType(e.target.value);
  };

  return (
    <>
      {!pcapFileId && (
        <Row justify="end">
          <Space style={{ textAlign: 'right', marginBottom: 10 }}>
            <Radio.Group
              options={Object.keys(NetworkType).map((key) => {
                return {
                  label: NETWORK_NAME[NetworkType[key]],
                  value: NetworkType[key],
                };
              })}
              onChange={handleNetworkTypeChange}
              value={networkType}
              optionType="button"
            />
            <Button
              type="primary"
              icon={<SettingOutlined />}
              onClick={() => history.push(`${pathname}/setting`)}
            >
              响应时间配置
            </Button>
          </Space>
        </Row>
      )}
      <CacheStateBox
        cacheState={cacheState}
        onNewState={setPayloadState}
        onCacheModelFlag={setCacheModelFlag}
      />
      <Row gutter={10}>
        {/* '服务器响应时间曲线图', '服务器响应时间分布曲线图', '客户端和服务器网络时延', '网络重传量' */}
        <Col span={12} style={{ marginBottom: 10 }} key="服务器响应时间曲线图">
          <WidgetChart
            title="服务器响应时间曲线图"
            selectedTime={selectedTimeInfo}
            tableColumns={[
              {
                title: '时间',
                dataIndex: 'timestamp',
              },
              {
                title: '平均响应时间',
                dataIndex: 'serverResponseLatencyAvg',
              },
              {
                title: '上周同期响应时间',
                dataIndex: 'lastWeekSamePeriodServerResponseLatencyAvg',
              },
              ...(networkId || serviceId
                ? [
                    {
                      // TODO: 时间粒度最小时，无峰值时间
                      title: '峰值响应时间',
                      dataIndex: 'serverResponseLatencyPeak',
                    },
                    {
                      title: '响应时间基线',
                      dataIndex: 'baselineServerResponseLatencyAvg',
                    },
                  ]
                : []),
            ]}
            loading={loading}
            tableData={payloadState.computeSeriesData.tableData}
            chartSeries={payloadState.computeSeriesData.serverLatencyChartData as any}
            formatterType={EFormatterType.ms}
            markArea={beforeOldestPacketArea}
            disableChangeTime={cacheModelFlag}
            isRefreshPage={isRefreshPage}
          />
        </Col>
        <Col span={12} style={{ marginBottom: 10 }} key="服务器响应时间分布曲线图">
          <WidgetChart
            title="服务器响应时间分布曲线图"
            selectedTime={selectedTimeInfo}
            tableColumns={[
              {
                title: '时间',
                dataIndex: 'timestamp',
              },
              {
                title: '迅速响应数量',
                dataIndex: 'serverResponseFastCounts',
              },
              {
                title: '正常响应数量',
                dataIndex: 'serverResponseNormalCounts',
              },
              {
                title: '超时响应数量',
                dataIndex: 'serverResponseTimeoutCounts',
              },
            ]}
            loading={loading}
            tableData={payloadState.computeSeriesData.tableData}
            chartSeries={payloadState.computeSeriesData.responseSpeedChartData as any}
            formatterType={EFormatterType.count}
            markArea={beforeOldestPacketArea}
            disableChangeTime={cacheModelFlag}
            isRefreshPage={isRefreshPage}
          />
        </Col>
        <Col span={12} style={{ marginBottom: 10 }} key="客户端和服务器网络时延">
          <WidgetChart
            title="客户端和服务器网络时延"
            selectedTime={selectedTimeInfo}
            tableColumns={[
              {
                title: '时间',
                dataIndex: 'timestamp',
              },
              {
                title: '客户端时延',
                dataIndex: 'tcpClientNetworkLatencyAvg',
              },
              {
                title: '服务器时延',
                dataIndex: 'tcpServerNetworkLatencyAvg',
              },
            ]}
            loading={loading}
            tableData={payloadState.computeSeriesData.tableData}
            chartSeries={payloadState.computeSeriesData.latencyChartData}
            formatterType={EFormatterType.ms}
            markArea={beforeOldestPacketArea}
            disableChangeTime={cacheModelFlag}
            isRefreshPage={isRefreshPage}
          />
        </Col>
        <Col span={12} style={{ marginBottom: 10 }} key="网络重传量">
          <WidgetChart
            title="网络重传量"
            selectedTime={selectedTimeInfo}
            tableColumns={[
              {
                title: '时间',
                dataIndex: 'timestamp',
              },
              {
                title: '客户端重传',
                dataIndex: 'tcpClientRetransmissionPackets',
              },
              {
                title: '服务器重传',
                dataIndex: 'tcpServerRetransmissionPackets',
              },
            ]}
            loading={loading}
            tableData={payloadState.computeSeriesData.tableData}
            chartSeries={payloadState.computeSeriesData.retransPacketChartData as any}
            formatterType={EFormatterType.count}
            markArea={beforeOldestPacketArea}
            disableChangeTime={cacheModelFlag}
            isRefreshPage={isRefreshPage}
          />
        </Col>
      </Row>
    </>
  );
};

export default connect(
  ({
    loading: { effects },
    npmdModel: { performanceHistogramData, beforeOldestPacketArea },
    appModel: { globalSelectedTime, realTimeStatisticsFlag },
  }: ConnectState) => ({
    globalSelectedTime,
    beforeOldestPacketArea,
    realTimeStatisticsFlag,
    performanceHistogramData,
    queryLoading: effects['npmdModel/queryPerformanceHistogram'],
  }),
)(PerformanceAnalysis);
