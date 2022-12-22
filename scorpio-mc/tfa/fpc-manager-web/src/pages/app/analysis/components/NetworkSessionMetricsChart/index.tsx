import { EMetricApiType } from '@/common/api/analysis';
import TimeAxisChart from '@/components/TimeAxisChart';
import type { TimeAxisChartData } from '@/components/TimeAxisChart/typing';
import type { ConnectState } from '@/models/connect';
import type { INetworkAnalysis } from '@/pages/app/Home/typings';
import { chartHeight, chartLoading } from '@/pages/app/Home/utils';
import { useSafeState } from 'ahooks';
import { Card } from 'antd';
import numeral from 'numeral';
import { useEffect, useMemo } from 'react';
import type { Dispatch, IGlobalSelectedTime } from 'umi';
import { connect } from 'umi';

const cardProps = {
  bordered: true,
  size: 'small',
  style: { marginBottom: 10 },
  bodyStyle: { height: chartHeight, padding: 6 },
};

interface Props {
  dispatch: Dispatch<any>;
  networkHistogram: INetworkAnalysis[];
  globalSelectedTime: Required<IGlobalSelectedTime>;
  bordered?: boolean;
  markArea?: any;
}

const nameMap = {
  activeSessions: '活动会话数',
  concurrentSessions: '最大并发会话数',
  establishedSessions: '新建会话数',
  destroyedSessions: '销毁会话数',
};

function NetworkSessionMetricsChart(props: Props) {
  const { dispatch, networkHistogram, globalSelectedTime, bordered, markArea } = props;

  const { startTime, endTime, interval = 60, startTimestamp, endTimestamp } = globalSelectedTime;

  const chartData = useMemo<TimeAxisChartData[]>(() => {
    if (networkHistogram) {
      return networkHistogram.map((item) => {
        return {
          timestamp: item.timestamp,
          activeSessions: item.activeSessions,
          concurrentSessions: item.concurrentSessions,
          establishedSessions: item.establishedSessions,
          destroyedSessions: item.destroyedSessions,
        };
      });
    }
    return [];
  }, [networkHistogram]);

  const [queryLoading, setQueryLoading] = useSafeState(true);

  useEffect(() => {
    setQueryLoading(true);
    dispatch({
      type: 'homeModel/queryMetricAnalysysHistogram',
      payload: {
        metricApi: EMetricApiType.network,
        startTime,
        endTime,
        interval,
        dsl: ` | gentimes timestamp start="${startTime}" end="${endTime}"`,
      },
    }).then(() => {
      setQueryLoading(false);
    });
  }, [dispatch, endTime, interval, setQueryLoading, startTime]);

  return (
    <Card {...(bordered ? { ...cardProps, bordered } : cardProps)} size="small" title="会话统计">
      {queryLoading ? (
        chartLoading
      ) : (
        <TimeAxisChart
          data={chartData}
          brush={true}
          showLegend={true}
          startTime={startTimestamp}
          endTime={endTimestamp}
          interval={interval}
          nameMap={nameMap}
          unitConverter={(value) => {
            return numeral(value).format('0, 0');
          }}
          markArea={markArea}
        />
      )}
    </Card>
  );
}
export default connect(
  ({ homeModel: { analysisHistogramMap }, appModel: { globalSelectedTime } }: ConnectState) => {
    return {
      networkHistogram: analysisHistogramMap[EMetricApiType.network],
      globalSelectedTime,
    };
  },
)(NetworkSessionMetricsChart);
