// import { EMetricApiType } from '@/common/api/analysis';
// import { ANALYSIS_APPLICATION_TYPE_ENUM } from '@/common/app';
// import Message from '@/components/Message';
// import TimeAxisChart from '@/components/TimeAxisChart';
// import type { TimeAxisChartData } from '@/components/TimeAxisChart/typing';
// import type { ConnectState } from '@/models/connect';
// import { bytesToSize, snakeCase, timeFormatter } from '@/utils/utils';
// import { Card, Col, Progress, Row } from 'antd';
// import classnames from 'classnames';
// import { connect } from 'dva';
// import type { LineSeriesOption } from 'echarts';
// import moment from 'moment';
// import numeral from 'numeral';
// import { useCallback, useEffect, useMemo, useState } from 'react';
// import type { Dispatch, ISituationModelState } from 'umi';
// import Topology from '../../analysis/Network/Topology';
// import { queryAllNetworkStat } from '../../analysis/service';
// // import { queryAllNetworkStat } from '../../analysis/service';
// import type { IFlowAnalysisData, IFlowAppStatFileds } from '../../analysis/typings';
// import { ESortDirection } from '../../analysis/typings';
// import type { IApplicationMap } from '../../configuration/SAKnowledge/typings';
// import BarChart from '../components/BarChart';
// import FlowChat from '../components/FlowChat';
// import FullScreenCard, { HightContext } from '../components/FullScreenCard';
// import TimeAutoRefreshSelectBox from '../components/TimeAutoRefreshSelectBox';
// import { timeToNumber } from '../utils/timeTools';
// import styles from './index.less';

// const nameMap = {
//   tcpServerNetworkLatencyAvg: '服务端网络平均时延',
//   tcpClientNetworkLatencyAvg: '客户端网络平均时延',
//   serverResponseLatencyAvg: '服务端平均响应时延',
//   upRetransRate: '重传率（上行）',
//   downRetransRate: '重传率（下行）',
//   zeroWindowUp: '零窗口（上行）',
//   zeroWindowDown: '零窗口（下行）',
// };

// interface ITotalPayloadType {
//   downstreamBytes: number;
//   upstreamBytes: number;
//   totalBytes: number;
// }

// export interface ITimeInfo {
//   startTime: string;
//   endTime: string;
//   interval: number;
// }

// interface INetworkSituationProps {
//   dispatch: Dispatch;
//   situationModel: ISituationModelState;
//   allApplicationMap: IApplicationMap;
//   flowTopLoading: boolean | undefined;
//   conversitionTopLoading: boolean | undefined;
//   totalFlowLoading: boolean | undefined;
//   histogramLoading: boolean | undefined;
//   appFlowTopLoading: boolean | undefined;
// }
// const NetworkSituation = ({
//   dispatch,
//   allApplicationMap,
//   flowTopLoading,
//   totalFlowLoading,
//   conversitionTopLoading,
//   histogramLoading,
//   appFlowTopLoading,
//   situationModel: { flowHistogramData, startTime, endTime },
// }: INetworkSituationProps) => {
//   // 当前查看统计的网络
//   const [networkId, setNetworkId] = useState<string>();

//   // 当前查看统计的应用
//   const [applicationId, setApplicationId] = useState<string>();

//   const [appFlowTop, setAppFlowTop] = useState<IFlowAnalysisData[]>([]);

//   const [applicationHistogramData, setApplicationHistogramData] = useState<IFlowAppStatFileds[]>(
//     [],
//   );

//   const [ipTopData, setIpTopData] = useState<IFlowAnalysisData[]>([]);

//   const [ipConversationTopData, setIpConversationTopData] = useState<IFlowAnalysisData[]>([]);

//   const selectTimeInfo = useMemo<ITimeInfo>(() => {
//     const formatter = timeFormatter(startTime, endTime);
//     return {
//       startTime: formatter.startTime,
//       endTime: formatter.endTime,
//       interval: formatter.interval,
//     };
//   }, [endTime, startTime]);

//   useEffect(() => {
//     queryAllNetworkStat({
//       ...selectTimeInfo,
//       sortDirection: ESortDirection.DESC,
//       sortProperty: 'total_bytes',
//     });
//   }, [dispatch, selectTimeInfo]);

