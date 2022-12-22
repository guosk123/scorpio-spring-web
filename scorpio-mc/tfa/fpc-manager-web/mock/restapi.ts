// import type { Request, Response } from 'express';
// import { Random } from 'mockjs';
// import moment from 'moment';

// export interface IRestAPIRecord {
//   apiName: string;
//   uri: string;
//   userIp: string;
//   userId: string;
//   timestamp: string;
//   status: number;
//   method: string;
//   response: string;
// }

// export enum ERestStatType {
//   'User' = 'user',
//   'API' = 'api',
// }

// export interface IRestMetric {
//   timestamp: string;
//   [key: string]: number | string;
// }

// const HTTP_METHOD = ['GET', 'POST', 'PUT', 'DELETE'];

// const restRecord: IRestAPIRecord[] = [];

// for (let i = 0; i < 10; i++) {
//   restRecord.push({
//     apiName: Random.name(),
//     uri: Random.url(),
//     userIp: Random.ip(),
//     userId: Random.id(),
//     method: HTTP_METHOD[Random.integer(0, HTTP_METHOD.length - 1)],
//     timestamp: Random.time(),
//     status: Random.integer(0, 1),
//     response: Random.sentence(),
//   });
// }

// const generateRestStat = (type: ERestStatType, limit = 10) => {
//   const res: IRestMetric[] = [];
//   const now = moment().valueOf();
//   const fromNow30M = now - 30 * 60 * 1000;

//   const names: string[] = [];
//   for (let i = 0; i < (limit || 10); i++) {
//     const name = `${Random.name()}-${type === ERestStatType.User ? 'U' : 'A'}`;
//     names.push(name);
//   }

//   for (let j = 0; j < 30; j++) {
//     res.push({
//       ...names.reduce((total, curr) => {
//         return {
//           ...total,
//           [curr]: Random.integer(10, 200),
//         };
//       }, {}),
//       timestamp: moment(fromNow30M + 1000 * j * 60).format(),
//     });
//   }

//   return res;
// };

// const getRestApiRecord = (req: Request, res: Response) => {
//   return res.json(restRecord);
// };

// const queryRestStat = (req: Request, res: Response) => {
//   const { type } = req.query;
//   console.log(type);

//   return res.json(generateRestStat(type as ERestStatType, 10));
// };

// const queryRestApiStatList = (req: Request, res: Response) => {
//   const { type } = req.query;
//   return res.json(generateRestStat(type as ERestStatType, 20));
// };

// export default {
//   'GET /api/webapi/fpc-v1/system/restapi-records': getRestApiRecord,
//   'GET /api/webapi/fpc-v1/system/rest-api/stat': queryRestStat,
//   'GET /api/webapi/fpc-v1/system/rest-api/stat/as-list': queryRestApiStatList,
// };
