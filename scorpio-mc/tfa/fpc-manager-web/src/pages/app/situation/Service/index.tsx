// import { EMetricApiType } from '@/common/api/analysis';
// import { ANALYSIS_APPLICATION_TYPE_ENUM } from '@/common/app';
// import TimeAxisChart from '@/components/TimeAxisChart';
// import type { TimeAxisChartData } from '@/components/TimeAxisChart/typing';
// import type { ConnectState } from '@/models/connect';
// import { bytesToSize, convertBandwidth, timeFormatter } from '@/utils/utils';
// import { Card, Col, Descriptions, Input, InputNumber, Radio, Row, Select, Tabs } from 'antd';
// import { snakeCase } from 'lodash';
// import numeral from 'numeral';
// import { useEffect, useMemo, useState } from 'react';
// import type { Dispatch, ISituationModelState } from 'umi';
// import { connect } from 'umi';
// import { queryServiceStatList } from '../../analysis/service';
// import ApplicationTopology from '../../analysis/Service/ApplicationTopology';
// import { fieldList } from '../../analysis/Service/List/components/FieldDisplaySetting';
// import type { IFlowAnalysisData, IFlowQueryParams, IServiceStatData } from '../../analysis/typings';
// import { ESortDirection, ESourceType } from '../../analysis/typings';
// import type { IApplicationMap } from '../../configuration/SAKnowledge/typings';
// import type { IService } from '../../configuration/Service/typings';
// import BarChart from '../components/BarChart';
// import FullScreenCard, { HightContext } from '../components/FullScreenCard';
// import ServiceApplicationStat from '../components/ServiceApplication';
// import TimeAutoRefreshSelectBox from '../components/TimeAutoRefreshSelectBox';
// import type { ITimeInfo } from '../Network';
// import type { IServiceHistogramParams } from '../service';
// import { timeToNumber } from '../utils/timeTools';
// import styles from './index.less';

// const nameMap = {
//   connectRate: '连接成功率',
//   tcpServerNetworkLatencyAvg: '服务器网络平均时延',
//   tcpClientNetworkLatencyAvg: '客户端网络平均时延',
//   serverResponseLatencyAvg: '服务器网络平均响应时延',
//   upThroughout: '吞吐量（上行）',
//   downThroughout: '吞吐量（下行）',
//   totalThroughout: '吞吐量',
// };

// const canBeSortedMetrics = fieldList.filter(
//   (metric) =>
//     metric.key !== 'alertCounts' &&
//     metric.key !== 'networkBandwidthAvg' &&
//     metric.key !== 'tcpEstablishedSuccessRate' &&
//     metric.key !== 'bytepsAvg',
// );

// export type SessionData = {
//   tcpEstablishedFailCounts: number;
//   tcpEstablishedSuccessCounts: number;
//   tcpEstablishedFailRate: number;
//   applicationId: string;
// };

// interface IServiceSituationProps {
//   dispatch: Dispatch;
//   situationModel: ISituationModelState;

//   allApplicationMap: IApplicationMap;
//   allServiceMap: Record<string, IService>;
//   queryServiceHistogramLoading: boolean | undefined;
//   queryServiceListLoading: boolean | undefined;
//   queryFlowLoading: boolean | undefined;
//   queryLatencyLoading: boolean | undefined;
//   querySessionLoading: boolean | undefined;
//   serviceLinkLoading: boolean | undefined;
// }

// const ServiceSituation = ({
//   dispatch,
//   allServiceMap,
//   situationModel: { serviceStatData, startTime, endTime },
// }: // queryServiceListLoading,
// // queryServiceHistogramLoading,
// // queryFlowLoading,
// // queryLatencyLoading,
// // querySessionLoading,
// // serviceLinkLoading,
// IServiceSituationProps) => {
//   // 当前查看统计的业务ID
//   const [serviceId, setServiceId] = useState<string>();
//   // 业务归属归属的网络ID
//   const [networkId, setNetworkId] = useState<string>();

