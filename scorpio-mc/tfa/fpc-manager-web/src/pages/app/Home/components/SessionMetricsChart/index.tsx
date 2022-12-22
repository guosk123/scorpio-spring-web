import type { ConnectState } from '@/models/connect';
import * as dateMath from '@/utils/frame/datemath';
import { timeFormatter } from '@/utils/utils';
import { Card, Radio } from 'antd';
import { connect } from 'umi';
import _ from 'lodash';
import moment from 'moment';
import React from 'react';
import type { Dispatch } from 'redux';
import type { INetworkAnalysis } from '@/pages/app/Home/typings';
import type { ISeries } from '@/components/TimeHistogramChart';
import TimeHistogramChart, { EChartType } from '@/components/TimeHistogramChart';
import {
  cardProps,
  chartHeight,
  chartLoading,
  dateStringParse,
  defaultSelectedTime,
  timeRange,
} from '../../utils';
import { EMetricApiType } from '@/common/api/analysis';

interface SessionMetricsChartProps {
  dispatch: Dispatch<any>;
  networkHistogram: INetworkAnalysis[];
  bordered?: boolean;
}

interface SessionMetricsChartState {
  selectedTime?: string;
  from: string;
  to: string;
  queryLoading: boolean;
}

class SessionMetricsChart extends React.PureComponent<
  SessionMetricsChartProps,
  SessionMetricsChartState
> {
  constructor(props: SessionMetricsChartProps) {
    super(props);
    this.state = {
      selectedTime: defaultSelectedTime,
      from: dateStringParse(defaultSelectedTime),
      to: moment().format(),
      queryLoading: false,
    };
  }

  componentDidMount() {
    this.queryHistogram();
  }

  handleTimeChange = (newSelectedTime: string) => {
    this.setState(
      {
        selectedTime: newSelectedTime,
        from: moment(dateMath.parse(newSelectedTime)).format(),
        to: moment().format(),
      },
      () => {
        this.queryHistogram();
      },
    );
  };

  handleChartSelection = (from: any, to: any) => {
    this.setState(
      {
        from,
        to,
      },
      () => {
        this.queryHistogram();
      },
    );
  };

  handleResult = () => {
    const { networkHistogram = [] } = this.props;
    const { from, to } = this.state;
    const { startTime, endTime, interval = 30 } = timeFormatter(from, to) || {};

    const activeSessionsPsArr: any[] = [];
    const concurrentSessionsArr: any[] = [];
    const establishedSessionsPsArr: any[] = [];
    const destroyedSessionsPsArr: any[] = [];

    const groupByTimeMap = _.groupBy(networkHistogram, (item) =>
      new Date(item.timestamp).getTime(),
    );

    // 补点
    const startTimestamp = new Date(startTime!).getTime();
    const endTimestamp = new Date(endTime!).getTime();
    const diffCount = (endTimestamp - startTimestamp) / 1000 / interval!;
    // 时间补点
    for (let index = 0; index < diffCount; index += 1) {
      const time = startTimestamp + index * interval * 1000;
      const pointList = groupByTimeMap[time];
      let totalActiveSessions = 0;
      let totalConcurrentSessions = 0;
      let totalEstablishedSessions = 0;
      let totalDestroyedSessions = 0;
      if (Array.isArray(pointList)) {
        for (let j = 0; j < pointList.length; j += 1) {
          // 把所有的网络的会话数加起来
          totalActiveSessions += pointList[j].activeSessions || 0;
          totalConcurrentSessions += pointList[j].concurrentSessions || 0;
          totalEstablishedSessions += pointList[j].establishedSessions || 0;
          totalDestroyedSessions += pointList[j].destroyedSessions || 0;
        }
      }
      const point = {
        startTime: time,
        endTime: time + interval * 1000,
        smooth: false,
      };

      activeSessionsPsArr.push({
        ...point,
        value: [time, totalActiveSessions],
      });
      concurrentSessionsArr.push({
        ...point,
        value: [time, totalConcurrentSessions],
      });
      establishedSessionsPsArr.push({
        ...point,
        value: [time, totalEstablishedSessions],
      });
      destroyedSessionsPsArr.push({
        ...point,
        value: [time, totalDestroyedSessions],
      });
    }

    // 会话统计
    const sessionData: ISeries[] = [
      {
        type: EChartType.LINE,
        name: '活动会话数',
        symbol: 'none',
        data: activeSessionsPsArr,
      },
      {
        type: EChartType.LINE,
        name: '最大并发会话数',
        symbol: 'none',
        data: concurrentSessionsArr,
      },
      {
        type: EChartType.LINE,
        name: '新建会话数',
        symbol: 'none',
        data: establishedSessionsPsArr,
      },
      {
        type: EChartType.LINE,
        name: '销毁会话数',
        symbol: 'none',
        data: destroyedSessionsPsArr,
      },
    ];

    return sessionData;
  };

  queryHistogram = () => {
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
        type: 'homeModel/queryMetricAnalysysHistogram',
        payload: {
          metricApi: EMetricApiType.network,
          startTime,
          endTime,
          interval,
          dsl: ` | gentimes timestamp start="${startTime}" end="${endTime}"`,
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

    const { bordered } = this.props;
    return (
      <Card
        {...(bordered ? { ...cardProps, bordered } : cardProps)}
        size="small"
        title="会话统计"
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
          <TimeHistogramChart
            height={chartHeight}
            seriesData={seriesData}
            loading={queryLoading}
            changeGlobalTime={false}
            onTimeSelection={this.handleChartSelection}
          />
        )}
      </Card>
    );
  }
}

export default connect(({ homeModel: { analysisHistogramMap } }: ConnectState) => ({
  networkHistogram: analysisHistogramMap[EMetricApiType.network],
}))(SessionMetricsChart);
