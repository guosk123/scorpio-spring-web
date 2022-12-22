import { Card } from 'antd';
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
import { DataShowedType, IShowCategory } from '../../../typings';
import type { IApplicationMap } from '@/pages/app/configuration/SAKnowledge/typings';
// import { v1 as uuidv1 } from 'uuid';
// import { BOOL_NO } from '@/common/dict';
import { bytesToSize } from '@/utils/utils';
import { tableTop } from '../../../typings';
import usePieChartLabelColor from '@/utils/hooks/usePieChartLabelColor';
import ExtraBar from '../ExtraBar';

export interface IIpImageRadioHistorgramProps {
  title: string;
  height?: number;
  IpAddress: string;
  category: string;
  networkId: string | null;
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
    let queryDsl = `| gentimes timestamp start="${globalSelectTime.startTime}" end="${globalSelectTime.endTime}"`;
    if (networkId) {
      queryDsl = `(network_id<Array>=${networkId}) ` + queryDsl;
    }
    let sortProperty = 'total_bytes';
    if (category == IShowCategory.FLOWCONNECTIONS) {
      sortProperty = 'tcp_established_counts';
    }
    return {
      sourceType: 'network',
      packetFileId: '',
      networkId: networkId,
      startTime: globalSelectTime.startTime,
      endTime: globalSelectTime.endTime,
      ipResponder: IpAddress,
      ipInitiator: IpAddress,
      sortProperty: sortProperty,
      queryProperty: 'application',
      sortDirection: ESortDirection.DESC,
      dsl: queryDsl,
      // drilldown: BOOL_NO,
      // queryId: uuidv1(),
      count: top,
    };
  }, [IpAddress, category, globalSelectTime.endTime, globalSelectTime.startTime, networkId, top]);

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
    // const topIndex = ['connections', '访问次数', '失败次数'];
    const appConnections = converter
      .map((item: any) => ({
        connections: item.name,
        ['访问次数']: item.tcpEstablishedCounts,
        ['失败次数']: item.tcpEstablishedFailCounts,
      }))
      .sort((a, b) => b.访问次数 - a.访问次数);
    return {
      data: [...appConnections],
      xAxisNames: converter.map((i) => i.name),
    };
  }, [converter]);

  const flowRankingOptions = useMemo<ECOption>(() => {
    return {
      legend: {},
      tooltip: {},
      dataset: {
        dimensions: ['connections', '访问次数', '失败次数'],
        source: histogramData.data,
      },
      xAxis: {
        type: 'category',
        data: histogramData.xAxisNames,
        axisLabel: {
          rotate: 70,
          interval: 0, // 标签设置为全部显示
          formatter(params: any) {
            let newParamsName = ''; // 最终拼接成的字符串
            const paramsNameNumber = params.length; // 实际标签的个数
            const provideNumber = 4; // 每行能显示的字的个数
            const rowNumber = Math.ceil(paramsNameNumber / provideNumber); // 换行的话，需要显示几行，向上取整

            // 条件等同于rowNumber>1
            if (paramsNameNumber > provideNumber) {
              for (let p = 0; p < rowNumber; p += 1) {
                let tempStr = ''; // 表示每一次截取的字符串
                const start = p * provideNumber; // 开始截取的位置
                const end = start + provideNumber; // 结束截取的位置
                // 此处特殊处理最后一行的索引值
                if (p === rowNumber - 1) {
                  // 最后一次不换行
                  tempStr = params.substring(start, paramsNameNumber);
                } else {
                  // 每一次拼接字符串并换行
                  tempStr = `${params.substring(start, end)}\n`;
                }
                newParamsName += tempStr; // 最终拼成的字符串
              }
            } else {
              // 将旧标签的值赋给新标签
              newParamsName = params;
            }
            // 将最终的字符串返回
            return newParamsName;
          },
        },
      },
      yAxis: {},
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

  const extraTool = useMemo(() => {
    let currentType: DataShowedType = DataShowedType.PIECHART,
      currentTypes: DataShowedType[] = [];
    if (category === IShowCategory.FlOWRATIO) {
      currentType = DataShowedType.PIECHART;
      currentTypes = [DataShowedType.PIECHART];
    }
    if (category === IShowCategory.FLOWCONNECTIONS) {
      currentType = DataShowedType.BARCHART;
      currentTypes = [DataShowedType.BARCHART];
    }

    return (
      <ExtraBar
        hasTopSelectionBar={true}
        top={top}
        changeTop={setTop}
        hasTypeChangeBar={true}
        types={currentTypes}
        showedType={currentType}
      />
    );
  }, [category, top]);

  return (
    <Card
      size="small"
      style={{ height: height, marginBottom: 15 }}
      bodyStyle={{ height: 'calc(100% - 41px)', padding: 5 }}
      title={title}
      loading={isLoading}
      extra={extraTool}
    >
      {/* 应用流量占比 */}
      {category === IShowCategory.FlOWRATIO && (
        <ReactECharts
          option={flowRatioOptions}
          style={{ margin: 10, marginBottom: 20 }}
          opts={{ height: 400 }}
        />
      )}
      {/* 应用流量排名 */}
      {category === IShowCategory.FLOWCONNECTIONS && (
        <ReactECharts
          option={flowRankingOptions}
          style={{ margin: 10, marginBottom: 20 }}
          opts={{ height: 400 }}
        />
      )}
    </Card>
  );
};

export default IpImageRadioHistorgram;
