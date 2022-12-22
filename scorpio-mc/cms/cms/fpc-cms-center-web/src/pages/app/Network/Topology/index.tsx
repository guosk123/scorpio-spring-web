import AutoHeightContainer from '@/components/AutoHeightContainer';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import TopologyGraph from '@/components/TopologyGraph';
import {
  clientUser,
  firewall,
  router,
  server,
  switchMachine,
  WAN,
} from '@/components/TopologyGraph/template';
import type { EventCallbacks, NodeProperties } from '@/components/TopologyGraph/typing';
import type { ConnectState } from '@/models/connect';
import type { INetworkTopology } from '@/services/app/networkTopology';
import { convertBandwidth, parseArrayJson, snakeCase } from '@/utils/utils';
import type { Edge, Graph, Model } from '@antv/x6';
import { Button, Col, message, Result, Row, Select, Space } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch, LogicalSubnetModelState, NetworkModelState } from 'umi';
import { history, useSelector } from 'umi';
import { v1 as uuidv1 } from 'uuid';
import type { INetworkStatData } from '../../analysis/typings';
import { ESortDirection } from '../../analysis/typings';
import { getColumns } from '../List/constant';
import { queryAllNetworkStat } from '../service';
import { getBytepsAvg, getTcpEstablishedSuccessRate } from '../utils';
import styles from './index.less';

export interface ITopology {
  dispatch: Dispatch;
  globalSelectedTime: Required<IGlobalTime>;
  submitting?: boolean;
  timeInfo?: {
    startTime: number;
    endTime: number;
    interval: number;
  };
  onEdgeClick?: (networkId?: string) => void;
}

