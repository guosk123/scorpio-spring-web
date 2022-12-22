// import { IPacketResponse } from '@/pages/app/appliance/Packet-next/typings';
// import { mock, Random } from 'mockjs';
// import moment from 'moment';

// // 默认的最近30分钟，所以开始时间，可以设置成 前28分钟
// const getCurrentDate = () => moment().subtract('28', 'm');

// const mockListData = () => {
//   const packetsList: IPacketResponse = {
//     code: 0,
//     result: [],
//   };
//   for (let i = 0; i < 100; i += 1) {
//     // 先先
//     const timestamp = moment(getCurrentDate()).add(i, 's').format();
//     packetsList.result.push({
//       timestamp,
//       networkId: 'mit2nHkBE-zCkGOLNr8L',
//       serviceId: 'pit7nHkBE-zCkGOLXL8h',
//       ipInitiator: Random.ip(),
//       portInitiator: mock({
//         'port|1-65535': 100,
//       }).port,
//       ipResponder: Random.ip(),
//       portResponder: mock({
//         'port|1-65535': 100,
//       }).port,
//       ipProtocol: mock({
//         'ipProtocol|1': ['TCP', 'UDP', 'ICMP', 'SCTP', 'OTHER'],
//       }).ipProtocol,
//       tcpFlags: 'SYN,ACK',
//       vlanId: mock({
//         'vlanId|1-4094': 100,
//       }).vlanId,
//       totalBytes: mock({
//         'totalBytes|1000-9999': 100,
//       }).totalBytes,
//       applicationId:
//         mock({
//           'applicationId|1-1000': 100,
//         }).applicationId + '',
//       l7ProtocolId: 'l7ProtocolId',
//       ethernetType: mock({
//         'ethernetType|0-8': 1,
//       }).ethernetType,
//       ethernetInitiator: 'ethernetInitiator',
//       ethernetResponder: 'ethernetResponder',
//       countryIdInitiator: mock({
//         'countryId|1-20': 10,
//       }).countryId,
//       provinceIdInitiator: mock({
//         'provinceId|1-20': 10,
//       }).provinceId,
//       cityIdInitiator: mock({
//         'cityId|1-20': 10,
//       }).cityId,
//       countryIdResponder: mock({
//         'countryId|1-20': 10,
//       }).countryId,
//       provinceIdResponder: mock({
//         'provinceId|1-20': 10,
//       }).provinceId,
//       cityIdResponder: mock({
//         'cityId|1-20': 10,
//       }).cityId,
//     });
//   }

//   return packetsList;
// };

// const mockRefineData = () => ({
//   code: 0,
//   result: {
//     status: '',
//     message: '',
//     execution: {
//       searchBytes: 123,
//       searchPacketCount: 2323,
//       searchFlowCount: 23434,
//       matchMinTimestamp: moment(getCurrentDate()).valueOf(),
//       matchMaxTimestamp: moment(getCurrentDate()).add(20, 'm').valueOf(),
//     },
//     aggregations: [
//       {
//         label: '1.1.1.2:port',
//         labelType: 'port',
//         items: [
//           {
//             label: 'tcp:1233',
//             value: '2342111',
//             keys: [
//               { itemType: 'port', itemValue: '1233' },
//               { itemType: 'ipProtocol', itemValue: 'tcp' },
//             ],
//           },
//           {
//             label: 'tcp:1234',
//             value: '2342322',
//             keys: [
//               { itemType: 'port', itemValue: '1234' },
//               { itemType: 'ipProtocol', itemValue: 'tcp' },
//             ],
//           },
//           {
//             label: 'udp:1235',
//             value: '23421122',
//             keys: [
//               { itemType: 'port', itemValue: '1235' },
//               { itemType: 'ipProtocol', itemValue: 'udp' },
//             ],
//           },
//           {
//             label: 'udp:1236',
//             value: '234234222',
//             keys: [
//               { itemType: 'port', itemValue: '1236' },
//               { itemType: 'ipProtocol', itemValue: 'udp' },
//             ],
//           },
//           {
//             label: 'udp:1237',
//             value: '234232242',
//             keys: [
//               { itemType: 'port', itemValue: '1237' },
//               { itemType: 'ipProtocol', itemValue: 'udp' },
//             ],
//           },
//           {
//             label: 'tcp:1238',
//             value: '2342112342',
//             keys: [
//               { itemType: 'port', itemValue: '1238' },
//               { itemType: 'ipProtocol', itemValue: 'tcp' },
//             ],
//           },
//         ],
//         total: 3242222,
//       },
//       {
//         label: 'IPv5',
//         labelType: 'ipAddress',
//         items: [
//           {
//             label: '2.1.1.1',
//             value: '2342223342',
//             keys: [{ itemType: 'ipAddress', itemValue: '2.1.1.1' }],
//           },
//           {
//             label: '3.1.1.1',
//             value: '23324442',
//             keys: [{ itemType: 'ipAddress', itemValue: '3.1.1.1' }],
//           },
//           {
//             label: '4.1.1.1',
//             value: '23423342',
//             keys: [{ itemType: 'ipAddress', itemValue: '4.1.1.1' }],
//           },
//           {
//             label: '5.1.1.1',
//             value: '232345342',
//             keys: [{ itemType: 'ipAddress', itemValue: '5.1.1.1' }],
//           },
//           {
//             label: '6.1.1.1',
//             value: '23454232242',
//             keys: [{ itemType: 'ipAddress', itemValue: '6.1.1.1' }],
//           },
//           {
//             label: '7.1.1.1',
//             value: '231231342',
//             keys: [{ itemType: 'ipAddress', itemValue: '7.1.1.1' }],
//           },
//         ],
//         total: 323333,
//       },
//       {
//         label: 'Port',
//         items: [
//           {
//             label: 'tcp:3232',
//             value: '2332422',
//             keys: [
//               { itemType: 'port', itemValue: '3232' },
//               { itemType: 'ipProtocol', itemValue: 'tcp' },
//             ],
//           },
//           {
//             label: 'udp:2424',
//             value: '2342342342',
//             keys: [
//               { itemType: 'port', itemValue: '2424' },
//               { itemType: 'ipProtocol', itemValue: 'udp' },
//             ],
//           },
//         ],
//         total: 3244444,
//       },
//       {
//         label: 'Port1',
//         items: [
//           { label: '1.2.2.1', value: '2332422' },
//           { label: '1.3.3.1', value: '2342342342' },
//           { label: '1.4.4.1', value: '23234234342' },
//           { label: '1.5.5.1', value: '23123123342' },
//           { label: '1.6.6.1', value: '233453642' },
//           { label: '1.7.7.1', value: '236523342' },
//         ],
//         total: 3245555,
//       },
//     ],
//   },
// });