//   const [flowTopData, setFlowTopData] = useState<IFlowAnalysisData[]>([]);
//   const [latenyData, setLatencyData] = useState<IFlowAnalysisData[]>([]);

//   const [sessionData, setSessionData] = useState<SessionData[]>([]);
//   const [flowHistogramData, setFlowHistogramData] = useState<IFlowAnalysisData[]>([]);
//   const [latencySortDirection, setLatencySortDirection] = useState<ESortDirection>(
//     ESortDirection.ASC,
//   );

//   const [allApplicationMap, setAllApplicationMap] = useState();

//   useEffect(() => {
//     dispatch({
//       type: 'serviceModel/queryAllServices',
//     });
//     dispatch({ type: 'SAKnowledgeModel/queryAllApplications' }).then((res: any) => {
//       setAllApplicationMap(res.allApplicationMap);
//     });
//   }, [dispatch]);

//   const [topN, setTopN] = useState(10);
//   const [sortedBy, setSortedBy] = useState(canBeSortedMetrics[0].key);

//   const [services, setServices] = useState<IServiceStatData[]>([]);

//   const selectTimeInfo = useMemo<ITimeInfo>(() => {
//     const formatter = timeFormatter(startTime, endTime);
//     return {
//       startTime: formatter.startTime,
//       endTime: formatter.endTime,
//       interval: formatter.interval,
//     };
//   }, [endTime, startTime]);

//   // 先获取所有的业务列表
//   useEffect(() => {
//     queryServiceStatList({
//       sortProperty: snakeCase(sortedBy),
//       pageSize: topN,
//       ...selectTimeInfo,
//       isFlow: '0',
//     }).then(({ success, result }) => {
//       if (success) {
//         setServices(result.content);
//       }
//     });
//   }, [dispatch, selectTimeInfo, sortedBy, topN]);

//   // 拆分业务的网络
//   const serviceFlattenData = useMemo(() => {
//     const flattenData: { id: string; name: string; networkId: string; networkName: string }[] = [];

//     services.forEach((service) => {
//       const serviceItem = allServiceMap[service.serviceId];
//       const netList = serviceItem?.networkIds?.split(',') || [];
//       const netNameList = serviceItem?.networkNames?.split(',') || [];

//       flattenData.push({
//         id: service.serviceId,
//         name: allServiceMap[service.serviceId]?.name || service.serviceId,
//         networkId: service.networkId,
//         networkName:
//           netNameList[netList.findIndex((id) => id === service.networkId)] || service.networkId,
//       });
//     });

//     return flattenData;
//   }, [allServiceMap, services]);

//   useEffect(() => {
//     if (!serviceId && !networkId && serviceFlattenData.length > 0) {
//       setServiceId(serviceFlattenData[0].id);
//       setNetworkId(serviceFlattenData[0].networkId);
//     }
//   }, [serviceId, networkId, serviceFlattenData]);

//   const baseQueryParams = useMemo(() => {
//     return {
//       sourceType: ESourceType.SERVICE,
//       networkId,
//       serviceId,
//       metricApi: EMetricApiType.application,
//       type: 2,
//       drilldown: '0' as const,
//     };
//   }, [networkId, serviceId]);

//   const durationOneDayPayload = useMemo<IFlowQueryParams>(() => {
//     const dsl = `(network_id=${networkId} and service_id=${serviceId})| gentimes timestamp start="${selectTimeInfo.startTime}" end="${selectTimeInfo.endTime}"`;
//     return {
//       ...baseQueryParams,
//       ...selectTimeInfo,
//       dsl,
//     };
//   }, [baseQueryParams, networkId, selectTimeInfo, serviceId]);

//   // 应用体验
//   useEffect(() => {
//     if (networkId && serviceId) {
//       dispatch({
//         type: 'situationModel/queryServiceStat',
//         payload: {
//           ...selectTimeInfo,
//           networkId,
//           serviceId,
//         },
//       });
//     }
//   }, [dispatch, endTime, networkId, selectTimeInfo, serviceId, startTime]);

