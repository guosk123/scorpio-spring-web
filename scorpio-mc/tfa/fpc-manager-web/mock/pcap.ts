// const mockpcap = () => {
//   const data = {
//     bytepsPeak: 123,
//     totalBytes: 123,
//     packetpsPeak: 123,
//     totalPackets: 123,
//     establishedSessions: 123,
//     concurrentSessions: 123,
//     tcpClientNetworkLatencyAvg: 123,
//     tcpServerNetworkLatencyAvg: 123,
//     serverResponseLatencyAvg: 123,
//     tcpClientRetransmissionPackets: 123,
//     tcpClientRetransmissionRate: 123,
//     tcpServerRetransmissionPackets: 123,
//     tcpServerRetransmissionRate: 123,
//     tcpClientZeroWindowPackets: 123,
//     tcpServerZeroWindowPackets: 123,
//     tcpEstablishedSuccessCounts: 123,
//     tcpEstablishedFailCounts: 123,
//   };
//   const one = {
//     content: [
//     ],
//     number: 1,
//     size: 15,
//     sort: [
//       {
//         direction: 'DESC',
//         property: 'create_time',
//       },
//     ],
//     totalElements: 100,
//     totalPages: 7,
//   };
//   let n = {
//     id: '1',
//     name: '1',
//     packetStartTime: 1622361000000,
//     packetEndTime: 1622361000000,
//     size: '1',
//     filePath: '1',
//     status: '1',
//     statusText: '1',
//     executionProgress: 1,
//     executionResult: JSON.stringify(data),
//     createTime: '1',
//     operatorId: '1',
//   }
  
//   for (let index = 0; index < 10; index++) {
//     n.id = index+''
//     one.content.push(
//       {...n}
//     )
//   }
//   return one
// };

// export default {
//   '/api/webapi/fpc-v1/appliance/offline-analysis-tasks': mockpcap(),
// };
