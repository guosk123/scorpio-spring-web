// import { Request, Response } from 'express';

// const getAbnormalEventMessage = (req: Request, res: Response) => {
//   if(req.query.metricType==='type'){
//       res.json(
//             [
//                 {
//                     "type" : 2,
//                     "count" : 1253
//                 },
//                 {
//                   "type" : 203,
//                   "count" : 2000
//                 },
//                 {
//                   "type" : 204,
//                   "count" : 1000
//                 },
//                 {
//                   "type" : 41,
//                   "count" : 400
//                 }
//             ]
//         );
//   }else if(req.query.metricType==='locationInitiator'){
//         res.json(
//         [
//           {
//               "countryIdInitiator": 1,
//               "provinceIdInitiator": 2,
//               "cityIdInitiator": 3,
//               "count": 1000
//           },
//           {
//             "countryIdInitiator": 2,
//             "provinceIdInitiator": 3,
//             "cityIdInitiator": 1,
//             "count": 500
//           },
//           {
//             "countryIdInitiator": 4,
//             "provinceIdInitiator": 3,
//             "cityIdInitiator": 2,
//             "count": 300
//           }
//       ]
//     );
//   }else if(req.query.metricType==='locationResponder'){
    
//         res.json(
//               [
//                 {
//                     "countryIdResponder": 1,
//                     "provinceIdResponder": 2,
//                     "cityIdResponder": 3,
//                     "count": 300
//                 },
//                 {
//                   "countryIdResponder": 3,
//                   "provinceIdResponder": 2,
//                   "cityIdResponder": 5,
//                   "count": 300
//               },
//               {
//                 "countryIdResponder": 5,
//                 "provinceIdResponder": 2,
//                 "cityIdResponder": 3,
//                 "count": 300
//               },
//             ]
//         );
//   }

// };
// export default {
//    'GET /api/webapi/fpc-v1/analysis/abnormal-events/as-count': getAbnormalEventMessage,
// }