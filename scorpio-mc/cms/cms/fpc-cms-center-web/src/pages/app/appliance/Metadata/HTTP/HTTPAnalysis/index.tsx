import { ChartCard } from '@/components/Charts';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts from '@/components/ReactECharts';
import TimeAxisChart from '@/components/TimeAxisChart';
import type { TimeAxisChartData } from '@/components/TimeAxisChart/typing';
import type { ConnectState } from '@/models/connect';
import type { HttpAnalysisResult } from '@/pages/app/analysis/typings';
import { NetworkTypeContext } from '@/pages/app/Network/Analysis';
import { ServiceContext } from '@/pages/app/analysis/Service/index';
import type { INetworkTreeItem } from '@/pages/app/Network/typing';
import { ENetowrkType } from '@/pages/app/Network/typing';
import { Col, Row } from 'antd';
import moment from 'moment';
import React, { useContext, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, useParams } from 'umi';
import { queryHttpAnalysis } from '../../service';

export interface IHTTPAnalysisProps {
  globalSelectedTime: Required<IGlobalTime>;
  dispatch: Dispatch;
}

const nameMap = {
  requestCounts: '请求数量',
  errorResponseCounts: '错误响应数量',
  responseCounts: '响应数量',
};

const HTTPAnalysis: React.FC<IHTTPAnalysisProps> = ({ globalSelectedTime, dispatch }) => {
  const { networkId, serviceId } = useParams() as { networkId: string; serviceId: string };
  const [httpAnalysisData, setHttpAnalysisData] = useState<HttpAnalysisResult | null>(null);
  const [networkType] = useContext<[ENetowrkType, INetworkTreeItem[]] | any>(
    serviceId ? ServiceContext : NetworkTypeContext,
  );
  const dsl = useMemo(() => {
    return `${
      networkType === ENetowrkType.NETWORK && networkId ? `(network_id=${networkId})` : ''
    } | gentimes timestamp start="${globalSelectedTime.startTime}" end="${
      globalSelectedTime.endTime
    }" `;
  }, [networkId, globalSelectedTime.endTime, globalSelectedTime.startTime]);

  useEffect(() => {
    const queryData: any = {
      dsl,
      interval: globalSelectedTime.interval,
      serviceId,
    };
    if (networkType === ENetowrkType.NETWORK_GROUP) {
      queryData.networkGroupId = networkId;
    }
    queryHttpAnalysis(queryData).then((res) => {
      const { success, result } = res;
      if (success) {
        setHttpAnalysisData(result);
      }
    });
  }, [dispatch, dsl, globalSelectedTime]);

  const chartOptions = useMemo(() => {
    let totalReq = 0;
    let totalRes = 0;
    let totalError = 0;

    httpAnalysisData?.httpRequest.forEach((item) => {
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
          source: httpAnalysisData?.httpMethod.map((item) => {
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
          source: httpAnalysisData?.httpCode.map((item) => {
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
          source: httpAnalysisData?.os.map((item) => {
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
    if (!httpAnalysisData?.httpRequest) {
      return [];
    }
    return httpAnalysisData?.httpRequest.map((item) => {
      return {
        ...item,
        timestamp: new Date(item.timestamp).getTime(),
      };
    });
  }, [httpAnalysisData?.httpRequest]);

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

export default connect(({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
}))(HTTPAnalysis);
