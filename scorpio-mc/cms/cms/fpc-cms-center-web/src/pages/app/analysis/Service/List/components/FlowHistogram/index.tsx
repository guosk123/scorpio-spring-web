import type { ConnectState } from '@/models/connect';
import type { IMetricQueryParams, IPayloadStat } from '@/pages/app/analysis/typings';
import React, { useEffect, useMemo, useState } from 'react';
import type { Dispatch, IGlobalSelectedTime } from 'umi';
import { connect } from 'dva';
import MiniLine from '../MiniLine';

interface IServiceFlowHistogramProps {
  dispatch: Dispatch;
  selectedTime: IGlobalSelectedTime;

  networkGroupId?: string;
  networkId?: string;
  serviceId: string;
}
const ServiceFlowHistogram: React.FC<IServiceFlowHistogramProps> = ({
  dispatch,
  networkGroupId,
  serviceId,
  networkId,
  selectedTime,
}) => {
  const [flowHistogramData, setFlowHistogramData] = useState<IPayloadStat[]>([]);

  // TODO: 如何在时间变化时仅 请求一次数据;
  useEffect(() => {
    let mounted = true;
    let dsl = '(';
    if (networkId) {
      dsl += `network_id="${networkId?.includes('^') ? networkId.split('^')[1] : networkId}"`;
    }
    if (serviceId) {
      if (networkId) {
        dsl += ' AND ';
      }
      dsl += `service_id="${serviceId}"`;
    }
    dsl += ')';
    dsl += ` | gentimes timestamp start="${selectedTime.startTime}" end="${selectedTime.endTime}"`;

    dispatch({
      type: 'npmdModel/queryServiceFlowHistogram',
      payload: {
        networkId: networkId?.includes('^') ? networkId.split('^')[1] : networkId,
        serviceId,
        networkGroupId,
        startTime: selectedTime.startTime,
        endTime: selectedTime.endTime,
        interval: selectedTime.interval,
        dsl,
      } as IMetricQueryParams,
    }).then((res: IPayloadStat[]) => {
      if (mounted) {
        setFlowHistogramData(res);
      }
    });

    return () => {
      mounted = false;
    };
  }, [dispatch, networkId, networkGroupId, selectedTime, serviceId]);

  const chartData = useMemo(() => {
    return flowHistogramData.map((row) => [new Date(row.timestamp).valueOf(), row.bandwidth]);
  }, [flowHistogramData]);

  return <MiniLine data={chartData} />;
};

// export default React.memo(ServiceFlowHistogram);

export default connect(({ loading: { effects } }: ConnectState) => ({
  queryLoading: effects['npmdModel/queryServiceFlowHistogram'],
}))(ServiceFlowHistogram);