//   useEffect(() => {
//     if (networkId) {
//       const payload = {
//         ...selectTimeInfo,
//         networkId,
//         dsl: `(network_id=${networkId}) AND (service_id="") | gentimes timestamp start="${selectTimeInfo.startTime}" end="${selectTimeInfo.endTime}"`,
//       };
//       dispatch({
//         type: 'situationModel/queryApplicationFlowTableData',
//         payload: {
//           ...payload,
//           metricApi: EMetricApiType.application,
//           sortProperty: snakeCase('totalBytes'),
//           sortDirection: ESortDirection.DESC,
//           type: ANALYSIS_APPLICATION_TYPE_ENUM.应用,
//           sourceType: 'network',
//           dsl: `(type = ${ANALYSIS_APPLICATION_TYPE_ENUM.应用}) and ${payload.dsl}`,
//         },
//       }).then((flowTopData: IFlowAnalysisData[]) => {
//         setAppFlowTop(flowTopData);
//       });
//     }
//   }, [dispatch, networkId, selectTimeInfo]);

//   useEffect(() => {
//     const payload = {
//       ...selectTimeInfo,
//       networkId,
//       dsl: `(network_id=${networkId})| gentimes timestamp start="${selectTimeInfo.startTime}" end="${selectTimeInfo.endTime}"`,
//     };
//     if (networkId) {
//       // 日总流量
//       dispatch({
//         type: 'situationModel/queryNetworkPayloadHistogram',
//         payload: {
//           ...selectTimeInfo,
//           networkId,
//         },
//       });
//       // 用户日总流量top10
//       dispatch({
//         type: 'situationModel/queryIpTop',
//         payload,
//       }).then((data: IFlowAnalysisData[]) => {
//         setIpTopData(data.slice(0, 10));
//       });
//       // ip会话日总流量top10
//       dispatch({
//         type: 'situationModel/queryIpConversationTop',
//         payload,
//       }).then((data: any) => {
//         setIpConversationTopData(data);
//       });
//     }
//   }, [networkId, dispatch, selectTimeInfo]);

//   const [totalPayload, setTotalPayload] = useState<ITotalPayloadType>({
//     downstreamBytes: 0,
//     upstreamBytes: 0,
//     totalBytes: 0,
//   });

//   useEffect(() => {
//     if (networkId) {
//       dispatch({
//         type: 'situationModel/queryTotalPayload',
//         payload: {
//           networkId,
//           startTime: selectTimeInfo.startTime,
//           endTime: selectTimeInfo.endTime,
//         },
//       }).then((data: ITotalPayloadType) => {
//         setTotalPayload(data);
//       });
//     }
//   }, [dispatch, networkId, selectTimeInfo]);

//   //  计算流量统计相关数据
//   const computedFlowData = useMemo(() => {
//     const flowSeriesData: LineSeriesOption['data'] = [];
//     // 补点
//     const startTimestamp = moment(selectTimeInfo.startTime)
//       .add(selectTimeInfo.interval, 's')
//       .valueOf();
//     const endTimestamp = new Date(selectTimeInfo.endTime).getTime();
//     const diffCount = (endTimestamp - startTimestamp) / 1000 / selectTimeInfo.interval!;
//     // 时间补点
//     for (let index = 0; index < diffCount; index += 1) {
//       const time = startTimestamp + index * selectTimeInfo.interval * 1000;
//       let point = null;
//       for (let j = 0; j < flowHistogramData.length; j += 1) {
//         const row = flowHistogramData[j];
//         const pointTime = new Date(row.timestamp).getTime();
//         if (pointTime === new Date(time).getTime()) {
//           point = row;
//           break;
//         }
//       }

//       // @ts-ignore
//       flowSeriesData.push([time, point ? point.bandwidth || 0 : 0]);
//     }

//     return { flowSeriesData };
//   }, [flowHistogramData, selectTimeInfo]);

//   const applicationTopList = useMemo(() => {
//     if (appFlowTop.length > 0) {
//       // 更新当前展示的应用 ID
//       setApplicationId(String(appFlowTop[0].applicationId));
//     }

