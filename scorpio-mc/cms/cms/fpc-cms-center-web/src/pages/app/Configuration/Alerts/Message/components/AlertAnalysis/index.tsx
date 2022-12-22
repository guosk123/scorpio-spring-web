import { useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { connect } from 'dva';
import moment from 'moment';
import type { AlertMetricTypes, IAlertMessage } from '../../../typings';
import { WINDOW_SECONDS_NUMBER } from '../../../typings';
import { EAlertCategory } from '../../../typings';
import { ALERT_METRIC_ENUM } from '../../../typings';
import { camelCase, timeFormatter } from '@/utils/utils';
import { Menu } from 'antd';
import type { Dispatch} from 'umi';
import { useParams } from 'umi';
import {
  fieldFormatterFuncMap,
  fieldsMapping,
} from '@/pages/app/analysis/components/fieldsManager';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { timeAxis } from '@/components/ReactECharts';
import { ETimeType, getGlobalTime, globalTimeFormatText } from '@/components/GlobalTimeSelector';
import { Fragment } from 'react';
import { AnalysisContext } from '@/pages/app/Network/Analysis';
import { jumpToAnalysisTabNew } from '@/pages/app/Network/Analysis/constant';
import { ENetworkTabs } from '@/pages/app/Network/typing';
import { ServiceAnalysisContext } from '@/pages/app/analysis/Service/index';
import { jumpToSericeAnalysisTab } from '@/pages/app/analysis/Service/constant';
import { EServiceTabs } from '@/pages/app/analysis/Service/typing';
import { completeTimePoint } from '@/pages/app/analysis/components/FlowAnalysis';
import type { IUriParams } from '@/pages/app/analysis/typings';

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
    alertDetail?.alertDefine?.thresholdSettings || alertDetail?.alertDefine?.trendSettings;

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
      interval: timeFormatter(startTime, endTime).interval,
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

  // 根据是否在标志位数组中存在对应的值，存在的话就设置为false,不存在的话就设置为true
  const analysisFlag = useMemo(() => {
    return (
      !analysisBannedMetrics.includes(alertDenominatorMetric) &&
      !analysisBannedMetrics.includes(alertNumeratorMetric)
    );
  }, [alertDenominatorMetric, alertNumeratorMetric]);

  // 判断是否有分母
  const isRatioLine = useMemo(() => {
    return !!alertDenominatorMetric;
  }, [alertDenominatorMetric]);

  // 获取表格数据
  const [alertChartData, setAlertChartData] = useState<any>([]);
  useEffect(() => {
    // 根据标志位去判断位置
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
                item[camelCase(alertNumeratorMetric)] / item[camelCase(alertDenominatorMetric)]
              ).toFixed(0),
            ];
          }
          return [moment(item.timestamp).valueOf(), item[camelCase(alertNumeratorMetric)]];
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
    const valueType = fieldsMapping[camelCase(alertNumeratorMetric)]?.formatterType;
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
          return isRatioLine ? '比值' : name;
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
  }, [alertNumeratorMetric, alertChartData, markYAxis, currentFormatter, alertDetail, isRatioLine]);

  const [brushTime, setBrushTime] = useState([0, 0]);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const nsUrlParams: IUriParams = useParams();

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

  const updateGlobalTime = useCallback(
    (from: number, to: number) => {
      let timeObj;
      if ((to - from) / 1000 < 120) {
        const diffSeconds = 120 - (to - from) / 1000;
        const offset = diffSeconds / 2;
        timeObj = timeFormatter(from - offset * 1000, to + offset * 1000);
      } else {
        timeObj = timeFormatter(from, to);
      }

      dispatch({
        type: 'appModel/updateGlobalTime',
        payload: getGlobalTime({
          relative: false,
          type: ETimeType.CUSTOM,
          custom: [
            moment(timeObj.startTime, globalTimeFormatText),
            moment(timeObj.endTime, globalTimeFormatText),
          ],
        }),
      });
    },
    [dispatch],
  );

  const [state, analysisDispatch] = useContext(
    serviceId ? ServiceAnalysisContext : AnalysisContext,
  );

  const handleMenuClick = useCallback(
    (info: any) => {
      // const url = getLinkUrl(
      //   `/analysis/${urlFragment}/${info.key}?from=${brushTime[0]}&to=${brushTime[1]}&timeType=${ETimeType.CUSTOM}`,
      // );
      // jumpNewPage(url);
      setIsModalVisible(false);
      if (info.key === 'flow-record') {
        updateGlobalTime(brushTime[0], brushTime[1]);
        jumpToAnalysisTabNew(state, analysisDispatch, ENetworkTabs.FLOWRECORD, {
          globalSelectedTime: { startTime: brushTime[0], endTime: brushTime[1] },
          networkId: nsUrlParams.networkId,
          serviceId: nsUrlParams.serviceId,
        });
        chartRef.current?.hideMenu();
      }
    },
    [
      analysisDispatch,
      brushTime,
      nsUrlParams.networkId,
      nsUrlParams.serviceId,
      state,
      updateGlobalTime,
    ],
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
