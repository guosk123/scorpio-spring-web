// import { Request, Response } from 'express';
// import { Random } from 'mockjs';
// import moment from 'moment';

// const regionStatFields = [
//   'timestamp',
//   'networkId',
//   'serviceId',
//   'totalBytes',
//   'downstreamBytes',
//   'upstreamBytes',
//   'totalPackets',
//   'downstreamPackets',
//   'upstreamPackets',
//   'establishedSessions',
//   'tcpClientNetworkLatency',
//   'tcpClientNetworkLatencyCounts',
//   'tcpServerNetworkLatencyCounts',
//   'serverResponseLatency',
//   'serverResponseLatencyCounts',
//   'tcpClientZeroWindowPackets',
//   'tcpEstablishedSuccessCounts',
//   'tcpEstablishedFailCounts',
//   'tcpClientRetransmissionPackets',
//   'tcpClientPackets',
//   'tcpServerRetransmissionPackets',
//   'tcpServerPackets',
//   'countryId',
//   'provinceId',
//   'cityId',
//   'totalPayloadBytes',
//   'downstreamPayloadBytes',
//   'upstreamPayloadBytes',
//   'totalPayloadPackets',
//   'downstreamPayloadPackets',
//   'upstreamPayloadPackets',
//   'tcpSynPackets',
//   'tcpSynAckPackets',
//   'tcpSynRstPackets',
//   'tcpServerNetworkLatency',
//   'tcpServerZeroWindowPackets',
// ];

// const numberField = ['bytes', 'packets', 'latency', 'port', 'counts', 'sessions'];

// export function fieldTypeToJSType(fieldTypeName: string) {
//   const tmpName = fieldTypeName.toLowerCase();
//   const isNumber =
//     numberField.filter((item) => {
//       return fieldTypeName.toLowerCase().includes(item);
//     }).length > 0;
//   if (isNumber) {
//     return Random.integer(0, 1000);
//   } else if (tmpName === 'networkid') {
//     return 'mWjkRXkBlGvi2k_JwDgv';
//   } else if (tmpName.includes('time')) {
//     return moment(new Date()).format();
//   } else if (tmpName.includes('id')) {
//     return Random.integer(1, 30);
//   } else if (tmpName.includes('ip')) {
//     return Random.ip();
//   } else if (tmpName.includes('name')) {
//     return Random.name();
//   } else {
//     return Random.string();
//   }
// }

// const tableData = [];
// const rowObj = regionStatFields.reduce((acc, curr) => {
//   acc[curr] = '';
//   return acc;
// }, {});

// for (let i = 0; i < 10; i++) {
//   const tmp = Object.assign({}, rowObj);
//   regionStatFields.forEach((field) => {
//     tmp[field] = fieldTypeToJSType(field);
//   });
//   tableData.push(tmp);
// }

// let date = new Date().getTime();
// const chartData = [];
// for (let i = 0; i < 60; i++) {
//   date += 5000;
//   for (let j = 0; j < 10; j++) {
//     chartData.push({
//       totalBytes: Random.integer(105, 1220520),
//       timestamp: date,
//       countryId: j + 1,
//     });
//   }
// }

// const flowLocationData = {
//   table: tableData,
//   histogram: chartData,
// };

// const getFlowLocationTableData = (req: Request, res: Response) => {
//   return res.json(flowLocationData.table);
// };

// const getFlowLocationHistogramData = (req: Request, res: Response) => {
//   return res.json(flowLocationData.histogram);
// };

// export default {
//   'GET /api/webapi/fpc-v1/metric/locations': getFlowLocationTableData,
//   'GET /api/webapi/fpc-v1/metric/local/locations/as-histogram': getFlowLocationHistogramData,
// };