//     // 返回前10个应用
//     return appFlowTop.splice(0, 10);
//   }, [appFlowTop]);

//   useEffect(() => {
//     if (applicationId && networkId) {
//       dispatch({
//         type: 'situationModel/queryApplicationDetailHistogram',
//         payload: {
//           ...selectTimeInfo,
//           networkId,
//           id: applicationId,
//           dsl: `(network_id=${networkId} and type=${String(
//             ANALYSIS_APPLICATION_TYPE_ENUM.应用,
//           )})| gentimes timestamp start="${selectTimeInfo.startTime}" end="${
//             selectTimeInfo.endTime
//           }"`,
//         },
//       }).then((result: IFlowAppStatFileds[]) => {
//         setApplicationHistogramData(result);
//       });
//     }
//   }, [dispatch, networkId, applicationId, selectTimeInfo]);

//   const trendChartData = useMemo(() => {
//     const responseTimeData: TimeAxisChartData[] = [];
//     const retransmissionData: TimeAxisChartData[] = [];
//     const zeroWindowData: TimeAxisChartData[] = [];

//     applicationHistogramData.forEach((item) => {
//       const {
//         tcpServerNetworkLatencyAvg = 0,
//         tcpClientNetworkLatencyAvg = 0,
//         serverResponseLatencyAvg = 0,

//         tcpClientRetransmissionPackets = 0,
//         tcpClientPackets = 0,
//         tcpServerRetransmissionPackets = 0,
//         tcpServerPackets = 0,

//         tcpClientZeroWindowPackets = 0,
//         tcpServerZeroWindowPackets = 0,

//         timestamp,
//       } = item;
//       const upRetransRate = tcpClientPackets
//         ? (tcpClientRetransmissionPackets as number) / (tcpClientPackets as number)
//         : 0;
//       const downRetransRate = tcpServerPackets
//         ? (tcpServerRetransmissionPackets as number) / (tcpServerPackets as number)
//         : 0;
//       const time = new Date(timestamp).getTime();
//       responseTimeData.push({
//         timestamp: time,
//         tcpServerNetworkLatencyAvg:
//           parseFloat((tcpServerNetworkLatencyAvg as number)?.toFixed(2)) || 0,
//         tcpClientNetworkLatencyAvg:
//           parseFloat((tcpClientNetworkLatencyAvg as number)?.toFixed(2)) || 0,
//         serverResponseLatencyAvg: parseFloat((serverResponseLatencyAvg as number)?.toFixed(2)) || 0,
//       });

//       retransmissionData.push({
//         timestamp: time,
//         upRetransRate: upRetransRate || 0,
//         downRetransRate: downRetransRate || 0,
//       });

//       zeroWindowData.push({
//         timestamp: time,
//         zeroWindowUp: (tcpClientZeroWindowPackets as number) / selectTimeInfo.interval || 0,
//         zeroWindowDown: (tcpServerZeroWindowPackets as number) / selectTimeInfo.interval || 0,
//       });
//     });

//     return {
//       responseTimeData,
//       retransmissionData,
//       zeroWindowData,
//     };
//   }, [applicationHistogramData, selectTimeInfo.interval]);

//   const handleNetworkChange = useCallback((id?: string) => {
//     setNetworkId(id);
//   }, []);

//   const topologyTime = useMemo(() => {
//     return timeToNumber(selectTimeInfo);
//   }, [selectTimeInfo]);

