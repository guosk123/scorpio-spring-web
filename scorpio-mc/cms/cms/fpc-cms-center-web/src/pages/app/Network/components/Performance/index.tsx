import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { jumpToSericeAnalysisTab } from '@/pages/app/analysis/Service/constant';
import { ServiceAnalysisContext, ServiceContext } from '@/pages/app/analysis/Service/index';
import { EServiceTabs } from '@/pages/app/analysis/Service/typing';
import type { IPerformanceStatData } from '@/pages/app/analysis/typings';
import { ESourceType, REAL_TIME_POLLING_MS } from '@/pages/app/analysis/typings';
import { SettingOutlined } from '@ant-design/icons';
import type { RadioChangeEvent } from 'antd';
import { Button, Col, Radio, Row, Space } from 'antd';
import type { BarSeriesOption } from 'echarts';
import type { LineSeriesOption } from 'echarts/charts';
import moment from 'moment';
import numeral from 'numeral';
import React, { useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, ERealTimeStatisticsFlag, useParams } from 'umi';
import { AnalysisContext, NetworkTypeContext } from '../../Analysis';
import { jumpToAnalysisTabNew } from '../../Analysis/constant';
import useAbortXhr from '../../hooks/useAbortXhr';
import { queryPerformanceHistogram } from '../../service';
import {
  ENetowrkType,
  ENetworkDirectionType,
  ENetworkTabs,
  NETWORK_DIRECITON_METRIC_SUFFIX,
  NETWORK_DIRECTION_NAME,
} from '../../typing';
import ChartMarkAreaDetail from '../Dashboard/components/ChartMarkAreaDetail';
import WidgetChart, { EFormatterType } from '../WidgetChart';

interface IPerformanceAnalysisProps {
  dispatch: Dispatch;
  globalSelectedTime: Required<IGlobalTime>;
  realTimeStatisticsFlag: ERealTimeStatisticsFlag;
  queryLoading: boolean | undefined;
}

