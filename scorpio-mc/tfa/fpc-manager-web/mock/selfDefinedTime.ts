// import { Request, Response } from 'express';
// // import moment from 'moment';
// // import { isArray } from 'lodash';
// import { v4 as uuidv4 } from 'uuid';

// export enum EWeek {
//   'Monday' = '1',
//   'Tuesday' = '2',
//   'Wednesday' = '3',
//   'Thursday' = '4',
//   'Friday' = '5',
//   'Saturday' = '6',
//   'Sunday' = '7',
// }

// export enum ECustomTimeType {
//   '周期性时间' = '0',
//   '一次性时间' = '1',
// }

// export const timeTypeOptions = [
//   { label: '周期性时间', value: ECustomTimeType.周期性时间 },
//   { label: '一次性时间', value: ECustomTimeType.一次性时间 },
// ];

// // 表格中的数据项
// export interface TimeConfigItem {
//   id: string;
//   name: string;
//   type: ECustomTimeType;
//   startTime: string;
//   endTime: string;
//   period: EWeek[];
// }

// let data: TimeConfigItem[] = [
//   {
//     id: 'dasdasaw22esdfd_s2lals',
//     name: 'aaa',
//     startTime: '2022-06-01 06:06:06',
//     endTime: '2022-06-01 07:07:07',
//     type: ECustomTimeType.周期性时间,
//     period: [EWeek.Monday, EWeek.Thursday, EWeek.Friday],
//   },
//   {
//     id: 'dasdasaw22esdfd_s222lals',
//     name: 'ccc',
//     startTime: '2022-06-03 13:13:13',
//     endTime: '2022-06-03 19:06:06',
//     type: ECustomTimeType.一次性时间,
//     period: [],
//   },
// ];

// const createSelfDefinedTime = (req: Request, res: Response) => {
//   const { name, startTime, endTime, type, period } = req.body;
//   const id = uuidv4();
//   data.push({
//     id,
//     name,
//     startTime,
//     endTime,
//     type,
//     period,
//   });
//   setTimeout(() => {
//     const index = data.findIndex((item) => item.id === id);
//     data[index] = {
//       ...data[index],
//     };
//   }, 10000);
//   res.json({});
// };

// const querySelfDefinedTime = (req: Request, res: Response) => {
//   res.json([{customTimeSetting: "[{\"start_time_2\":\"00:00:00\",\"end_time_2\":\"00:03:00\"}]",
//   id: "vNYwaoEBW78FP2JODHcm",
//   name: "test",
//   period: "[\"1\",\"2\"]",
//   type: "0"}]);
// };

// const deleteSelfDefinedTime = (req: Request, res: Response) => {
//   console.log(req.body, req.params);
//   const { id } = req.params;
//   if (req.body._method === 'DELETE') {
//     // 删除数据
//     data = data.filter((item) => item.id !== id);
//     res.json({});
//   }
// };

// const getOneSelfDefinedTime = (req: Request, res: Response) => {
//   const { id } = req.params;
//   const result = data.find((item) => item.id === id);
//   res.json(result);
// };

// const updateOneSelfDefinedTime = (req: Request, res: Response) => {
//   // const {id, ...restParams} = req.params;
//   // console.log(restParams, "restP")
//   if (req.body._method === 'PUT') {
//     // data = data.find((item) => (id === item.id ? ({...restParams}): item));
//     // setTimeout(() => {
//     //   const index = data.findIndex((item) => item.id === id);
//     //   data[index] = {
//     //     ...restParams,
//     //   };
//     // }, 10000);
//     setTimeout(() => {}, 1000);
//   }
//   res.json({});
// };

// export default {
//   //新建一个自定义的时间
//   'POST /api/webapi/fpc-v1/appliance/custom-times': createSelfDefinedTime,
//   //查询所有自定义时间
//   'GET  /api/webapi/fpc-v1/appliance/custom-times': querySelfDefinedTime,
//   //删除一个自定义时间
//   'DELETE /api/webapi/fpc-v1/appliance/custom-times/:id': deleteSelfDefinedTime,
//   //查询一个自定义时间
//   'GET /api/webapi/fpc-v1/appliance/custom-times/:id': getOneSelfDefinedTime,

//   'POST /api/webapi/fpc-v1/appliance/custom-times/:id': updateOneSelfDefinedTime,
// };
