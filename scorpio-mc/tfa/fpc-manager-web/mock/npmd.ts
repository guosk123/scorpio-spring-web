// import { Request, Response } from 'express';
// import { Random } from 'mockjs';

// const now = new Date().getTime();

// const histogram: any[] = [];

// for (let i = 0; i < 60; i++) {
//   const tmp = {
//     timestamp: now + i * 1000,
//     // 以太网帧长统计
//     tinyPackets: Random.integer(0, 64),
//     smallPackets: Random.integer(65, 127),
//     mediumPackets: Random.integer(128, 255),
//     bigPackets: Random.integer(256, 512),
//     largePackets: Random.integer(512, 1023),
//     hugePackets: Random.integer(1024, 1517),
//     jumboPackets: Random.integer(1518, 3000),

//     // IP协议包数统计
//     tcpTotalPackets: Random.integer(10, 100),
//     udpTotalPackets: Random.integer(10, 100),
//     icmpTotalPackets: Random.integer(10, 100),
//     icmp6TotalPackets: Random.integer(10, 100),
//     otherTotalPackets: Random.integer(10, 100),

//     // 以太网类型统计
//     ipv4Frames: Random.integer(10, 100),
//     ipv6Frames: Random.integer(10, 100),
//     arpFrames: Random.integer(10, 100),
//     ieee8021xFrames: Random.integer(10, 100),
//     ipxFrames: Random.integer(10, 100),
//     lacpFrames: Random.integer(10, 100),
//     mplsFrames: Random.integer(10, 100),
//     stpFrames: Random.integer(10, 100),
//     otherFrames: Random.integer(10, 100),

//     // 数据包类型统计
//     unicastBytes: Random.integer(10, 100),
//     broadcastBytes: Random.integer(10, 100),
//     multicastBytes: Random.integer(10, 100),

//     // 分片包统计
//     fragmentTotalBytes: Random.integer(10, 100),
//     fragmentTotalPackets: Random.integer(10, 100),
//   };
//   histogram.push(tmp);
// }

// const dscp: { volumn: any[], histogram: any[]} = {
//   volumn: [],
//   histogram: []
// }
// let volumn: any[] = [];
// for (let i = 0; i < 20; i++) {
//   volumn.push({
//     type: `t${i}`,
//     totalBytes: Random.integer(110, 310),
//   })
// }
// const dscpHistogram: any[] = [];
// for (let i = 0; i < 60; i++) {
//   let tempTime = now + i * 1000;
//   for(let j = 0; j < 20; j++) {
//     dscpHistogram.push({
//       timestamp: tempTime,
//       type: `t${j}`,
//       totalBytes: Random.integer(11, 16),
//     })
//   }
// }
// dscp.volumn = volumn;
// dscp.histogram = dscpHistogram;

// const getNpmdDashboard = (req: Request, res: Response) => {
//   res.json(
//     //网络秒级统计
//     {
//       networkId: 'id',

//       totalBytes: 12,
//       totalPackets: 12,
//       upstreamBytes: 12,
//       downstreamBytes: 12,
//       tcpRetransRate: 12,
//       tcpClientRetransRate: 12,
//       tcpServerRetransRate: 12,
//       alertCounts: 12,
//       tcpClientNetworkLatencyAvg: 12,
//       tcpServerNetworkLatencyAvg: 12,

//       // 以太网帧长统计
//       tinyPackets: 12,
//       smallPackets: 12,
//       mediumPackets: 12,
//       bigPackets: 12,
//       largePackets: 12,
//       hugePackets: 12,
//       jumboPackets: 12,

//       // IP协议包数统计
//       tcpTotalPackets: 12,
//       udpTotalPackets: 12,
//       icmpTotalPackets: 12,
//       icmp6TotalPackets: 12,
//       otherTotalPackets: 12,

//       // 以太网类型统计
//       ipv4Frames: 12,
//       ipv6Frames: 12,
//       arpFrames: 12,
//       ieee8021xFrames: 12,
//       ipxFrames: 12,
//       lacpFrames: 12,
//       mplsFrames: 12,
//       stpFrames: 12,
//       otherFrames: 12,

//       // 数据包类型统计
//       unicastBytes: 12,
//       broadcastBytes: 12,
//       multicastBytes: 12,

//       l3DevicesTop: {
//         totalBytes: [
//           {
//             ip: '1.1.1.1',
//             value: 11,
//           },
//           {
//             ip: '1.1.1.2',
//             value: 12,
//           },
//           {
//             ip: '1.1.1.3',
//             value: 13,
//           },
//           {
//             ip: '1.1.1.4',
//             value: 14,
//           },
//           {
//             ip: '1.1.1.5',
//             value: 13,
//           },
//           {
//             ip: '1.1.1.6',
//             value: 14,
//           },
//           {
//             ip: '1.1.1.7',
//             value: 13,
//           },
//           {
//             ip: '1.1.1.8',
//             value: 14,
//           },
//         ],
//         totalSessions: [
//           {
//             ip: '2.1.1.1',
//             value: 21,
//           },
//           {
//             ip: '2.1.1.2',
//             value: 22,
//           },
//           {
//             ip: '2.1.1.3',
//             value: 23,
//           },
//           {
//             ip: '2.1.1.4',
//             value: 24,
//           },
//         ],
//       },
//       ipConversationTop: {
//         totalBytes: [
//           {
//             ipA: '1.1.1.1',
//             ipB: '1.1.1.2',
//             value: 12,
//           },
//         ],
//         totalSessions: [
//           {
//             ipA: '1.1.1.1',
//             ipB: '1.1.1.2',
//             value: 12,
//           },
//         ],
//       },

//       dscp: dscp,
//       histogram: histogram
//     },
//   );
// };

// const getNetworkPerformance = (req: Request, res: Response) => {
//   res.json([
//     {
//       metric: 'server_response_normal',
//       value: 2323
//     },
//     {
//       metric: 'server_response_timeout',
//       value: 2323
//     }
//   ])
// }

// const postNetworkPerformance = (req: Request, res: Response) => {
//   res.json({
//     success: true
//   })
// }

// export default {
//   'GET /api/webapi/fpc-v1/metric/networks/:networkId/dashboard': getNpmdDashboard,

//   // sourceType=network&networkId = xxxx
//   'GET /api/webapi/fpc-v1/appliance/metric-settings': getNetworkPerformance,
//   'POST /api/webapi/fpc-v1/appliance/metric-settings': postNetworkPerformance
// };
