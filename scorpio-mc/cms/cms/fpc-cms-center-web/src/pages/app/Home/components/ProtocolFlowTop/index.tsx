import ReactECharts from '@/components/ReactECharts';
import EChartsMessage from '@/components/Message';
import type { ConnectState } from '@/models/connect';
import type { ICountL7Procolcol } from '@/pages/app/Home/typings';
import * as dateMath from '@/utils/frame/datemath';
import { bytesToSize, timeFormatter } from '@/utils/utils';
import { Card, Radio } from 'antd';
import { connect } from 'dva';
import type { EChartsOption } from 'echarts';
import moment from 'moment';
import React from 'react';
import type { Dispatch } from 'redux';
import {
  cardProps,
  chartHeight,
  chartLoading,
  computedPercent,
  dateStringParse,
  defaultSelectedTime,
  timeRange,
} from '../../utils';

const SIZE_LIMIT = 20;
const chartTitle = `协议流量Top${SIZE_LIMIT}`;

interface ProtocolFlowTopProps {
  dispatch: Dispatch<any>;
  l7ProtocolFlowCount: ICountL7Procolcol[];
  allL7ProtocolMap: ConnectState['metadataModel']['allL7ProtocolMap'];
}

interface ProtocolFlowTopState {
  selectedTime?: string;
  from: string;
  to: string;
  queryLoading: boolean;
}

class ProtocolFlowTop extends React.PureComponent<ProtocolFlowTopProps, ProtocolFlowTopState> {
  constructor(props: ProtocolFlowTopProps) {
    super(props);

    this.state = {
      selectedTime: defaultSelectedTime,
      from: dateStringParse(defaultSelectedTime),
      to: moment().format(),
      queryLoading: false,
    };
  }

  componentDidMount() {
    this.queryData();
  }

  queryData = () => {
    this.setState({
      queryLoading: true,
    });
    const { dispatch } = this.props;
    const { from, to } = this.state;
    const timeInfo = timeFormatter(from, to);
    if (!timeInfo) {
      return;
    }
    const { startTime, endTime, interval } = timeInfo;
    (
      dispatch({
        type: 'homeModel/queryL7ProtocolFlowCount',
        payload: {
          startTime,
          endTime,
          interval,
          count: SIZE_LIMIT,
          sortProperty: 'total_bytes',
          sortDirection: 'desc',
        },
      }) as unknown as Promise<any>
    ).then(() => {
      this.setState({
        queryLoading: false,
      });
    });
  };

  handleTimeChange = (newSelectedTime: string) => {
    this.setState(
      {
        selectedTime: newSelectedTime,
        from: moment(dateMath.parse(newSelectedTime)).format(),
        to: moment().format(),
      },
      () => {
        this.queryData();
      },
    );
  };

  handleResult = () => {
    const { l7ProtocolFlowCount = [], allL7ProtocolMap } = this.props;
    const seriesData: number[] = [];
    const categories: string[] = [];

    // 排序
    const resultCopy = l7ProtocolFlowCount.slice();
    // 根据总流量排序
    resultCopy.sort((a, b) => b.totalBytes - a.totalBytes);
    let allBytes = 0;
    // 取前20个
    resultCopy.forEach((row, index) => {
      const { l7ProtocolId, totalBytes } = row;
      if (index < SIZE_LIMIT) {
        const protocolName = allL7ProtocolMap[l7ProtocolId]?.nameText || l7ProtocolId;
        categories.push(protocolName);
        seriesData.push(totalBytes);
      }
      allBytes += totalBytes;
    });

    return { categories, seriesData, allBytes };
  };

  render() {
    const { selectedTime, queryLoading } = this.state;
    const { categories, seriesData, allBytes } = this.handleResult();

    const option: EChartsOption = {
      xAxis: {
        type: 'category',
        data: categories,
        axisTick: {
          show: false,
          interval: 1,
        },
        axisLabel: {
          interval: 0,
          rotate: categories.length > 10 ? 45 : 0,
        },
      },
      yAxis: [
        {
          type: 'value',
          axisLabel: {
            formatter: (value: any) => {
              return bytesToSize(value);
            },
          },
        },
      ],
      dataZoom: [
        {
          type: 'inside',
          minValueSpan: 5,
        },
      ],
      grid: {
        bottom: 10,
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
        },
        formatter: (params: any) => {
          const item = params[0];
          let label = `<div>${item.name}</div>`;
          label += item.marker;
          label += '流量: ';
          label += `${bytesToSize(item.value)}`;
          // 计算占比
          label += `（${computedPercent(item.value, allBytes)}）`;
          label += '</b><br/>';
          return label;
        },
      },
      legend: {
        show: false,
      },
      series: [
        {
          name: '流量',
          type: 'bar',
          barWidth: '20px',
          data: seriesData,
        },
      ],
    };
    const renderContent = () => {
      if (queryLoading) {
        return chartLoading;
      }
      if (seriesData.length === 0) {
        return <EChartsMessage height={chartHeight} />;
      }
      // @ts-ignore
      return <ReactECharts option={option} opts={{ height: chartHeight }} />;
    };
    return (
      <Card
        {...cardProps}
        size="small"
        title={chartTitle}
        extra={
          <Radio.Group
            value={selectedTime}
            buttonStyle="solid"
            size="small"
            disabled={queryLoading}
            onChange={(e) => this.handleTimeChange(e.target.value)}
          >
            {timeRange.map((time) => (
              <Radio.Button key={time.key} value={time.key}>
                {time.label}
              </Radio.Button>
            ))}
          </Radio.Group>
        }
      >
        {renderContent()}
      </Card>
    );
  }
}

export default connect(
  ({ homeModel: { l7ProtocolFlowCount }, metadataModel: { allL7ProtocolMap } }: ConnectState) => ({
    l7ProtocolFlowCount,
    allL7ProtocolMap,
  }),
)(ProtocolFlowTop);