const Topology: React.FC<ITopology> = React.memo(
  ({ dispatch, globalSelectedTime, submitting = false, timeInfo, onEdgeClick }) => {
    const graphRef = useRef<Graph>();
    const [editable, setEditable] = useState(false);
    const [metrics, setMetrics] = useState<string[]>([]);
    const [graphData, setGraphData] = useState<Model.ToJSONData>({ cells: [] });

    const {
      allNetworkGroup,
      allNetworkSensor,
      allNetworkSensorMap,
      allNetworkGroupMap,
      networkTopology,
    } = useSelector<ConnectState, NetworkModelState>((state) => state.networkModel);

    const { allLogicalSubnetMap, allLogicalSubnets } = useSelector<
      ConnectState,
      LogicalSubnetModelState
    >((state) => state.logicSubnetModel);

    const [, setPrevEdge] = useState<Edge>();

    const [isSave, setIsSave] = useState(false);

    const graphContainerRef = useRef<HTMLDivElement>(null);

    const [edges, setEdges] = useState<Edge[]>([]);

    // 是否已请求过拓扑图接口
    const [hasQueryTopology, setHasQueryTopology] = useState(false);

    const id = useMemo(() => uuidv1(), []);
    const columns = useMemo(() => {
      return getColumns(globalSelectedTime.startTime, globalSelectedTime.endTime);
    }, [globalSelectedTime.startTime, globalSelectedTime.endTime]);

    const allMetric = columns
      .map((col) => ({
        value: col.dataIndex as string,
        text: col.title as string,
        formatter: col.render,
      }))
      .filter((metric) => !['networkName'].includes(metric.value));

    const allMetricMap: Record<string, { text: string; value: string; formatter: any }> =
      allMetric.reduce((pre, cur) => {
        return {
          ...pre,
          [cur.value]: cur,
        };
      }, {});

    useEffect(() => {
      if (networkTopology?.metric) {
        const res = parseArrayJson(networkTopology.metric);
        setMetrics(res);
      }
    }, [networkTopology?.metric]);

    // 计算时间
    const selectedTimeInfo = useMemo(() => {
      if (timeInfo) {
        return {
          startTime: moment(timeInfo.startTime).format(),
          endTime: moment(timeInfo.endTime).format(),
          interval: timeInfo.interval,
        };
      }
      return globalSelectedTime;
    }, [globalSelectedTime, timeInfo]);

    const isEmbed = useMemo(() => {
      return timeInfo !== undefined;
    }, [timeInfo]);

    const hasNetwork = useMemo(() => {
      if (networkTopology.topology) {
        const j = JSON.parse(networkTopology.topology) as Model.ToJSONData;
        return j.cells.length > 0;
      }
      return false;
    }, [networkTopology.topology]);

    useEffect(() => {
      // 获取网络拓扑
      dispatch({
        type: 'networkModel/queryNetworkTopology',
      }).then(() => {
        setHasQueryTopology(true);
      });
      // 获取网络组
      dispatch({
        type: 'networkModel/queryAllNetworkGroups',
      });

      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    useEffect(() => {
      if (!editable && edges.length && metrics.length) {
        queryAllNetworkStat({
          sortProperty: snakeCase('totalBytes'),
          sortDirection: ESortDirection.DESC,
          startTime: selectedTimeInfo.startTime,
          endTime: selectedTimeInfo.endTime,
          interval: selectedTimeInfo.interval,
        }).then(({ result }: { result: INetworkStatData[] }) => {
          if (result && Array.isArray(result)) {
            const networkData = result?.reduce((prev, current) => {
              return {
                ...prev,
                [current.networkId || (current.networkGroupId as string)]: current,
              };
            }, {});

            edges.forEach((edge) => {
              const { value: networkId } = edge.getData() || {};
              if (networkId) {
                let data: INetworkStatData | null = null;
                let hasData = false;
                if (networkData) {
                  data = networkData[networkId];
                  hasData = !!(data && Object.keys(data).length > 0);
                }
                const showData: Record<string, any> = hasData
                  ? metrics.reduce((prev, current) => {
                      // 平均带宽 需要从接口数据中算出来，不能直接取到
                      if (current === 'bandwidth') {
                        return {
                          ...prev,
                          [allMetricMap[current].text]:
                            allNetworkGroupMap[networkId]?.bandwidth ||
                            allNetworkSensorMap[networkId]?.bandwidth ||
                            allLogicalSubnetMap[networkId]?.bandwidth ||
                            0,
                        };
                      }
                      if (current === 'type') {
                        return {
                          ...prev,
                          [allMetricMap[current].text]: allNetworkGroupMap[networkId]
                            ? '网络组'
                            : allNetworkSensorMap[networkId]
                            ? '探针网络'
                            : allLogicalSubnetMap[networkId]
                            ? '逻辑子网'
                            : '',
                        };
                      }
                      if (current === 'bytepsAvg') {
                        return {
                          ...prev,
                          [allMetricMap[current].text]: convertBandwidth(
                            getBytepsAvg(
                              (data as INetworkStatData).totalBytes,
                              selectedTimeInfo.startTime,
                              selectedTimeInfo.endTime,
                            ) * 8,
                          ),
                        };
                      }
                      if (current === 'tcpEstablishedSuccessRate') {
                        return {
                          ...prev,
                          [allMetricMap[current].text]: `${getTcpEstablishedSuccessRate(
                            (data as INetworkStatData).tcpEstablishedSuccessCounts,
                            (data as INetworkStatData).tcpEstablishedFailCounts,
                          )}%`,
                        };
                      }
                      const dataValue: number = (data && data[current]) || 0;
                      return {
                        ...prev,
                        [allMetricMap[current].text]: allMetricMap[current].formatter
                          ? allMetricMap[current].formatter(dataValue)
                          : dataValue,
                      };
                    }, {})
                  : {};

                const networkName = edge.labels[0];
                const newLabels: string[] = [];
                if (hasData) {
                  Object.keys(showData).forEach((field) => {
                    newLabels.push(`${field}: ${showData[field] || 0}`);
                  });
                } else {
                  newLabels.push('没有相关数据');
                }

                networkName.position = {
                  distance: 0.5,
                  offset: {
                    y: 20 * -Math.floor(newLabels.length / 2),
                  },
                };
                const edgeLabels: Edge.Label[] = [
                  networkName,
                  ...newLabels.map((label, index, labels) => {
                    return {
                      attrs: {
                        label: { text: label },
                      },
                      position: {
                        distance: 0.5,
                        offset: {
                          y: 20 * (index + 1 - Math.floor(labels.length / 2)),
                        },
                      },
                    };
                  }),
                ];

                edge.setLabels(edgeLabels);
              }
            });
          }
        });
      }
    }, [
      allLogicalSubnetMap,
      allMetricMap,
      allNetworkGroupMap,
      allNetworkSensorMap,
      dispatch,
      edges,
      editable,
      metrics,
      selectedTimeInfo,
    ]);

    useEffect(() => {
      if (networkTopology.topology) {
        const graphDataFromApi = JSON.parse(networkTopology.topology) as Model.ToJSONData;
        setGraphData(graphDataFromApi);
      }
    }, [networkTopology]);

    const networks = useMemo(() => {
      const groups = allNetworkGroup.map((net) => ({ id: net.id, name: net.name }));
      const nets = allNetworkSensor.map((net) => ({
        id: net.networkInSensorId,
        name: net.name || net.networkInSensorName,
      }));
      const subNets = allLogicalSubnets.map((net) => ({ id: net.id, name: net.name }));
      return nets
        .concat(subNets)
        .concat(groups)
        .map((item) => ({ label: item.name, value: item.id }));
    }, [allLogicalSubnets, allNetworkGroup, allNetworkSensor]);

    const handleSave = () => {
      setIsSave(true);
      if (graphRef.current) {
        const savedEdges = graphRef.current.getEdges();
        savedEdges.forEach((edge) => {
          edge.attr('line/strokeDasharray', null);
          edge.attr('line/style', null);
          edge.removeTools();
        });
        const graphJson = graphRef.current.toJSON();
        setGraphData(graphJson);
        dispatch({
          type: 'networkModel/updateNetworkTopology',
          payload: {
            topology: JSON.stringify(graphJson),
            metric: JSON.stringify(metrics),
          } as INetworkTopology,
        });
      }
    };

    const candidateNode = useMemo<NodeProperties[]>(() => {
      return [clientUser, WAN, server, router, switchMachine, firewall];
    }, []);

    const handleGraphReady = useCallback((graph: Graph) => {
      graphRef.current = graph;
    }, []);

    const handleChange = useCallback((value) => {
      if (value.length > 4) {
        message.warning('最多选择4项指标');
        return;
      }
      setMetrics(value);
    }, []);

    const handleEditOn = () => {
      setEditable(true);
    };

    const handleEditOff = () => {
      const graphInstance = graphRef.current;
      if (graphInstance && isSave) {
        const graphJson = graphInstance.toJSON();
        setGraphData(graphJson);
        setIsSave(false);
      }
      setEditable(false);
    };

    const handleHeightChange = useCallback((height) => {
      if (graphRef.current) {
        graphRef.current.resize(undefined, height);
      }
    }, []);

    const handleCellLoaded = useCallback(() => {
      if (graphRef.current) {
        const g = graphRef.current;
        setEdges(g.getEdges());

        // 如果是内嵌的网络拓扑图， 默认使一天带网络的边为选中状态
        if (onEdgeClick) {
          const find = g.getEdges().find((e) => {
            const { value: networkId } = e.getData() || {};
            if (networkId) {
              setPrevEdge((prev) => {
                prev?.setAttrs({ line: { stroke: '#5F95FF' } });
                e.setAttrs({ line: { stroke: '#ff0011' } });
                return e;
              });

              onEdgeClick(networkId);
              return true;
            }
            return false;
          });
          if (!find) {
            onEdgeClick(undefined);
          }
        }
      }
    }, [onEdgeClick]);

    const handleEdgeClick = useCallback(
      (e: JQuery.ClickEvent, edge: Edge) => {
        const { value: networkId } = edge.getData() || {};

        if (networkId && onEdgeClick) {
          setPrevEdge((prev) => {
            prev?.setAttrs({ line: { stroke: '#5F95FF' } });
            edge.setAttrs({ line: { stroke: '#ff0011' } });
            return edge;
          });
          onEdgeClick(networkId);
        }
      },
      [onEdgeClick],
    );

    const events = useMemo<EventCallbacks>(() => {
      if (handleEdgeClick) {
        return {
          edgeClick: handleEdgeClick,
        };
      }
      return {};
    }, [handleEdgeClick]);

    // 已请求过拓扑图接口后，判断是否算然提示信息
    if (hasQueryTopology && !hasNetwork && isEmbed) {
      return (
        <Result
          status="warning"
          subTitle="尚未配置网络拓扑图，请先配置网络拓扑"
          extra={
            <Button
              type="primary"
              key="console"
              onClick={() => {
                history.push('/network/topology');
              }}
            >
              编辑拓扑图
            </Button>
          }
        />
      );
    }

    return (
      <>
        <AutoHeightContainer
          onHeightChange={handleHeightChange}
          autoHeight={!isEmbed}
          headerRender={
            <Row gutter={{ xs: 8, sm: 12, md: 16 }}>
              {editable && (
                <Col span={14}>
                  <Select
                    mode={'multiple'}
                    allowClear
                    placeholder="请选择需要查看的指标"
                    value={metrics}
                    style={{ width: '100%' }}
                    onChange={handleChange}
                  >
                    {allMetric.map((item) => {
                      return (
                        <Select.Option key={item.value} value={item.value}>
                          {item.text}
                        </Select.Option>
                      );
                    })}
                  </Select>
                </Col>
              )}
              {!isEmbed && (
                <Col offset={editable ? 0 : 14} span={10} style={{ textAlign: 'right' }}>
                  <Space>
                    {editable ? (
                      <Button type="primary" onClick={handleEditOff}>
                        取消
                      </Button>
                    ) : (
                      <Button type="primary" onClick={handleEditOn}>
                        编辑
                      </Button>
                    )}
                    {editable && (
                      <Button type="primary" onClick={handleSave} loading={submitting}>
                        保存
                      </Button>
                    )}
                  </Space>
                </Col>
              )}
            </Row>
          }
        >
          <div className={styles.graphContainer} ref={graphContainerRef}>
            <TopologyGraph
              id={id}
              onGrapgReady={handleGraphReady}
              editable={editable}
              cells={graphData}
              graphEvents={events}
              options={{
                edge: {
                  inputType: 'select',
                  selectItem: networks,
                },
              }}
              nodes={candidateNode}
              cellLoaded={handleCellLoaded}
            />
          </div>
        </AutoHeightContainer>
      </>
    );
  },
);

const mapStateToProps = ({
  logicSubnetModel: { allLogicalSubnets },
  appModel: { globalSelectedTime },
  loading: { effects },
}: ConnectState) => ({
  allLogicalSubnets,
  globalSelectedTime,
  submitting: effects['networkModel/updateNetworkTopology'],
});

export default connect(mapStateToProps)(Topology);