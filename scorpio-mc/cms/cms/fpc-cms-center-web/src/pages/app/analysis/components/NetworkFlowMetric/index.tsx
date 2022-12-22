import TimeAxisChart from '@/components/TimeAxisChart';
import type { TimeAxisChartData } from '@/components/TimeAxisChart/typing';
import type { ConnectState } from '@/models/connect';
import type { INetworkAnalysis } from '@/pages/app/Home/typings';
import { chartHeight, chartLoading } from '@/pages/app/Home/utils';
import { convertBandwidth } from '@/utils/utils';
import { Card } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import type { Dispatch, IGlobalSelectedTime } from 'umi';
import { connect } from 'umi';

interface Props {
  dispatch: Dispatch<any>;
  allNetworkFlowHistogram: INetworkAnalysis[];
  globalSelectedTime: Omit<Required<IGlobalSelectedTime>, 'last'>;
  bordered?: boolean;
  markArea?: any;
}

const cardProps = {
  bordered: true,
  size: 'small',
  style: { marginBottom: 10 },
  bodyStyle: { height: chartHeight, padding: 6 },
};

const nameMap = {
  bindWidth: '总带宽',
};

function NetworkFlowMetric(props: Props) {
  const { allNetworkFlowHistogram = [], dispatch, globalSelectedTime, bordered, markArea } = props;

  const { startTime, endTime, interval = 60, startTimestamp, endTimestamp } = globalSelectedTime;

  const flowData = useMemo<TimeAxisChartData[]>(() => {
    return allNetworkFlowHistogram.map((item) => {
      return {
        timestamp: item.timestamp,
        bindWidth: (item.totalBytes * 8) / interval,
      };
    });
  }, [allNetworkFlowHistogram, interval]);

  const [queryLoading, setQueryLoading] = useState(true);

  useEffect(() => {
    setQueryLoading(true);
    dispatch({
      type: 'homeModel/queryAllNetworkFlowHistogram',
      payload: {
        startTime,
        endTime,
        interval,
        dsl: `| gentimes timestamp start="${startTime}" end="${endTime}"`,
      },
    }).then(() => {
      setQueryLoading(false);
    });
  }, [dispatch, endTime, interval, startTime]);

  return (
    <Card {...(bordered ? { ...cardProps, bordered } : cardProps)} size="small" title="流量统计">
      {queryLoading ? (
        chartLoading
      ) : (
        // <ReactECharts option={option} needPrettify={false} opts={{ height: chartHeight }} />
        <TimeAxisChart
          data={flowData}
          brush={true}
          showLegend={true}
          startTime={startTimestamp}
          endTime={endTimestamp}
          interval={interval}
          nameMap={nameMap}
          unitConverter={convertBandwidth}
          markArea={markArea}
        />
      )}
    </Card>
  );
}
export default connect(
  ({ homeModel: { allNetworkFlowHistogram }, appModel: { globalSelectedTime } }: ConnectState) => {
    return {
      allNetworkFlowHistogram,
      globalSelectedTime,
    };
  },
)(NetworkFlowMetric);
