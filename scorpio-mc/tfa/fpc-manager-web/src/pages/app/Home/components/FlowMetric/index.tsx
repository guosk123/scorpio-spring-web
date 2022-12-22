import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { timeAxis } from '@/components/ReactECharts';
import type { ConnectState } from '@/models/connect';
import * as dateMath from '@/utils/frame/datemath';
import { convertBandwidth, timeFormatter } from '@/utils/utils';
import { Card, Radio } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import React from 'react';
import type { Dispatch } from 'redux';
import type { INetworkAnalysis } from '../../typings';
import {
  cardProps,
  chartHeight,
  chartLoading,
  dateStringParse,
  defaultSelectedTime,
  timeRange,
} from '../../utils';

interface FlowMetricProps {
  dispatch: Dispatch<any>;
  allNetworkFlowHistogram: INetworkAnalysis[];
  bordered?: boolean;
}

interface FlowMetricState {
  selectedTime?: string;
  from: string;
  to: string;
  queryLoading: boolean | undefined;
}

class FlowMetric extends React.PureComponent<FlowMetricProps, FlowMetricState> {
  constructor(props: FlowMetricProps) {
    super(props);
    this.state = {
      selectedTime: defaultSelectedTime,
      from: dateStringParse(defaultSelectedTime),
      to: moment().format(),
      queryLoading: false,
    };
  }

  componentDidMount() {
    this.queryNetifHistogram();
  }

  handleTimeChange = (newSelectedTime: string) => {
    this.setState(
      {
        selectedTime: newSelectedTime,
        from: moment(dateMath.parse(newSelectedTime)).format(),
        to: moment().format(),
      },
      () => {
        this.queryNetifHistogram();
      },
    );
  };

  handleBrushEnd = (startTime: number, endTime: number) => {
    const from = moment(startTime).format();
    const to = moment(endTime).format();
    this.setState(
      {
        from,
        to,
      },
      () => {
        this.queryNetifHistogram();
      },
    );
  };

  handleResult = () => {
    const { allNetworkFlowHistogram = [] } = this.props;
    const { from, to } = this.state;
    const timeInfo = timeFormatter(from, to);
    if (!timeInfo) {
      return [];
    }
    const { startTime, endTime, interval } = timeInfo;
    const flowData = [];
    // 补点
    const startTimestamp = new Date(startTime).getTime();
    const endTimestamp = new Date(endTime).getTime();
    const diffCount = (endTimestamp - startTimestamp) / 1000 / interval!;
    // 时间补点
    for (let index = 0; index < diffCount; index += 1) {
      const time = startTimestamp + index * interval * 1000;
      let point = null;
      for (let j = 0; j < allNetworkFlowHistogram.length; j += 1) {
        const row = allNetworkFlowHistogram[j];
        const pointTime = new Date(row.timestamp).getTime();
        if (pointTime === time) {
          point = row;
          break;
        }
      }

      flowData.push([time, point ? (point.totalBytes * 8) / interval : null]);
    }

    return [
      {
        type: 'line',
        connectNulls: false,
        symbol: 'none',
        name: '总带宽',
        data: flowData,
      },
    ];
  };

  queryNetifHistogram = () => {
    const { dispatch } = this.props;
    const { from, to } = this.state;
    const timeInfo = timeFormatter(from, to);
    if (!timeInfo) {
      return;
    }
    const { startTime, endTime, interval } = timeInfo;

    this.setState(() => ({
      queryLoading: true,
    }));
    (
      dispatch({
        type: 'homeModel/queryAllNetworkFlowHistogram',
        payload: {
          startTime,
          endTime,
          interval,
          dsl: `| gentimes timestamp start="${startTime}" end="${endTime}"`,
        },
      } as unknown) as Promise<any>
    ).then(() => {
      this.setState(() => ({
        queryLoading: false,
      }));
    });
  };

  render() {
    const { selectedTime, queryLoading } = this.state;
    const seriesData = this.handleResult();

    const option: ECOption = {
      xAxis: {
        ...timeAxis,
      },
      yAxis: {
        min: 0,
        axisLabel: {
          formatter(value: number) {
            return convertBandwidth(value);
          },
        },
      },
      tooltip: {
        formatter(params: any) {
          let label = `${params.lastItem.axisValueLabel}<br/>`;
          if (!Array.isArray(params)) {
            return '';
          }

          params.forEach((item) => {
            label += item.marker;
            label += `${item.seriesName}：`;
            label += convertBandwidth(item.data[1]);
            label += '<br/>';
          });
          return label;
        },
      },
      series: seriesData as any,
    };

    const { bordered } = this.props;
    return (
      <Card
        {...(bordered ? { ...cardProps, bordered } : cardProps)}
        size="small"
        title="流量统计"
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
        {queryLoading ? (
          chartLoading
        ) : (
          <ReactECharts
            option={option}
            needPrettify={false}
            opts={{ height: chartHeight }}
            onBrushEnd={this.handleBrushEnd}
          />
        )}
      </Card>
    );
  }
}

export default connect(({ homeModel: { allNetworkFlowHistogram } }: ConnectState) => ({
  allNetworkFlowHistogram,
}))(FlowMetric);
