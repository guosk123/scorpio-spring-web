import type { IFilter } from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { timeAxis } from '@/components/ReactECharts';
import {
  fieldFormatterFuncMap,
  fieldsMapping,
} from '@/pages/app/analysis/components/fieldsManager';
import { completeTimePoint } from '@/pages/app/analysis/components/FlowAnalysis';
import { camelCase, getLinkUrl, jumpNewPage, timeFormatter } from '@/utils/utils';
import { Menu } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import { Fragment, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import type { AlertMetricTypes, IAlertMessage } from '../../../typings';
import { ALERT_METRIC_ENUM, EAlertCategory, WINDOW_SECONDS_NUMBER } from '../../../typings';

interface Props {
  alertDetail: IAlertMessage;
  dispatch: Dispatch;
}

const ALERT_ANALYSIS_WINDOW_TIME = {
  [WINDOW_SECONDS_NUMBER.TIME60S]: 30,
  [WINDOW_SECONDS_NUMBER.TIME300S]: 60,
  [WINDOW_SECONDS_NUMBER.TIME600S]: 60 * 2,
  [WINDOW_SECONDS_NUMBER.TIME1800S]: 60 * 6,
  [WINDOW_SECONDS_NUMBER.TIME3600S]: 60 * 12,
};
const analysisBannedMetrics: AlertMetricTypes[] = ['long_connections', 'broadcast_packets'];

function AlertAnalysis(props: Props) {
  const { alertDetail, dispatch } = props;

  const { networkId, serviceId } = alertDetail;
  const alertTime = alertDetail?.ariseTime || '';

  const tmpAlertDetail =
    alertDetail?.alertDefine?.thresholdSettings || alertDetail?.alertDefine?.trendSettings || {};

  const alertNumeratorMetric = tmpAlertDetail.metrics?.numerator?.metric;
  const alertDenominatorMetric = tmpAlertDetail.metrics?.denominator?.metric;

  const alertSourceType = tmpAlertDetail.metrics?.numerator?.sourceType;
  const alertSourceValue = tmpAlertDetail.metrics?.numerator?.sourceValue;

  const alertWindowSize = ALERT_ANALYSIS_WINDOW_TIME[tmpAlertDetail.fireCriteria?.windowSeconds];

  const payload = useMemo(() => {
    const tmpMetrics = [];
    if (alertNumeratorMetric) {
      tmpMetrics.push(alertNumeratorMetric);
    }
    if (alertDenominatorMetric) {
      tmpMetrics.push(alertDenominatorMetric);
    }
    const endTime = moment(moment(alertTime).format('YYYY-MM-DD HH:mm:00')).format();
    const startTime = moment(endTime).add(-alertWindowSize, 'm').format();
    return {
      startTime,
      endTime,
      interval: 60,
      sourceType: alertSourceType,
      sourceValue: alertSourceValue,
      networkId,
      serviceId,
      metrics: tmpMetrics.join(),
    };
  }, [
    alertDenominatorMetric,
    alertNumeratorMetric,
    alertSourceType,
    alertSourceValue,
    alertTime,
    alertWindowSize,
    networkId,
    serviceId,
  ]);

  const analysisFlag = useMemo(() => {
    return (
      !analysisBannedMetrics.includes(alertDenominatorMetric) &&
      !analysisBannedMetrics.includes(alertNumeratorMetric)
    );
  }, [alertDenominatorMetric, alertNumeratorMetric]);

  // 获取表格数据
  const [alertChartData, setAlertChartData] = useState<any>([]);

  // 判断是否有分母
  const isRatioLine = useMemo(() => {
    return !!alertDenominatorMetric;
  }, [alertDenominatorMetric]);

  useEffect(() => {
    if (!analysisFlag) {
      return;
    }
    dispatch({
      type: 'alertModel/queryAlertAnalysisDetail',
      payload,
    }).then((result: any) => {
      const chartData = completeTimePoint(
        result?.map((item: any) => {
          if (isRatioLine) {
            return [
              moment(item.timestamp).valueOf(),
              (
                item[camelCase(alertNumeratorMetric || '')] /
                item[camelCase(alertDenominatorMetric || '')]
              ).toFixed(0),
            ];
          }
          return [moment(item.timestamp).valueOf(), item[camelCase(alertNumeratorMetric || '')]];
        }),
        payload.startTime,
        payload.endTime,
        payload.interval,
      );
      setAlertChartData(chartData);
    });
  }, [alertDenominatorMetric, alertNumeratorMetric, analysisFlag, dispatch, isRatioLine, payload]);

  // 图表中的数据格式化方法
  const currentFormatter = useMemo(() => {
    const valueType = fieldsMapping[camelCase(alertNumeratorMetric || '')]?.formatterType;
    return fieldFormatterFuncMap[valueType];
  }, [alertNumeratorMetric]);

  const chartRef = useRef<any>();

  const markYAxis = useMemo(() => {
    const thresholdMarkLine = alertDetail?.alertDefine?.thresholdSettings?.fireCriteria?.operand;
    if (thresholdMarkLine || thresholdMarkLine === 0) {
      return thresholdMarkLine;
    }
    return alertDetail?.components[0]?.alertFireContext?.trendBaseline;
  }, [alertDetail]);

  const chartOption: ECOption = useMemo(() => {
    // 实时统计时，过滤掉特殊的统计
    return {
      legend: {
        show: true,
        formatter(name) {
          return isRatioLine && tmpAlertDetail?.metrics?.isRatio ? '比值' : name;
        },
      },
      grid: {
        right: 50,
      },
      xAxis: {
        ...timeAxis,
        axisLine: {
          show: false,
        },
        axisTick: {
          show: false,
        },
      },
      yAxis: {
        type: 'value',
        max: (value) => {
          const tmpMax =
            alertDetail?.alertDefine?.thresholdSettings?.fireCriteria?.operand ||
            alertDetail?.components[0]?.alertFireContext?.trendBaseline;
          if (value.max > tmpMax) {
            return value.max;
          }
          return tmpMax;
        },
        axisLabel: {
          formatter: (value: number) => {
            if (isRatioLine) {
              return String(value);
            }
            if (isRatioLine && alertDetail.category === EAlertCategory.TREND) {
              return `${value}%`;
            }
            return currentFormatter ? currentFormatter(value) : value.toString();
          },
        },
      },
      series: [
        {
          name: `${ALERT_METRIC_ENUM[alertNumeratorMetric]}`,
          data: alertChartData,
          type: 'line',
          // smooth: true,
          markLine: {
            symbol: 'none',
            data: [
              {
                silent: false,
                lineStyle: {
                  type: 'solid',
                  color: 'rgba(174, 87, 108, 1)',
                },
                yAxis: markYAxis,
              },
            ],
          },
        },
      ],
      tooltip: currentFormatter
        ? {
            formatter: (params: any) => {
              let label = `${params[0]?.axisValueLabel}<br/>`;
              if (!params?.length) {
                label = `${moment(params?.value[0]).format('YYYY-MM-DD HH:mm:ss')}<br/>`;
                label += `${params.marker}${params.seriesName}: ${currentFormatter(
                  params.value[1],
                )}<br/>`;
              }
              for (let i = 0; i < params.length; i += 1) {
                if (isRatioLine) {
                  label += `${params[i].marker}比值: ${params[i].value[1]} <br/>`;
                } else {
                  label += `${params[i].marker}${params[i].seriesName}: ${currentFormatter(
                    params[i].value[1],
                  )}<br/>`;
                }
              }
              return label;
            },
          }
        : {},
    };
  }, [
    alertNumeratorMetric,
    alertChartData,
    markYAxis,
    currentFormatter,
    isRatioLine,
    tmpAlertDetail?.metrics?.isRatio,
    alertDetail?.alertDefine?.thresholdSettings?.fireCriteria?.operand,
    alertDetail?.components,
    alertDetail.category,
  ]);

  const [brushTime, setBrushTime] = useState([0, 0]);

  const handleBrushEnd = useMemo(() => {
    const internalHandleBrushEnd = (from: number, to: number) => {
      // 时间跨度小于两分钟时;
      if ((to - from) / 1000 < 120) {
        const diffSeconds = 120 - (to - from) / 1000;
        const offset = diffSeconds / 2;
        setBrushTime([from - offset * 1000, to + offset * 1000]);
      } else {
        const { startTime, endTime } = timeFormatter(from, to);
        setBrushTime([new Date(startTime).valueOf(), new Date(endTime).valueOf() + 60 * 1000]);
      }
    };

    return internalHandleBrushEnd;
  }, []);

  const filter: IFilter[] = useMemo(() => {
    const res: IFilter[] = [];
    if (serviceId) {
      res.push({
        field: 'service_id',
        operator: EFilterOperatorTypes.EQ,
        operand: serviceId,
      });
    }
    if (networkId) {
      res.push({
        field: 'network_id',
        operator: EFilterOperatorTypes.EQ,
        operand: networkId,
      });
    }
    return res;
  }, [networkId, serviceId]);

  const handleMenuClick = useCallback(
    (info: any) => {
      const url = getLinkUrl(
        `/analysis/trace/${info.key}?from=${brushTime[0]}&to=${brushTime[1]}&timeType=${
          ETimeType.CUSTOM
        }&filter=${encodeURIComponent(JSON.stringify(filter))}`,
      );
      jumpNewPage(url);

      chartRef.current?.hideMenu();
    },
    [brushTime, filter],
  );

  const brushMenuChildren = useMemo(() => {
    return (
      <Menu onClick={handleMenuClick}>
        <Menu.Item key="flow-record">详单分析</Menu.Item>
      </Menu>
    );
  }, [handleMenuClick]);

  return (
    <Fragment>
      {analysisFlag && (
        <div style={{ height: 300, width: 800 }}>
          <ReactECharts
            ref={chartRef}
            option={chartOption}
            style={{ height: '100%' }}
            onBrushEnd={handleBrushEnd}
            brushMenuChildren={brushMenuChildren}
          />
        </div>
      )}
    </Fragment>
  );
}
export default connect()(AlertAnalysis);