const PerformanceAnalysis: React.FC<IPerformanceAnalysisProps> = ({
  dispatch,
  globalSelectedTime,
  realTimeStatisticsFlag,
  queryLoading = false,
}) => {
  const realTimePollingRef = useRef<number | undefined>(undefined);
  const { serviceId: serviceIdParam, networkId: networkIdParam } = useParams() as unknown as {
    networkId: string;
    serviceId?: string;
  };
  const { serviceId, networkId } = (() => {
    if (networkIdParam.includes('^')) {
      return {
        serviceId: serviceIdParam,
        networkId: networkIdParam.split('^')[1],
      };
    }
    return { serviceId: serviceIdParam, networkId: networkIdParam };
  })();
  const [performanceHistogramData, setPerformanceHistogramData] = useState<IPerformanceStatData[]>(
    [],
  );

  const [networkDirection, setNetworkDirection] = useState<ENetworkDirectionType>(
    ENetworkDirectionType.TOTAL,
  );

  // FIXME: 这个地方的类型不知道该怎么写， 暂时搞个any
  const [networkType] = useContext<any>(serviceId ? ServiceContext : NetworkTypeContext) as [
    ENetowrkType,
  ];
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
    return globalSelectedTime;
  }, [globalSelectedTime]);

  const queryData = useCallback(() => {
    // 组装DSL条件
    let dsl = '';
    if (networkId && networkType === ENetowrkType.NETWORK) {
      dsl += '(';
      if (networkId && networkType === ENetowrkType.NETWORK) {
        dsl += `network_id="${networkId}"`;
      }
      if (serviceId) {
        dsl += ` AND service_id="${serviceId}"`;
      }
      dsl += ')';
    }

    dsl += ` | gentimes timestamp start="${selectedTimeInfo.startTime}" end="${selectedTimeInfo.endTime}"`;

    const queryDataParams = {
      sourceType,
      serviceId,
      startTime: selectedTimeInfo.startTime,
      endTime: selectedTimeInfo.endTime,
      interval: selectedTimeInfo.interval,
      dsl,
      realTime: realTimeStatisticsFlag,
    };
    queryDataParams[networkType === ENetowrkType.NETWORK ? 'networkId' : 'networkGroupId'] =
      networkId;
    dispatch({
      type: 'npmdModel/queryPerformanceHistogram',
      payload: queryDataParams,
    });
    queryPerformanceHistogram(queryDataParams).then((res) => {
      const { success, result } = res;
      if (success) {
        setPerformanceHistogramData(result);
      }
    });
  }, [
    networkId,
    networkType,
    selectedTimeInfo.startTime,
    selectedTimeInfo.endTime,
    selectedTimeInfo.interval,
    sourceType,
    serviceId,
    realTimeStatisticsFlag,
    dispatch,
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

  // 实时统计时图表不显示loading
  const loading = useMemo(() => {
    if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.OPEN) {
      return false;
    }
    return queryLoading;
  }, [queryLoading, realTimeStatisticsFlag]);

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

    const suffix = NETWORK_DIRECITON_METRIC_SUFFIX[networkDirection];

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
  }, [networkDirection, networkId, performanceHistogramData, serviceId]);
  const [state, performanceDispatch] = useContext(
    serviceId ? ServiceAnalysisContext : AnalysisContext,
  );

  const [markArea, setMarkArea] = useState<any>({});

  const markAreaProps = useMemo(() => {
    if (networkType === ENetowrkType.NETWORK) {
      return { networkId };
    } else {
      return { networkGroupId: networkId };
    }
  }, [networkId, networkType]);

  useAbortXhr({
    cancelUrls: ['/central/packet-oldest-time', '/metric/networks/performance'],
  });

  const handleNetworkTypeChange = (e: RadioChangeEvent) => {
    setNetworkDirection(e.target.value);
  };

  return (
    <>
      <ChartMarkAreaDetail
        {...markAreaProps}
        markAreaDetail={setMarkArea}
        globalSelectedTime={globalSelectedTime}
      />
      <div style={{ textAlign: 'right', marginBottom: 10 }}>
        <Row justify="end">
          <Space style={{ textAlign: 'right', marginBottom: 10 }}>
            <Radio.Group
              options={Object.keys(ENetworkDirectionType).map((key) => {
                return {
                  label: NETWORK_DIRECTION_NAME[ENetworkDirectionType[key]],
                  value: ENetworkDirectionType[key],
                };
              })}
              onChange={handleNetworkTypeChange}
              value={networkDirection}
              optionType="button"
            />
            <Button
              type="primary"
              icon={<SettingOutlined />}
              onClick={() => {
                jumpToAnalysisTabNew(state, performanceDispatch, ENetworkTabs.PERFORMANCESETTING);
              }}
            >
              响应时间配置
            </Button>
          </Space>
        </Row>
      </div>
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
            tableData={computeSeriesData.tableData}
            chartSeries={computeSeriesData.serverLatencyChartData as any}
            formatterType={EFormatterType.ms}
            markArea={markArea}
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
            tableData={computeSeriesData.tableData}
            chartSeries={computeSeriesData.responseSpeedChartData as any}
            formatterType={EFormatterType.count}
            markArea={markArea}
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
            tableData={computeSeriesData.tableData}
            chartSeries={computeSeriesData.latencyChartData}
            formatterType={EFormatterType.ms}
            markArea={markArea}
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
            tableData={computeSeriesData.tableData}
            chartSeries={computeSeriesData.retransPacketChartData as any}
            formatterType={EFormatterType.count}
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
    appModel: { globalSelectedTime, realTimeStatisticsFlag },
  }: ConnectState) => ({
    globalSelectedTime,
    realTimeStatisticsFlag,
    queryLoading: effects['npmdModel/queryPerformanceHistogram'],
  }),
)(PerformanceAnalysis);
