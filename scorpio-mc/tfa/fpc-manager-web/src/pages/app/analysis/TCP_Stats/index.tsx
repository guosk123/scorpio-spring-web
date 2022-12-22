import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ERealTimeStatisticsFlag } from '@/models/app';
import type { ConnectState } from '@/models/connect';
import type { RadioChangeEvent } from 'antd';
import { Col, Radio, Row, Space } from 'antd';
import { connect } from 'dva';
import type { BarSeriesOption, LineSeriesOption } from 'echarts/charts';
import moment from 'moment';
import numeral from 'numeral';
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useParams } from 'react-router';
import type { Dispatch } from 'umi';
import { useLocation } from 'umi';
import CacheStateBox, { stopLoad } from '../components/CacheStateBox';
import WidgetChart, { EFormatterType } from '../components/WidgetChart';
import { stopRefreshPageFlag } from '../Network/Dashboard';
import type { IMetricQueryParams, ITcpStatData, IUriParams } from '../typings';
import { ESourceType, REAL_TIME_POLLING_MS } from '../typings';

interface ITCPAnalysisProps {
  dispatch: Dispatch;
  globalSelectedTime: Required<IGlobalTime>;
  realTimeStatisticsFlag: ERealTimeStatisticsFlag;
  tcpHistogramData: ITcpStatData[];
  queryLoading?: boolean;
  beforeOldestPacketArea?: any;
}

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