//   return (
//     <FullScreenCard
//       title={'网络运行态势'}
//       extra={
//         <>
//           {/* <span >{networkId}</span> */}
//           <TimeAutoRefreshSelectBox />
//         </>
//       }
//     >
//       <HightContext.Consumer>
//         {(isFullscreen) => {
//           return (
//             <div className={styles.wrap} style={{ height: '100%' }}>
//               <Row gutter={10} style={{ height: '100%' }}>
//                 <Col span={6} style={{ height: '100%' }}>
//                   <Card
//                     size="small"
//                     loading={totalFlowLoading}
//                     title="总流量"
//                     style={{ height: isFullscreen ? '30%' : 'auto' }}
//                     className={styles.totalFlow}
//                     bodyStyle={{
//                       display: 'flex',
//                       flexDirection: 'column',
//                       justifyContent: 'center',
//                       padding: 10,
//                       height: isFullscreen ? 'calc(100% - 40px)' : 160,
//                     }}
//                   >
//                     <p className={styles['totalFlow-title']}>
//                       {bytesToSize(totalPayload.totalBytes)}
//                     </p>
//                     <div className={styles['totalFlow-row']}>
//                       <span className={styles.item}>
//                         上行流量{' '}
//                         <span className={styles.progressWrap}>
//                           <Progress
//                             percent={(totalPayload.upstreamBytes / totalPayload.totalBytes) * 100}
//                             showInfo={false}
//                           />
//                         </span>
//                       </span>
//                       <span className={styles.item}>{bytesToSize(totalPayload.upstreamBytes)}</span>
//                     </div>
//                     <div className={styles['totalFlow-row']}>
//                       <span className={styles.item}>
//                         下行流量{' '}
//                         <span className={styles.progressWrap}>
//                           <Progress
//                             percent={(totalPayload.downstreamBytes / totalPayload.totalBytes) * 100}
//                             showInfo={false}
//                           />
//                         </span>
//                       </span>
//                       <span className={styles.item}>
//                         {bytesToSize(totalPayload.downstreamBytes)}
//                       </span>
//                     </div>
//                   </Card>
//                   <Card
//                     size="small"
//                     loading={flowTopLoading}
//                     style={{ height: isFullscreen ? '35%' : 'auto' }}
//                     title="IP总流量TOP10"
//                   >
//                     <BarChart
//                       data={ipTopData}
//                       showMetrics={[
//                         { title: '发送', value: 'upstreamBytes' },
//                         { title: '接收', value: 'downstreamBytes' },
//                       ]}
//                       category={['ipAddress']}
//                       unitConverter={bytesToSize}
//                       height={280}
//                     />
//                   </Card>
//                   <Card
//                     size="small"
//                     loading={conversitionTopLoading}
//                     style={{ height: isFullscreen ? '35%' : 'auto' }}
//                     title="IP通讯对总流量TOP10"
//                   >
//                     <BarChart
//                       data={ipConversationTopData}
//                       showMetrics={[
//                         { title: '上行', value: 'upstreamBytes' },
//                         { title: '下行', value: 'downstreamBytes' },
//                       ]}
//                       category={['ipAAddress', 'ipBAddress']}
//                       unitConverter={bytesToSize}
//                       tooltipFormater={(params: any) => {
//                         const [ipAAddress, ipBAddress] = params[0]?.axisValue?.split('-');
//                         let label = `${params.lastItem.axisValue}<br/>`;
//                         for (let i = 0; i < params.length; i += 1) {
//                           // label += `${params[i].marker}${
//                           label += `${
//                             params[i].seriesName === '上行'
//                               ? `${ipAAddress} -> ${ipBAddress}`
//                               : `${ipBAddress} -> ${ipAAddress}`
//                           }: ${bytesToSize(params[i].value)}<br/>`;
//                         }
//                         return label;
//                       }}
//                       height={280}
//                     />
//                   </Card>
//                 </Col>
//                 <Col span={12} style={{ height: '100%' }}>
//                   <Card
//                     style={{ height: isFullscreen ? '30%' : 'auto' }}
//                     size="small"
//                     title="流量趋势图"
//                     loading={totalFlowLoading}
//                     bodyStyle={{ padding: 10, height: isFullscreen ? 'calc(100% - 40px)' : 180 }}
//                   >
//                     <FlowChat data={computedFlowData.flowSeriesData} style={{ height: '100%' }} />
//                   </Card>
//                   <Card size="small" bodyStyle={{ padding: 0, height: 600 }}>
//                     <Topology timeInfo={topologyTime} onEdgeClick={handleNetworkChange} />
//                   </Card>
//                 </Col>
//                 <Col span={6} style={{ height: '100%' }}>
//                   <Card
//                     size="small"
//                     loading={appFlowTopLoading}
//                     title="应用总流量TOP10"
//                     style={{ height: isFullscreen ? '25%' : 'auto' }}
//                     bodyStyle={{ padding: 10, height: isFullscreen ? 'calc(100% - 40px)' : 270 }}
//                   >
//                     {applicationTopList.length === 0 && <Message />}
//                     <Row gutter={10}>
//                       {applicationTopList.map((item) => {
//                         // 计算流量占比情况
//                         const flowRate =
//                           totalPayload.totalBytes > 0
//                             ? (
//                                 ((item.totalBytes as number) / totalPayload.totalBytes) *
//                                 100
//                               ).toFixed(2)
//                             : 0;
//                         return (
//                           <Col span={12} key={item.applicationId}>
//                             <div
//                               className={classnames({
//                                 [styles.app]: true,
//                                 [styles.selected]: applicationId === item.applicationId,
//                               })}
//                               onClick={() => setApplicationId(String(item.applicationId))}
//                             >
//                               <div className={styles['app-title']}>
//                                 {/* 应用名称 */}
//                                 <span>
//                                   {allApplicationMap[item.applicationId]?.nameText ||
//                                     item.applicationId}
//                                 </span>
//                                 {/* 流量占比 */}
//                                 <span>{flowRate}%</span>
//                               </div>
//                               {/* 当前展示这个应用时，展示应用的速率情况和流量占比进度条 */}
//                               <div className={styles['app-stats']}>
//                                 <Progress status="success" percent={+flowRate} showInfo={false} />
//                               </div>
//                             </div>
//                           </Col>
//                         );
//                       })}
//                     </Row>
//                   </Card>
//                   <Card
//                     size="small"
//                     loading={histogramLoading}
//                     title="响应时间"
//                     style={{ height: isFullscreen ? '25%' : 'auto' }}
//                     bodyStyle={{ height: isFullscreen ? 'calc(100% - 40px)' : 150, padding: 0 }}
//                   >
//                     <TimeAxisChart
//                       data={trendChartData.responseTimeData}
//                       brush={false}
//                       {...timeToNumber(selectTimeInfo)}
//                       nameMap={nameMap}
//                       unitConverter={(value: number) => `${value || 0}ms`}
//                     />
//                   </Card>
//                   <Card
//                     size="small"
//                     loading={histogramLoading}
//                     title="重传%"
//                     style={{ height: isFullscreen ? '25%' : 'auto' }}
//                     bodyStyle={{ height: isFullscreen ? 'calc(100% - 40px)' : 150, padding: 0 }}
//                   >
//                     <TimeAxisChart
//                       data={trendChartData.retransmissionData}
//                       brush={false}
//                       {...timeToNumber(selectTimeInfo)}
//                       nameMap={nameMap}
//                       unitConverter={(value: number) => {
//                         return numeral(value).format('0%') || '0';
//                       }}
//                     />
//                   </Card>
//                   <Card
//                     size="small"
//                     loading={histogramLoading}
//                     title="零窗口/秒"
//                     style={{ height: isFullscreen ? '25%' : 'auto' }}
//                     bodyStyle={{ height: isFullscreen ? 'calc(100% - 60px)' : 150, padding: 0 }}
//                   >
//                     <TimeAxisChart
//                       data={trendChartData.zeroWindowData}
//                       brush={false}
//                       {...timeToNumber(selectTimeInfo)}
//                       nameMap={nameMap}
//                       unitConverter={(value: number) => {
//                         return `${numeral(value).format('0.0') || 0}pps`;
//                       }}
//                     />
//                   </Card>
//                 </Col>
//               </Row>
//             </div>
//           );
//         }}
//       </HightContext.Consumer>
//     </FullScreenCard>
//   );
// };

// export default connect(
//   ({
//     situationModel,
//     SAKnowledgeModel: { allApplicationMap },
//     loading: { effects },
//   }: ConnectState) => ({
//     situationModel,
//     allApplicationMap,
//     totalFlowLoading: effects['situationModel/queryNetworkPayloadHistogram'],
//     flowTopLoading: effects['situationModel/queryIpTop'],
//     conversitionTopLoading: effects['situationModel/queryIpConversationTop'],
//     histogramLoading: effects['situationModel/queryApplicationDetailHistogram'],
//     appFlowTopLoading: effects['situationModel/queryApplicationFlowTableData'],
//   }),
// )(NetworkSituation);