//   // 应用日流量TOP
//   useEffect(() => {
//     if (networkId && serviceId) {
//       dispatch({
//         type: 'situationModel/queryApplicationFlowTableData',
//         payload: {
//           ...durationOneDayPayload,
//           sortProperty: snakeCase('totalBytes'),
//           sortDirection: ESortDirection.DESC,
//           dsl: `(type = ${ANALYSIS_APPLICATION_TYPE_ENUM.应用}) and ${durationOneDayPayload.dsl}`,
//         },
//       }).then((flowTop: IFlowAnalysisData[]) => {
//         setFlowTopData(flowTop.slice(0, 10));
//       });
//     }
//   }, [dispatch, durationOneDayPayload, networkId, serviceId]);

//   // 应用服务器响应平均时延TOP10
//   useEffect(() => {
//     if (networkId && serviceId) {
//       // 平均响应时间数据
//       dispatch({
//         type: 'situationModel/queryApplicationFlowTableData',
//         payload: {
//           ...durationOneDayPayload,
//           sortProperty: snakeCase('serverResponseLatencyAvg'),
//           sortDirection: latencySortDirection,
//           dsl: `(type = ${ANALYSIS_APPLICATION_TYPE_ENUM.应用}) and ${durationOneDayPayload.dsl}`,
//         },
//       }).then((latencyData: IFlowAnalysisData[]) => {
//         setLatencyData(latencyData.slice(0, 10));
//       });
//     }
//   }, [dispatch, durationOneDayPayload, latencySortDirection, networkId, serviceId]);

//   // 应用会话失败率top
//   useEffect(() => {
//     if (networkId && serviceId) {
//       dispatch({
//         type: 'situationModel/queryApplicationSession',
//         payload: {
//           ...selectTimeInfo,
//           networkId,
//           serviceId,
//           count: 10,
//           sortProperty: 'tcp_established_fail_rate',
//           sortDirection: 'desc',
//         },
//       }).then((data: SessionData[]) => {
//         setSessionData(data);
//       });
//     }
//   }, [dispatch, endTime, networkId, selectTimeInfo, serviceId, startTime]);

//   // 所有图表数据
//   useEffect(() => {
//     if (serviceId && networkId) {
//       // 业务趋势数据
//       const serviceHistogramPayload: IServiceHistogramParams = {
//         ...selectTimeInfo,
//         networkId,
//         serviceId,
//       };
//       dispatch({
//         type: 'situationModel/queryServiceHistogram',
//         payload: serviceHistogramPayload,
//       }).then((latencyData: IFlowAnalysisData[]) => {
//         setFlowHistogramData(latencyData);
//       });
//       // 会话失败率top
//     }
//   }, [dispatch, endTime, networkId, selectTimeInfo, serviceId, startTime]);

//   const sessionFailData = useMemo(() => {
//     return sessionData.map((item) => {
//       return {
//         ...item,
//         tcpEstablishedFailRate: parseFloat((item.tcpEstablishedFailRate * 100).toFixed(2)),
//         sessionTotal: item.tcpEstablishedFailCounts + item.tcpEstablishedSuccessCounts,
//       };
//     });
//   }, [sessionData]);

//   const trendChartData = useMemo(() => {
//     const connectRateData: TimeAxisChartData[] = [];
//     const responseTrendData: TimeAxisChartData[] = [];
//     const throughoutData: TimeAxisChartData[] = [];

//     flowHistogramData.forEach((item) => {
//       const {
//         tcpEstablishedSuccessCounts,
//         tcpEstablishedFailCounts,
//         tcpServerNetworkLatencyAvg,
//         tcpClientNetworkLatencyAvg,
//         serverResponseLatencyAvg,
//         upstreamBytes,
//         downstreamBytes,
//         totalBytes,
//         timestamp,
//       } = item;

