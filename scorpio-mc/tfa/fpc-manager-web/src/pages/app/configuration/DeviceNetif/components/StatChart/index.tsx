import { EMetricApiType } from '@/common/api/analysis';
import { DEVICE_NETIF_CATEGORY_RECEIVE, DEVICE_NETIF_CATEGORY_REPLAY } from '@/common/dict';
import EChartsMessage from '@/components/Message';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { CHART_COLORS, timeAxis } from '@/components/ReactECharts';
import type { INetifAnalysis } from '@/pages/app/configuration/DeviceNetif/typings';
import { convertBandwidth, timeFormatter } from '@/utils/utils';
import { Card } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import React, { useCallback, useEffect, useState } from 'react';
import type { Dispatch } from 'umi';
import type { INetif } from '../../typings';

interface INetifStatsProps {
  dispatch: Dispatch;
  loading: boolean;
  netif: INetif;
  netifHistogram: INetifAnalysis[];
}

// 最近1小时，时间间隔指定成1分钟即可
const interval = 60;

const NetifStats: React.FC<INetifStatsProps> = ({ dispatch, netif, netifHistogram }) => {
  const [loading, setLoading] = useState<boolean>(false);

  const queryNetifHistogram = useCallback(
    (name: string) => {
      if (dispatch) {
        // 计算时间
        const timeInfo = timeFormatter(moment().subtract(1, 'hours').format(), moment().format());
        const { startTime, endTime } = timeInfo!;
        setLoading(true);
        (
          dispatch({
            type: 'deviceNetifModel/queryNetifHistogram',
            payload: {
              metricApi: EMetricApiType.netif,
              netifName: name,
              startTime,
              endTime,
              interval,
            },
          }) as unknown as Promise<any>
        ).then(() => {
          setLoading(false);
        });
      }
    },
    [dispatch],
  );

  useEffect(() => {
    if (netif) {
      queryNetifHistogram(netif.name);
    }
  }, [netif, queryNetifHistogram]);

  const seriesData = netifHistogram.map((row) => [
    moment(row.timestamp).valueOf(),
    // fix: 根据接口的分类，展示不同的数据
    (netif.category === DEVICE_NETIF_CATEGORY_REPLAY ? row.transmitBytes * 8 : row.totalBytes * 8) /
      interval,
  ]);

  const { name, category, categoryText } = netif || {};

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
        if (!Array.isArray(params)) {
          return '';
        }
        let label = '';
        label += `接口名称: ${name}`;
        label += '<br/>';
        label += `接口用途: ${categoryText}`;
        label += '<br/>';
        const time = params[0].axisValue;
        const start = moment(time);
        const end = moment(time).add(interval, 'seconds');
        if (start.format('YYYY-MM-DD') === end.format('YYYY-MM-DD')) {
          label += `<b>${start.format('YYYY-MM-DD HH:mm:ss')}-${end.format('HH:mm:ss')}</b><br/>`;
        } else {
          label += `<b>${start.format('YYYY-MM-DD HH:mm:ss')}-${end.format(
            'YYYY-MM-DD HH:mm:ss',
          )}</b><br/>`;
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
    color: [category === DEVICE_NETIF_CATEGORY_RECEIVE ? CHART_COLORS[0] : CHART_COLORS[1]],
    series: [
      {
        type: 'line',
        name: '带宽',
        connectNulls: false,
        symbol: 'none',
        data: seriesData,
      },
    ],
    grid: {
      bottom: 10,
    },
    legend: {
      show: false,
    },
  };

  const renderContent = () => {
    if (loading) {
      return <EChartsMessage height={300} message="loading" />;
    }
    if (seriesData.length === 0) {
      return <EChartsMessage height={300} />;
    }

    return (
      <ReactECharts needPrettify={false} loading={loading} option={option} opts={{ height: 300 }} />
    );
  };

  return (
    <Card title={`接口${netif.name}最近1小时流量统计`} size="small" style={{ marginTop: 10 }}>
      {renderContent()}
    </Card>
  );
};

export default connect()(NetifStats);