const TCPAnalysis: React.FC<ITCPAnalysisProps> = ({
  globalSelectedTime,
  realTimeStatisticsFlag,
  dispatch,
  tcpHistogramData,
  queryLoading,
  beforeOldestPacketArea,
}) => {
  const realTimePollingRef = useRef<number | undefined>(undefined);
  const { networkId, serviceId, pcapFileId }: IUriParams = useParams();

  const [networkType, setNetworkType] = useState<NetworkType>(NetworkType.TOTAL);

  const selectedTimeInfo = useMemo(() => {
    // if (pcapFileId && currentPcpInfo) {
    //   return timeFormatter(currentPcpInfo?.filterStartTime, currentPcpInfo?.filterEndTime);
    // }
    return globalSelectedTime;
  }, [globalSelectedTime]);

  const sourceType: ESourceType = useMemo(() => {
    if (serviceId) {
      return ESourceType.SERVICE;
    }
    if (networkId) {
      return ESourceType.NETWORK;
    }
    return ESourceType.OFFLINE;
  }, [serviceId, networkId]);

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
      type: 'npmdModel/queryTcpHistogram',
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
    pcapFileId,
    selectedTimeInfo.startTime,
    selectedTimeInfo.endTime,
    selectedTimeInfo.interval,
    dispatch,
    sourceType,
    networkId,
    serviceId,
    realTimeStatisticsFlag,
  ]);

  useEffect(() => {
    queryData();
  }, [queryData]);

  const isRealTimeStatistics = useMemo(
    () => realTimeStatisticsFlag === ERealTimeStatisticsFlag.OPEN,
    [realTimeStatisticsFlag],
  );

  // 实时统计
  useEffect(() => {
    if (!isRealTimeStatistics) {
      window.clearInterval(realTimePollingRef.current);
    } else {
      realTimePollingRef.current = window.setInterval(() => {
        queryData();
      }, REAL_TIME_POLLING_MS);
    }

    return () => {
      window.clearInterval(realTimePollingRef.current);
    };
  }, [isRealTimeStatistics, queryData]);

  const [cacheModelFlag, setcacheModelFlag] = useState(false);

  // 实时统计时图表不显示loading
  const loading = useMemo(() => {
    if (isRealTimeStatistics) {
      return false;
    }
    return stopLoad(cacheModelFlag, queryLoading || false);
  }, [isRealTimeStatistics, cacheModelFlag, queryLoading]);

  // 计算数据
  const computeSeriesData = useMemo(() => {
    const tableData = [];
    // 带宽曲线图数据
    const establishedChartData: BarSeriesOption[] = [
      {
        name: '建连成功',
        type: 'bar',
        barGap: 0,
        color: '#41D9C7',
        data: [],
      },
      {
        name: '建连失败',
        type: 'bar',
        barGap: 0,
        color: '#F04864',
        data: [],
      },
    ];
    // SYN包数量
    const synPacketChartData: BarSeriesOption[] = [
      {
        name: '客户端SYN包',
        type: 'bar',
        barGap: 0,
        data: [],
      },
      {
        name: '服务器SYNACK包',
        type: 'bar',
        barGap: 0,
        data: [],
      },
    ];

    const retransRateChartData: LineSeriesOption[] = [
      {
        name: '客户端重传率',
        type: 'line',
        data: [],
      },
      {
        name: '服务器重传率',
        type: 'line',
        data: [],
      },
    ];

    const retransPacketChartData: LineSeriesOption[] = [
      {
        name: '客户端重传包数',
        type: 'line',
        data: [],
      },
      {
        name: '服务器重传包数',
        type: 'line',
        data: [],
      },
    ];

    const zeroWindowChartData: BarSeriesOption[] = [
      {
        name: '客户端零窗口数量',
        type: 'bar',
        barGap: 0,
        data: [],
      },
      {
        name: '服务器零窗口数量',
        type: 'bar',
        barGap: 0,
        data: [],
      },
    ];

    const suffix = NETWORK_METRIC_SUFFIX[networkType];

    for (let index = 0; index < tcpHistogramData.length; index += 1) {
      const row = tcpHistogramData[index];

      const tcpEstablishedSuccessCounts = row[`tcpEstablishedSuccessCounts${suffix}`] || 0;
      const tcpEstablishedFailCounts = row[`tcpEstablishedFailCounts${suffix}`] || 0;

      const tcpServerSynPackets = row[`tcpServerSynPackets${suffix}`] || 0;
      const tcpClientSynPackets = row[`tcpClientSynPackets${suffix}`] || 0;

      const tcpClientRetransmissionRate = row[`tcpClientRetransmissionRate${suffix}`] || 0;
      const tcpClientRetransmissionPackets = row[`tcpClientRetransmissionPackets${suffix}`] || 0;

      const tcpServerRetransmissionRate = row[`tcpServerRetransmissionRate${suffix}`] || 0;
      const tcpServerRetransmissionPackets = row[`tcpServerRetransmissionPackets${suffix}`] || 0;

      const tcpClientZeroWindowPackets = row[`tcpClientZeroWindowPackets${suffix}`] || 0;
      const tcpServerZeroWindowPackets = row[`tcpServerZeroWindowPackets${suffix}`] || 0;

      const { timestamp } = row;
      const timeText = moment(timestamp).format('YYYY-MM-DD HH:mm:ss');
      const timestampNum = new Date(timestamp).valueOf();
      tableData.push({
        timestamp: timeText,
        tcpEstablishedFailCounts: numeral(tcpEstablishedFailCounts).format('0,0'),
        tcpEstablishedSuccessCounts: numeral(tcpEstablishedSuccessCounts).format('0,0'),
        tcpServerSynPackets: numeral(tcpServerSynPackets).format('0,0'),
        tcpClientSynPackets: numeral(tcpClientSynPackets).format('0,0'),
        tcpClientRetransmissionRate: `${(tcpClientRetransmissionRate * 100).toFixed(2)}%`,
        tcpClientRetransmissionPackets: numeral(tcpClientRetransmissionPackets).format('0,0'),
        tcpServerRetransmissionRate: `${(tcpServerRetransmissionRate * 100).toFixed(2)}%`,
        tcpServerRetransmissionPackets: numeral(tcpServerRetransmissionPackets).format('0,0'),
        tcpClientZeroWindowPackets: numeral(tcpClientZeroWindowPackets).format('0,0'),
        tcpServerZeroWindowPackets: numeral(tcpServerZeroWindowPackets).format('0,0'),
      });

      // TCP建连次数
      establishedChartData[0].data!.push([timestampNum, tcpEstablishedSuccessCounts]);
      establishedChartData[1].data!.push([timestampNum, tcpEstablishedFailCounts]);
      // SYN包数量
      synPacketChartData[0].data!.push([timestampNum, tcpClientSynPackets]);
      synPacketChartData[1].data!.push([timestampNum, tcpServerSynPackets]);
      // 重传率
      retransRateChartData[0].data!.push([
        timestampNum,
        (tcpClientRetransmissionRate * 100).toFixed(2),
      ]);
      retransRateChartData[1].data!.push([
        timestampNum,
        (tcpServerRetransmissionRate * 100).toFixed(2),
      ]);
      // 重传包数
      retransPacketChartData[0].data?.push([timestampNum, tcpClientRetransmissionPackets]);
      retransPacketChartData[1].data?.push([timestampNum, tcpServerRetransmissionPackets]);
      // 零窗口数量
      zeroWindowChartData[0].data!.push([timestampNum, tcpClientZeroWindowPackets]);
      zeroWindowChartData[1].data!.push([timestampNum, tcpServerZeroWindowPackets]);
    }

    return {
      tableData,
      establishedChartData,
      synPacketChartData,
      retransRateChartData,
      retransPacketChartData,
      zeroWindowChartData,
    };
  }, [networkType, tcpHistogramData]);

  const cacheState = useMemo(() => {
    return { computeSeriesData };
  }, [computeSeriesData]);

  const [tcpStats, setTcpStats] = useState(cacheState);
  const { pathname } = useLocation();
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
          </Space>
        </Row>
      )}
      <CacheStateBox
        cacheState={cacheState}
        onNewState={setTcpStats}
        onCacheModelFlag={setcacheModelFlag}
      />
      <Row gutter={10}>
        {/* 'TCP建连次数柱状图', 'TCP SYN量柱状图', 'TCP重传量曲线图', 'TCP零窗口曲线图' */}
        <Col span={12} style={{ marginBottom: 10 }} key="TCP建连次数柱状图">
          <WidgetChart
            title="TCP建连次数柱状图"
            selectedTime={selectedTimeInfo}
            tableColumns={[
              {
                title: '时间',
                dataIndex: 'timestamp',
              },
              {
                title: '建连成功',
                dataIndex: 'tcpEstablishedSuccessCounts',
              },
              {
                title: '建连失败',
                dataIndex: 'tcpEstablishedFailCounts',
              },
            ]}
            loading={loading}
            tableData={tcpStats.computeSeriesData.tableData}
            chartSeries={tcpStats.computeSeriesData.establishedChartData}
            formatterType={EFormatterType.count}
            markArea={beforeOldestPacketArea}
            disableChangeTime={cacheModelFlag}
            isRefreshPage={isRefreshPage}
          />
        </Col>
        <Col span={12} style={{ marginBottom: 10 }} key="TCP SYN量柱状图">
          <WidgetChart
            title="TCP SYN包数量柱状图"
            selectedTime={selectedTimeInfo}
            tableColumns={[
              {
                title: '时间',
                dataIndex: 'timestamp',
              },
              {
                title: '客户端SYN包',
                dataIndex: 'tcpClientSynPackets',
              },
              {
                title: '服务器SYNACK包',
                dataIndex: 'tcpServerSynPackets',
              },
            ]}
            loading={loading}
            tableData={tcpStats.computeSeriesData.tableData}
            chartSeries={tcpStats.computeSeriesData.synPacketChartData}
            formatterType={EFormatterType.count}
            markArea={beforeOldestPacketArea}
            disableChangeTime={cacheModelFlag}
            isRefreshPage={isRefreshPage}
          />
        </Col>
        {/* 实时统计显示重传包数，非实时统计显示重传率 */}
        {isRealTimeStatistics ? (
          <Col span={12} style={{ marginBottom: 10 }} key="TCP重传包数曲线图">
            <WidgetChart
              title="TCP重传包数曲线图"
              selectedTime={selectedTimeInfo}
              tableColumns={[
                {
                  title: '时间',
                  dataIndex: 'timestamp',
                },

                {
                  title: '客户端重传包数',
                  dataIndex: 'tcpClientRetransmissionPackets',
                },
                {
                  title: '服务器重传包数',
                  dataIndex: 'tcpServerRetransmissionPackets',
                },
              ]}
              loading={loading}
              tableData={tcpStats.computeSeriesData.tableData}
              chartSeries={tcpStats.computeSeriesData.retransPacketChartData}
              formatterType={EFormatterType.count}
              markArea={beforeOldestPacketArea}
              disableChangeTime={cacheModelFlag}
              isRefreshPage={isRefreshPage}
            />
          </Col>
        ) : (
          <Col span={12} style={{ marginBottom: 10 }} key="TCP重传率曲线图">
            <WidgetChart
              title="TCP重传率曲线图"
              selectedTime={selectedTimeInfo}
              tableColumns={[
                {
                  title: '时间',
                  dataIndex: 'timestamp',
                },
                {
                  title: '客户端重传率',
                  dataIndex: 'tcpClientRetransmissionRate',
                },
                {
                  title: '服务器重传率',
                  dataIndex: 'tcpServerRetransmissionRate',
                },
              ]}
              loading={loading}
              tableData={tcpStats.computeSeriesData.tableData}
              chartSeries={tcpStats.computeSeriesData.retransRateChartData}
              formatterType={EFormatterType.percentage}
              markArea={beforeOldestPacketArea}
              disableChangeTime={cacheModelFlag}
              isRefreshPage={isRefreshPage}
            />
          </Col>
        )}
        <Col span={12} style={{ marginBottom: 10 }} key="TCP零窗口曲线图">
          <WidgetChart
            title="TCP零窗口曲线图"
            selectedTime={selectedTimeInfo}
            tableColumns={[
              {
                title: '时间',
                dataIndex: 'timestamp',
              },
              {
                title: '客户端零窗口',
                dataIndex: 'tcpClientZeroWindowPackets',
              },
              {
                title: '服务器零窗口',
                dataIndex: 'tcpServerZeroWindowPackets',
              },
            ]}
            loading={loading}
            tableData={tcpStats.computeSeriesData.tableData}
            chartSeries={tcpStats.computeSeriesData.zeroWindowChartData}
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
    appModel: { globalSelectedTime, realTimeStatisticsFlag },
    npmdModel: { tcpHistogramData, beforeOldestPacketArea },
  }: ConnectState) => ({
    globalSelectedTime,
    realTimeStatisticsFlag,
    tcpHistogramData,
    queryLoading: effects['npmdModel/queryTcpHistogram'],
    beforeOldestPacketArea,
  }),
)(TCPAnalysis);