//       const time = new Date(timestamp).getTime();
//       const total = (tcpEstablishedFailCounts as number) + (tcpEstablishedSuccessCounts as number);
//       const successRate: number = total
//         ? ((tcpEstablishedSuccessCounts as number) || 0) / total
//         : 1;
//       connectRateData.push({ timestamp: time, connectRate: successRate });

//       responseTrendData.push({
//         timestamp: time,
//         tcpServerNetworkLatencyAvg: (tcpServerNetworkLatencyAvg as number) || 0,
//         tcpClientNetworkLatencyAvg: (tcpClientNetworkLatencyAvg as number) || 0,
//         serverResponseLatencyAvg: (serverResponseLatencyAvg as number) || 0,
//       });

//       throughoutData.push({
//         timestamp: time,
//         upThroughout: ((upstreamBytes as number) * 8) / selectTimeInfo.interval || 0,
//         downThroughout: ((downstreamBytes as number) * 8) / selectTimeInfo.interval || 0,
//         totalThroughout: ((totalBytes as number) * 8) / selectTimeInfo.interval || 0,
//       });
//     });
//     return {
//       connectRateData,
//       responseTrendData,
//       throughoutData,
//     };
//   }, [flowHistogramData, selectTimeInfo.interval]);

//   // 时延 数值越低， 效果越好
//   const handleBastClick = () => {
//     setLatencySortDirection(ESortDirection.ASC);
//   };
//   const handleBadClick = () => {
//     setLatencySortDirection(ESortDirection.DESC);
//   };

//   const flowTopElement = useMemo(() => {
//     return (
//       <BarChart
//         data={flowTopData}
//         showMetrics={[{ title: '总流量', value: 'totalBytes' }]}
//         category={['applicationId']}
//         categoryMap={allApplicationMap}
//         height={200}
//         unitConverter={bytesToSize}
//       />
//     );
//   }, [allApplicationMap, flowTopData]);

//   const sessionFailElement = useMemo(() => {
//     return (
//       <BarChart
//         data={sessionFailData}
//         showMetrics={[
//           { title: '连接失败数', value: 'tcpEstablishedFailCounts' },
//           {
//             title: '连接成功数',
//             value: 'tcpEstablishedSuccessCounts',
//           },
//           // {
//           //   title: '连接失败率',
//           //   value: 'tcpEstablishedFailRate',
//           //   type: 'line',
//           // },
//         ]}
//         category={['applicationId']}
//         categoryMap={allApplicationMap}
//         height={200}
//       />
//     );
//   }, [allApplicationMap, sessionFailData]);

//   const avgLatencyElement = useMemo(() => {
//     return (
//       <BarChart
//         data={latenyData}
//         showMetrics={[{ title: '服务器响应平均时延', value: 'serverResponseLatencyAvg' }]}
//         category={['applicationId']}
//         categoryMap={allApplicationMap}
//         height={200}
//         unitConverter={(value: number) => `${value}ms`}
//       />
//     );
//   }, [allApplicationMap, latenyData]);

