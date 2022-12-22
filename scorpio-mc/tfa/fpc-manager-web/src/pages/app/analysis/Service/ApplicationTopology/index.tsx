import { EMetricApiType } from '@/common/api/analysis';
import { ANALYSIS_APPLICATION_TYPE_ENUM } from '@/common/app';
import { BOOL_NO } from '@/common/dict';
import AutoHeightContainer from '@/components/AutoHeightContainer';
import TopologyGraph from '@/components/TopologyGraph';
import type { ConnectState } from '@/models/connect';
import type { IServiceLink } from '@/pages/app/configuration/Service/typings';
import { convertBandwidth, timeFormatter } from '@/utils/utils';
import type { Graph, Model, Node } from '@antv/x6';
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch, IGlobalSelectedTime } from 'umi';
import { connect, useParams } from 'umi';
import { v1 as uuidv1 } from 'uuid';
import { fieldFormatterFuncMap, fieldsMapping } from '../../components/fieldsManager';
import type { IUriParams } from '../../typings';
import { ESourceType } from '../../typings';
import { getBytepsAvg, getTcpEstablishedSuccessRate } from '../List';
import styles from './index.less';

export interface ITopology {
  globalSelectedTime: Required<IGlobalSelectedTime>;
  serviceId?: string;
  networkId?: string;
  customTime?: {
    startTime: number;
    endTime: number;
    interval: number;
  };
  dispatch: Dispatch;
}

