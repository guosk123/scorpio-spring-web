import { Card, Select, Space, Tooltip } from 'antd';
// import type { ColumnProps } from 'antd/lib/table';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import type { ConnectState } from '@/models/connect';
// import { EMetricApiType } from '@/common/api/analysis';
import { useSelector } from 'umi';
// import { ANALYSIS_APPLICATION_TYPE_ENUM } from '@/common/app';
import { ESortDirection } from '@/pages/app/analysis/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { queryHistogramData } from '../../../service';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts from '@/components/ReactECharts';
import { IShowCategory } from '../../../typings';
import type { IApplicationMap } from '@/pages/app/Configuration/SAKnowledge/typings';
import { v1 as uuidv1 } from 'uuid';
import { BOOL_NO } from '@/common/dict';
import { bytesToSize } from '@/utils/utils';
import { tableTop } from '../../../typings';
import usePieChartLabelColor from '@/utils/hooks/usePieChartLabelColor';
import { BarChartOutlined, PieChartOutlined } from '@ant-design/icons';

export interface IIpImageRadioHistorgramProps {
  title: string;
  height?: number;
  IpAddress: string;
  category: string;
  networkId: string;
  globalSelectTime: IGlobalTime;
}
const IpImageRadioHistorgram: React.FC<IIpImageRadioHistorgramProps> = ({
  title,
  height = 400,
  IpAddress,
  category,
  networkId,
  globalSelectTime,
}) => {
  const [isLoading, setIsLoading] = useState(true);
  const applicationDic = useSelector<ConnectState, IApplicationMap>(
    (state) => state.SAKnowledgeModel.allApplicationMap,
  );
  const applicationArr = Object.values(applicationDic);
  const [applicationContent, setApplicationContent] = useState([]);
  const [top, setTop] = useState(tableTop[0]);
  const labelColor = usePieChartLabelColor();

  const queryParams = useMemo(() => {
    return {
      sourceType: 'network',
      packetFileId: '',
      networkId: networkId,
      startTime: globalSelectTime.startTime,
      endTime: globalSelectTime.endTime,
      ipResponder: IpAddress,
      ipInitiator: IpAddress,
      sortProperty: 'tcp_established_counts',
      queryProperty: 'application',
      sortDirection: ESortDirection.DESC,
      dsl: `(network_id<Array>=${networkId}) | gentimes report_time start="${globalSelectTime.startTime}" end="${globalSelectTime.endTime}"`,
      // drilldown: BOOL_NO,
      // queryId: uuidv1(),
      count: top,
    };
  }, [IpAddress, globalSelectTime.endTime, globalSelectTime.startTime, networkId, top]);

  const reQueryApplicationContent = useCallback(async () => {
    setIsLoading(true);
    const { success, result } = await queryHistogramData(queryParams);
    if (success) {
      setApplicationContent(result);
      setIsLoading(false);
    }
  }, [queryParams]);

  useEffect(() => {
    reQueryApplicationContent();
  }, [reQueryApplicationContent]);

  const converter = useMemo(() => {
    return applicationContent.map((item: any) => {
      const neededMap = applicationArr.find((i) => item.application_id == i.applicationId);
      // console.log(neededMap);
      return {
        name: neededMap?.nameText ? neededMap?.nameText : item.nameText,
        applicationId: neededMap?.applicationId,
        totalBytes: item.totalBytes,
        tcpEstablishedCounts: item.tcpEstablishedCounts,
        tcpEstablishedFailCounts: item.tcpEstablishedFailCounts,
      };
    });
  }, [applicationArr, applicationContent]);

  const pieData = useMemo(() => {
    const appFlow: any = converter.map((item: any) => ({
      name: item.name,
      value: item.totalBytes,
    }));
    return {
      appFlow,
    };
  }, [converter]);

  const histogramData = useMemo(() => {
    // console.log(converter, 'converter');
    // const topIndex = ['connections', '访问次数', '失败次数'];
    const appConnections = converter
      .map((item: any) => ({
        connections: item.name,
        ['访问次数']: item.tcpEstablishedCounts,
        ['失败次数']: item.tcpEstablishedFailCounts,
      }))
      .sort((a, b) => b.访问次数 - a.访问次数);
    return [...appConnections];
  }, [converter]);

  const flowRankingOptions = useMemo<ECOption>(() => {
    return {
      legend: {},
      tooltip: {},
      dataset: {
        dimensions: ['connections', '访问次数', '失败次数'],
        source: histogramData,
      },
      xAxis: { type: 'category' },
      yAxis: {
        // axisLabel: {
        //   formatter: '{value}',
        // },
      },
      series: [{ type: 'bar' }, { type: 'bar' }],
      // dataZoom:[
      //   {
      //     type: 'slider',
      //     show: true,
      //     // xAxisIndex:[0],
      //     start: 0,
      //     end: 10,
      //     filterMode: 'filter',
      //     handleSize: 8,
      //     top: '80%',
      //   }
      // ],
    };
  }, [histogramData]);

  const flowRatioOptions = useMemo<ECOption>(() => {
    let hasLabel = true;
    if (pieData.appFlow.length > 10) {
      hasLabel = false;
    }
    return {
      tooltip: {
        trigger: 'item',
        formatter: (params: any) => {
          return `${params.name}: ${bytesToSize(params.value)}`;
        },
      },
      legend: {
        orient: 'vertical',
        left: 'right',
        top: 'middle',
      },
      xAxis: {
        show: false,
      },
      series: [
        {
          type: 'pie',
          right: '15%',
          radius: ['50%', '70%'],
          avoidLabelOverlap: true,
          label: {
            formatter: (params: any) => {
              // console.log(params, 'params');
              return `${params.name}: ${params.percent.toFixed(2)}%`;
            },
            show: hasLabel,
            color: labelColor,
            position: 'outer',
          },
          labelLine: {
            show: true,
          },
          data: pieData.appFlow,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
            },
            // formatter: (params: any) => {
            //   return `${params.name}: ${params.percent.toFixed(2)}%`;
            // },
          },
        },
      ],
    };
  }, [labelColor, pieData.appFlow]);

  const SelectionBar = (
    <Space>
      <Select
        size="small"
        defaultValue={top}
        // style={{ width: 80 }}
        onChange={(key) => {
          setTop(key);
        }}
      >
        {tableTop.map((item) => (
          <Select.Option value={item} key={item}>{`Top${item}`}</Select.Option>
        ))}
      </Select>
      {category === IShowCategory.FlOWRATIO && (
        <Tooltip title={'饼状图预览'}>
          <span>
            <PieChartOutlined style={{ fontSize: 16, color: '#198ce1' }} />
          </span>
        </Tooltip>
      )}
      {category === IShowCategory.FLOWCONNECTIONS && (
        <Tooltip title={'柱状图预览'}>
          <span>
            <BarChartOutlined style={{ fontSize: 16, color: '#198ce1' }} />
          </span>
        </Tooltip>
      )}
    </Space>
  );
  return (
    <Card
      size="small"
      style={{ height: height, marginBottom: 15 }}
      title={title}
      loading={isLoading}
      extra={SelectionBar}
    >
      {/* 应用流量占比 */}
      {category === IShowCategory.FlOWRATIO && (
        <ReactECharts
          option={flowRatioOptions}
          style={{ margin: 10, marginBottom: 20 }}
          opts={{ height: 320 }}
        />
      )}
      {/* 应用流量排名 */}
      {category === IShowCategory.FLOWCONNECTIONS && (
        <ReactECharts
          option={flowRankingOptions}
          style={{ margin: 10, marginBottom: 20 }}
          opts={{ height: 320 }}
        />
      )}
    </Card>
  );
};

export default IpImageRadioHistorgram;