//   return (
//     <FullScreenCard
//       title={'业务运行态势'}
//       extra={
//         <>
//           <Descriptions style={{ width: 400 }}>
//             <Descriptions.Item label="指标" style={{ paddingBottom: 0 }}>
//               <Input.Group compact size={'small'}>
//                 <Select
//                   size={'small'}
//                   style={{ width: 220 }}
//                   placeholder="请选择指标"
//                   onChange={(value) => {
//                     setSortedBy(value);
//                   }}
//                   value={sortedBy}
//                 >
//                   {canBeSortedMetrics.map((metric) => {
//                     return (
//                       <Select.Option key={metric.key} value={metric.key}>
//                         {metric.label}
//                       </Select.Option>
//                     );
//                   })}
//                 </Select>
//                 <InputNumber
//                   size={'small'}
//                   placeholder="topN"
//                   min={1}
//                   max={50}
//                   value={topN}
//                   onChange={(value) => setTopN(value)}
//                 />
//               </Input.Group>
//             </Descriptions.Item>
//           </Descriptions>
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
//                     title="应用体验"
//                     style={{
//                       height: isFullscreen ? '19%' : 'auto',
//                     }}
//                     bodyStyle={{
//                       display: 'flex',
//                       flexDirection: 'column',
//                       justifyContent: 'center',
//                       height: 'calc(100% - 40px)',
//                       padding: 10,
//                     }}
//                   >
//                     <Row gutter={10}>
//                       <Col span={12} className={styles.overview}>
//                         <div>{serviceStatData?.serverResponseLatencyAvg || 0}ms</div>
//                         <div>服务器响应平均时延</div>
//                       </Col>
//                       <Col span={12} className={styles.overview}>
//                         <div>{serviceStatData?.establishedSessions || 0}</div>
//                         <div>新建会话数</div>
//                       </Col>
//                     </Row>
//                   </Card>
//                   <Card
//                     size="small"
//                     title="应用流量TOP10"
//                     style={{
//                       height: isFullscreen ? '27%' : 240,
//                     }}
//                     bodyStyle={{ height: isFullscreen ? 'calc(100% - 40px)' : 200 }}
//                     // loading={queryFlowLoading}
//                   >
//                     {flowTopElement}
//                   </Card>
//                   <Card
//                     size="small"
//                     title="应用服务器响应平均时延TOP10"
//                     // loading={queryLatencyLoading}
//                     style={{
//                       height: isFullscreen ? '27%' : 'auto',
//                     }}
//                     bodyStyle={{ height: isFullscreen ? 'calc(100% - 40px)' : 200 }}
//                     extra={
//                       <Radio.Group defaultValue="a" size="small" buttonStyle="solid">
//                         <Radio.Button value="a" onClick={handleBastClick}>
//                           最佳
//                         </Radio.Button>
//                         <Radio.Button value="b" onClick={handleBadClick}>
//                           最差
//                         </Radio.Button>
//                       </Radio.Group>
//                     }
//                   >
//                     {avgLatencyElement}
//                   </Card>
//                   <Card
//                     size="small"
//                     style={{
//                       height: isFullscreen ? '27%' : 240,
//                     }}
//                     title="应用会话失败率TOP10"
//                     bodyStyle={{ height: isFullscreen ? 'calc(100% - 40px)' : 200 }}
//                     // loading={querySessionLoading}
//                   >
//                     {sessionFailElement}
//                   </Card>
//                 </Col>
//                 <Col span={12} style={{ height: '100%' }}>
//                   <Card
//                     size="small"
//                     // loading={queryServiceListLoading && serviceLinkLoading}
//                     title={
//                       <Tabs
//                         defaultActiveKey="1"
//                         activeKey={`${serviceId}.${networkId}`}
//                         onChange={(key) => {
//                           const split = key.split('.');
//                           setServiceId(split[0]);
//                           setNetworkId(split[1]);
//                         }}
//                       >
//                         {serviceFlattenData.map((service) => (
//                           <Tabs.TabPane
//                             tab={`${service.name} - ${service.networkName}`}
//                             key={`${service.id}.${service.networkId}`}
//                           />
//                         ))}
//                       </Tabs>
//                     }
//                     style={{ height: isFullscreen ? '50%' : 440 }}
//                     bodyStyle={{
//                       padding: 10,
//                       height: isFullscreen ? 'calc(100% - 40px)' : 400,
//                       overflow: 'hidden',
//                     }}
//                   >
//                     {networkId && serviceId ? (
//                       <ApplicationTopology
//                         key={`${serviceId}-${networkId}`}
//                         networkId={networkId}
//                         serviceId={serviceId}
//                         customTime={timeToNumber(selectTimeInfo)}
//                       />
//                     ) : (
//                       '系统暂时不存在业务！'
//                     )}
//                   </Card>
//                   <ServiceApplicationStat
//                     timeInfo={selectTimeInfo}
//                     networkId={networkId}
//                     serviceId={serviceId}
//                     isFullscreen={isFullscreen}
//                     style={{ height: isFullscreen ? '50%' : 460 }}
//                     bodyStyle={{
//                       height: isFullscreen ? 'calc(100% - 40px)' : 420,
//                     }}
//                   />
//                 </Col>
//                 <Col span={6} style={{ height: '100%' }}>
//                   <Card
//                     size="small"
//                     title="业务连接成功率%"
//                     // loading={queryServiceHistogramLoading}
//                     style={{
//                       height: isFullscreen ? '33%' : 300,
//                     }}
//                     bodyStyle={{ height: isFullscreen ? 'calc(100% - 40px)' : 260, padding: 0 }}
//                   >
//                     <TimeAxisChart
//                       data={trendChartData.connectRateData}
//                       brush={false}
//                       {...timeToNumber(selectTimeInfo)}
//                       nameMap={nameMap}
//                       unitConverter={(value: number) => {
//                         return numeral(value).format('0%');
//                       }}
//                     />
//                   </Card>
//                   {/* <Card
//                     size="small"
//                     title="业务访问成功率趋势%（近1小时）"
//                     loading={queryServiceHistogramLoading}
//                     bodyStyle={{ height: 180, padding: 0 }}
//                   >
//                     <TimeAxisChart
//                       data={trendChartData.connectRateData}
//                       {...selectTimeInfo}
//                       nameMap={nameMap}
//                       unitConverter={(value: number) => {
//                         return numeral(value).format('0%');
//                       }}
//                     />
//                   </Card> */}
//                   <Card
//                     size="small"
//                     title="业务响应性能趋势"
//                     // loading={queryServiceHistogramLoading}
//                     style={{
//                       height: isFullscreen ? '33%' : 300,
//                     }}
//                     bodyStyle={{ height: isFullscreen ? 'calc(100% - 40px)' : 260, padding: 0 }}
//                   >
//                     <TimeAxisChart
//                       data={trendChartData.responseTrendData}
//                       brush={false}
//                       {...timeToNumber(selectTimeInfo)}
//                       nameMap={nameMap}
//                       unitConverter={(value: number) => {
//                         return `${numeral(value).value().toString()}ms`;
//                       }}
//                     />
//                   </Card>
//                   <Card
//                     size="small"
//                     title="业务吞吐量趋势"
//                     // loading={queryServiceHistogramLoading}
//                     style={{
//                       height: isFullscreen ? '33%' : 300,
//                     }}
//                     bodyStyle={{ height: isFullscreen ? 'calc(100% - 40px)' : 260, padding: 0 }}
//                   >
//                     <TimeAxisChart
//                       data={trendChartData.throughoutData}
//                       brush={false}
//                       {...timeToNumber(selectTimeInfo)}
//                       nameMap={nameMap}
//                       unitConverter={convertBandwidth}
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
//     serviceModel: { allServiceMap },
//     loading: { effects },
//   }: ConnectState) => ({
//     situationModel,
//     allApplicationMap,
//     allServiceMap,
//     queryServiceListLoading: effects['serviceModel/queryAllServices'],
//     queryFlowLoading: effects['situationModel/queryApplicationFlowTableData'],
//     queryLatencyLoading: effects['situationModel/queryApplicationFlowTableData'],
//     querySessionLoading: effects['situationModel/queryApplicationSession'],
//     queryServiceHistogramLoading: effects['situationModel/queryServiceHistogram'],
//     serviceLinkLoading:
//       effects['npmdModel/queryNetworkFlowTableData'] &&
//       effects['serviceModel/queryServiceDetail'] &&
//       effects['serviceModel/queryServiceLink'],
//   }),
// )(ServiceSituation);