const Topology: React.FC<ITopology> = ({
  dispatch,
  globalSelectedTime,
  serviceId: serviceIdParam,
  networkId: networkIdParam,
  customTime,
}) => {
  const { serviceId, networkId } = useParams() as IUriParams;
  const graphRef = useRef<Graph>();
  // const [tooltipPosition, setTooltipPosition] = useState({ top: -1, left: -1 });
  // const [tooltipDisplay, setTooltipDisplay] = useState(false);
  // const [tooltipNode, setTooltipNode] = useState<ReactNode>(null);
  const graphContainerRef = useRef<HTMLDivElement>(null);
  const [serviceLink, setServiceLink] = useState<IServiceLink>({});

  const [nodes, setNodes] = useState<Node[]>([]);

  const interServiceId = useMemo(() => {
    return serviceIdParam || serviceId;
  }, [serviceId, serviceIdParam]);

  const interNetworkId = useMemo(() => {
    return networkIdParam || networkId;
  }, [networkId, networkIdParam]);

  const graphId = useMemo(() => uuidv1(), []);

  const metricField = useMemo<string[]>(() => {
    if (serviceLink.metric) {
      return JSON.parse(serviceLink.metric) as [];
    }
    return [];
  }, [serviceLink.metric]);

  const graphData = useMemo(() => {
    if (serviceLink.link) {
      const cellData = JSON.parse(serviceLink.link) as Model.ToJSONData;
      return cellData;
    }
    return { cells: [] };
  }, [serviceLink.link]);

  const selectedTimeInfo = useMemo(() => {
    if (customTime) {
      return timeFormatter(customTime.startTime, customTime.endTime);
    }
    return globalSelectedTime;
  }, [customTime, globalSelectedTime]);

  useEffect(() => {
    const dsl = `(${`network_id=${interNetworkId} AND service_id=${interServiceId}`}) | gentimes timestamp start="${
      selectedTimeInfo.startTime
    }" end="${selectedTimeInfo.endTime}"`;
    dispatch({
      type: 'npmdModel/queryNetworkFlowTableData',
      payload: {
        sourceType: ESourceType.SERVICE,
        networkId: interNetworkId,
        serviceId: interServiceId,
        metricApi: EMetricApiType.application,
        startTime: selectedTimeInfo.startTime as string,
        endTime: selectedTimeInfo.endTime as string,
        interval: selectedTimeInfo.interval as number,
        type: ANALYSIS_APPLICATION_TYPE_ENUM.应用,
        dsl,
        drilldown: BOOL_NO,
      },
    }).then((response: { result: any[] }) => {
      const { result } = response;
      const metricObjData = result.reduce((preValue, current) => {
        return {
          ...preValue,
          [current.applicationId]: current,
        };
      }, {});
      nodes.forEach((currNode) => {
        const appId = currNode?.data?.value;
        if (appId) {
          let data = {} as Record<string, any>;
          if (Object.keys(metricObjData).length > 0) {
            data = metricObjData[appId];
          }

          let labelIsRed = false;
          // 判断服务器响应平均时延是否大于 1s
          // 大于 1s 时标签变红
          if ((data.serverResponseLatencyAvg || 0) > 1000) {
            labelIsRed = true;
          }

          // 将节点的数据准备好
          const showData =
            data && Object.keys(data).length > 0
              ? metricField.reduce((prev, field) => {
                  if (field === 'bytepsAvg') {
                    return {
                      ...prev,
                      [fieldsMapping[field].name]: convertBandwidth(
                        getBytepsAvg(
                          data.totalBytes || 0,
                          selectedTimeInfo.startTime,
                          selectedTimeInfo.endTime,
                        ) * 8,
                      ),
                    };
                  }

                  if (field === 'tcpEstablishedSuccessRate') {
                    return {
                      ...prev,
                      [fieldsMapping[field].name]: `${getTcpEstablishedSuccessRate(
                        (data as any).tcpEstablishedSuccessCounts || 0,
                        (data as any).tcpEstablishedFailCounts || 0,
                      )} %`,
                    };
                  }
                  return {
                    ...prev,
                    [fieldsMapping[field].name]: fieldFormatterFuncMap[
                      fieldsMapping[field].formatterType
                    ](data[field]),
                  };
                }, {})
              : {};

          // 从节点数据中取出数据直接展示
          const tooltipText =
            Object.keys(showData).length > 0
              ? Object.keys(showData)
                  .map((field) => `${field}:${data ? showData[field] : 0}`)
                  .join('\n')
              : '没有相关数据';

          // @see: https://x6.antv.vision/zh/docs/tutorial/basic/cell#attrs-1
          // @see: https://antv-x6.gitee.io/zh/examples/node/native-node#ellipse
          currNode.attr({
            label: {
              text: tooltipText,
              ...(labelIsRed ? { style: { color: 'red' } } : {}),
              refX: '100%',
              refX2: 4,
              refY: 0.5,
              textAnchor: 'start',
              textVerticalAnchor: 'middle',
              fontSize: 12,
              lineHeight: 18,
            },
          });
        }
      });
    });
  }, [
    dispatch,
    interNetworkId,
    interServiceId,
    metricField,
    nodes,
    selectedTimeInfo.endTime,
    selectedTimeInfo.interval,
    selectedTimeInfo.startTime,
  ]);

  useEffect(() => {
    if (interServiceId) {
      dispatch({
        type: 'serviceModel/queryServiceLink',
        payload: {
          serviceId: interServiceId,
        },
      }).then((link: IServiceLink) => {
        setServiceLink(link);
      });
      dispatch({
        type: 'serviceModel/queryServiceDetail',
        payload: interServiceId,
      });
    }
  }, [dispatch, interServiceId]);

  const handleGraphReady = useCallback((graph: Graph) => {
    graphRef.current = graph;
  }, []);

  const handleCellLoaded = useCallback(() => {
    if (graphRef.current) {
      const g = graphRef.current;
      setNodes(g.getNodes());
    }
  }, []);

  const isEmbed = useMemo(() => {
    return !!(serviceIdParam && networkIdParam);
  }, [networkIdParam, serviceIdParam]);

  const handleHeightChange = useCallback((height) => {
    if (graphRef.current) {
      graphRef.current.resize(undefined, height);
    }
  }, []);

  const body = (
    <div className={styles.pageWrap}>
      <div className={styles.graphContainer} ref={graphContainerRef}>
        <TopologyGraph
          id={graphId}
          onGrapgReady={handleGraphReady}
          editable={false}
          cells={graphData}
          fit={isEmbed}
          cellLoaded={handleCellLoaded}
        />
      </div>
    </div>
  );

  if (isEmbed) {
    return body;
  }

  return (
    <>
      <AutoHeightContainer onHeightChange={handleHeightChange} autoHeight={!isEmbed}>
        {body}
      </AutoHeightContainer>
    </>
  );
};

const mapStateToProps = ({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
});

export default connect(mapStateToProps)(Topology);