// const mockRefineData2 = () => ({
//   code: 0,
//   result: {
//     status: '',
//     message: '',
//     execution: {
//       searchBytes: 123,
//       searchPacketCount: 2323,
//       searchFlowCount: 23434,
//       matchMinTimestamp: moment(getCurrentDate()).valueOf(),
//       matchMaxTimestamp: moment(getCurrentDate()).add(20, 'm').valueOf(),
//     },
//     aggregations: [
//       {
//         label: '1.1.1.2:port',
//         type: 'port',
//         items: [
//           {
//             label: 'tcp:1233',
//             value: '2342111',
//             keys: {
//               port: 1233,
//               ipProtocol: 'tcp',
//             },
//           },
//           {
//             label: 'tcp:1234',
//             value: '2342322',
//             keys: {
//               port: 1234,
//               ipProtocol: 'TCP',
//             },
//           },
//           {
//             label: 'udp:1235',
//             value: '23421122',
//             keys: { port: 1235, ipProtocol: 'UDP' },
//           },
//           {
//             label: 'udp:1236',
//             value: '234234222',
//             keys: { port: '1236', ipProtocol: 'UDP' },
//           },
//           {
//             label: 'udp:1237',
//             value: '234232242',
//             keys: { port: '1237', ipProtocol: 'UDP' },
//           },
//           {
//             label: 'tcp:1238',
//             value: '2342112342',
//             keys: { port: '1238', ipProtocol: 'TCP' },
//           },
//         ],
//         total: 3242222,
//       },
//       {
//         label: 'IPv5',
//         type: 'ipAddress',
//         items: [
//           {
//             label: '2.1.1.1',
//             value: '2342223342',
//             keys: { ipAddress: '2.1.1.1' },
//           },
//           {
//             label: '3.1.1.1',
//             value: '23324442',
//             keys: { ipAddress: '3.1.1.1' },
//           },
//           {
//             label: '4.1.1.1',
//             value: '23423342',
//             keys: { ipAddress: '4.1.1.1' },
//           },
//           {
//             label: '5.1.1.1',
//             value: '232345342',
//             keys: { ipAddress: '5.1.1.1' },
//           },
//           {
//             label: '6.1.1.1',
//             value: '23454232242',
//             keys: { ipAddress: '6.1.1.1' },
//           },
//           {
//             label: '7.1.1.1',
//             value: '231231342',
//             keys: { ipAddress: '7.1.1.1' },
//           },
//         ],
//         total: 323333,
//       },
//       {
//         label: 'Port',
//         items: [
//           {
//             label: 'tcp:3232',
//             value: '2332422',
//             keys: { port: '3232', ipProtocol: 'TCP' },
//           },
//           {
//             label: 'udp:2424',
//             value: '2342342342',
//             keys: { port: '2424', ipProtocol: 'TCP' },
//           },
//         ],
//         total: 3244444,
//       },
//       {
//         label: 'Port1',
//         items: [
//           { label: '1.2.2.1', value: '2332422' },
//           { label: '1.3.3.1', value: '2342342342' },
//           { label: '1.4.4.1', value: '23234234342' },
//           { label: '1.5.5.1', value: '23123123342' },
//           { label: '1.6.6.1', value: '233453642' },
//           { label: '1.7.7.1', value: '236523342' },
//         ],
//         total: 3245555,
//       },
//     ],
//   },
// });

// export default {
//   '/api/showList': mockListData(),
//   '/api/showTree': mockRefineData(),
//   '/api/webapi/fpc-v1/appliance/packets': mockListData(),
//   '/api/webapi/fpc-v1/appliance/packets/as-statistics': mockRefineData2(),
// };
