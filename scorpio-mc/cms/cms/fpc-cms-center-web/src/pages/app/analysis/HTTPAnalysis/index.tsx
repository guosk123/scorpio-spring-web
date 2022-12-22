import { ChartCard } from '@/components/Charts';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts from '@/components/ReactECharts';
import TimeAxisChart from '@/components/TimeAxisChart';
import type { TimeAxisChartData } from '@/components/TimeAxisChart/typing';
import type { ConnectState } from '@/models/connect';
import { Col, Row } from 'antd';
import moment from 'moment';
import React, { useEffect, useMemo } from 'react';
import type { Dispatch } from 'umi';
import { connect, useParams } from 'umi';
import type { HttpAnalysisResult } from '../typings';

export interface IHTTPAnalysisProps {
  httpAnalysisData: HttpAnalysisResult;
  globalSelectedTime: Required<IGlobalTime>;
  dispatch: Dispatch;
}

const nameMap = {
  requestCounts: '请求数量',
  errorResponseCounts: '错误响应数量',
  responseCounts: '响应数量',
};

const HTTPAnalysis: React.FC<IHTTPAnalysisProps> = ({
  httpAnalysisData,
  globalSelectedTime,
  dispatch,
}) => {
  const { networkId } = useParams() as { networkId: string };

  const dsl = useMemo(() => {
    return `(network_id=${networkId}) | gentimes timestamp start="${globalSelectedTime.startTime}" end="${globalSelectedTime.endTime}" `;
  }, [networkId, globalSelectedTime.endTime, globalSelectedTime.startTime]);

  useEffect(() => {
    dispatch({
      type: 'npmdModel/queryHttpAnalysisData',
      payload: {
        dsl,
        interval: globalSelectedTime.interval,
      },
    });
  }, [dispatch, dsl, globalSelectedTime]);

  const chartOptions = useMemo(() => {
    let totalReq = 0;
    let totalRes = 0;
    let totalError = 0;

    httpAnalysisData.httpRequest.forEach((item) => {
      totalReq += item.requestCounts;
      totalRes += item.responseCounts;
      totalError += item.errorResponseCounts;
    });

    const httpMethodChartOption: ECOption = {
      xAxis: {
        type: 'category',
      },
      yAxis: {
        type: 'value',
      },
      dataset: [
        {
          dimensions: ['http_method', 'count'],
          source: httpAnalysisData.httpMethod.map((item) => {
            return [item.key, item.count];
          }),
        },
      ],
      series: [
        {
          type: 'bar',
          name: 'http_method',
          encode: {
            x: 'http_method',
            y: 'count',
          },
        },
      ],
    };

    const responseStatusCodeDistributionChartOption: ECOption = {
      xAxis: { type: 'category' },
      yAxis: {
        type: 'value',
      },
      dataset: [
        {
          dimensions: ['status_code', 'count'],
          source: httpAnalysisData.httpCode.map((item) => {
            return [item.key, item.count];
          }),
        },
      ],
      series: [
        {
          type: 'bar',
          name: '响应状态码',
          encode: {
            x: 'status_code',
            y: 'count',
          },
        },
      ],
    };

    const terminalTypeChartOption: ECOption = {
      xAxis: { type: 'category' },
      yAxis: {
        type: 'value',
      },
      dataset: [
        {
          dimensions: ['terminal_type', 'count'],
          source: httpAnalysisData.os.map((item) => {
            return [item.type, item.count];
          }),
        },
      ],
      series: [
        {
          type: 'bar',
          name: '终端类型',
          encode: {
            x: 'terminal_type',
            y: 'count',
          },
        },
      ],
    };

    return {
      httpMethodChartOption,
      responseStatusCodeDistributionChartOption,
      terminalTypeChartOption,
      totalReq,
      totalRes,
      totalError,
    };
  }, [httpAnalysisData]);

  const requestData: TimeAxisChartData[] = useMemo(() => {
    return httpAnalysisData.httpRequest.map((item) => {
      return {
        ...item,
        timestamp: new Date(item.timestamp).getTime(),
      };
    });
  }, [httpAnalysisData.httpRequest]);

  const timeObj = useMemo(() => {
    if (globalSelectedTime.startTime && globalSelectedTime.endTime) {
      return {
        startTime: new Date(globalSelectedTime.startTime).getTime(),
        endTime: new Date(globalSelectedTime.endTime).getTime(),
      };
    }
    return {
      startTime: moment().valueOf() - 30 * 60 * 1000,
      endTime: moment().valueOf(),
    };
  }, [globalSelectedTime.endTime, globalSelectedTime.startTime]);

  return (
    <>
      <section>
        <Row gutter={10}>
          {[
            {
              title: '请求总数',
              value: chartOptions.totalReq,
            },
            {
              title: '响应总数',
              value: chartOptions.totalRes,
            },
            {
              title: '错误总数（响应码4xx和5xx）',
              value: chartOptions.totalError,
            },
          ].map((item) => {
            const { title, value } = item;
            return (
              <Col span={8} key={title}>
                <ChartCard
                  title={title}
                  total={<span style={{ fontSize: 22 }}>{value}</span>}
                  bodyStyle={{ padding: '8px 10px 4px' }}
                  contentHeight={70}
                />
              </Col>
            );
          })}
        </Row>
      </section>
      <section>
        <Row gutter={10}>
          <Col span={12}>
            <TimeAxisChart
              data={requestData}
              startTime={timeObj.startTime}
              endTime={timeObj.endTime}
              interval={globalSelectedTime.interval || 60}
              nameMap={nameMap}
            />
          </Col>
          <Col span={12}>
            <ReactECharts option={chartOptions.httpMethodChartOption} opts={{ height: 300 }} />
          </Col>
          <Col span={12}>
            <ReactECharts
              option={chartOptions.responseStatusCodeDistributionChartOption}
              opts={{ height: 300 }}
            />
          </Col>
          <Col span={12}>
            <ReactECharts option={chartOptions.terminalTypeChartOption} opts={{ height: 300 }} />
          </Col>
        </Row>
      </section>
    </>
  );
};

export default connect(
  ({ npmdModel: { httpAnalysisData }, appModel: { globalSelectedTime } }: ConnectState) => ({
    httpAnalysisData,
    globalSelectedTime,
  }),
)(HTTPAnalysis);
