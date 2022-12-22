import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { ServiceContext } from '@/pages/app/analysis/Service/index';
import type { ITcpStatData } from '@/pages/app/analysis/typings';
import { ESourceType } from '@/pages/app/analysis/typings';
import type { RadioChangeEvent } from 'antd';
import { Col, Radio, Row, Space } from 'antd';
import { connect } from 'dva';
import type { BarSeriesOption, LineSeriesOption } from 'echarts/charts';
import moment from 'moment';
import numeral from 'numeral';
import React, { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router';
import { NetworkTypeContext } from '../../Analysis';
import useAbortXhr from '../../hooks/useAbortXhr';
import { queryTcpHistogram } from '../../service';
import {
  ENetowrkType,
  ENetworkDirectionType,
  NETWORK_DIRECITON_METRIC_SUFFIX,
  NETWORK_DIRECTION_NAME,
} from '../../typing';
import ChartMarkAreaDetail from '../Dashboard/components/ChartMarkAreaDetail';
import WidgetChart, { EFormatterType } from '../WidgetChart';

interface ITCPAnalysisProps {
  globalSelectedTime: Required<IGlobalTime>;
}

const TCPAnalysis: React.FC<ITCPAnalysisProps> = ({ globalSelectedTime }) => {
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
  const [tcpHistogramData, setTcpHistogramData] = useState<ITcpStatData[]>([]);
  const [queryLoading, setQueryLoading] = useState(false);
  const [networkDirection, setNetworkDirection] = useState<ENetworkDirectionType>(
    ENetworkDirectionType.TOTAL,
  );

  // FIXME: 这个地方的类型不知道该怎么写， 暂时搞个any
  const [networkType] = useContext<any>(serviceId ? ServiceContext : NetworkTypeContext) as [
    ENetowrkType,
  ];

  const selectedTimeInfo = useMemo(() => {
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
    setQueryLoading(true);
    const queryDataParams = {
      sourceType,
      startTime: selectedTimeInfo.startTime,
      endTime: selectedTimeInfo.endTime,
      interval: selectedTimeInfo.interval,
      dsl,
      serviceId,
    };
    queryDataParams[networkType === ENetowrkType.NETWORK ? 'networkId' : 'networkGroupId'] =
      networkId;
    queryTcpHistogram(queryDataParams).then((res) => {
      const { success, result } = res;
      if (success) {
        setTcpHistogramData(result);
      }
      setQueryLoading(false);
    });
  }, [
    selectedTimeInfo.startTime,
    selectedTimeInfo.endTime,
    selectedTimeInfo.interval,
    sourceType,
    networkId,
    serviceId,
    networkType,
  ]);

  useEffect(() => {
    queryData();
  }, [queryData]);

  // 实时统计时图表不显示loading
  const loading = useMemo(() => {
    return queryLoading;
  }, [queryLoading]);

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

    const suffix = NETWORK_DIRECITON_METRIC_SUFFIX[networkDirection];
    console.log(suffix);
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
  }, [networkDirection, tcpHistogramData]);

  const [markArea, setMarkArea] = useState<any>({});

  const markAreaProps = useMemo(() => {
    if (networkType === ENetowrkType.NETWORK) {
      return { networkId };
    } else {
      return { networkGroupId: networkId };
    }
  }, [networkId, networkType]);

  useAbortXhr({
    cancelUrls: ['/central/packet-oldest-time', '/metric/networks/tcp'],
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
        </Space>
      </Row>
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
            tableData={computeSeriesData.tableData}
            chartSeries={computeSeriesData.establishedChartData}
            formatterType={EFormatterType.count}
            markArea={markArea}
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
            tableData={computeSeriesData.tableData}
            chartSeries={computeSeriesData.synPacketChartData}
            formatterType={EFormatterType.count}
            markArea={markArea}
          />
        </Col>
        {/* 实时统计显示重传包数，非实时统计显示重传率 */}
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
            tableData={computeSeriesData.tableData}
            chartSeries={computeSeriesData.retransRateChartData}
            formatterType={EFormatterType.percentage}
            markArea={markArea}
          />
        </Col>
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
            tableData={computeSeriesData.tableData}
            chartSeries={computeSeriesData.zeroWindowChartData}
            formatterType={EFormatterType.count}
            markArea={markArea}
          />
        </Col>
      </Row>
    </>
  );
};

export default connect(({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
}))(TCPAnalysis);
