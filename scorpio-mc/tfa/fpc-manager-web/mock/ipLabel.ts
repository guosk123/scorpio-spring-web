// import type { Request, Response } from 'express';

// interface IIpLabel {
//   name: string;
//   ipAddress: string;
//   description: string;
//   category: '1' | '2' | '3' | '4';
// }

// const ipLabels: IIpLabel[] = [];

// function createIpLabel(req: Request, res: Response) {
//   ipLabels.push({
//     ...req.body,
//   });
//   res.end();
// }

// function updateIpLabel(req: Request, res: Response) {
//   const idx = ipLabels.findIndex((item) => req.path.indexOf(item.name) > -1);
//   if (idx !== -1) {
//     ipLabels[idx] = { ...req.body };
//   }
//   res.end();
// }

// function getLabelDetail(req: Request, res: Response) {
//   console.log(req.path);
//   const idx = ipLabels.findIndex((item) => req.path.indexOf(item.name) > -1);
//   if (idx !== -1) {
//     res.json(ipLabels[idx]);
//   }
//   res.end();
// }

// function deleteIpLabel(req: Request, res: Response) {
//   const idx = ipLabels.findIndex((item) => req.path.indexOf(item.name) > -1);
//   if (idx !== -1) {
//     ipLabels.splice(idx, 1);
//   }
//   res.end();
// }

// function queryLabelList(req: Request, res: Response) {
//   let result = [...ipLabels];
//   if (req.query.name) {
//     result = result.filter((item) => item.name === req.query.name);
//   }
//   if (req.query.category) {
//     result = result.filter((item) => item.category === req.query.category);
//   }

//   return res.json(result);
// }

// function queryLabelStat(req: Request, res: Response) {
//   const result = ipLabels.reduce(
//     (prev, current) => {
//       return {
//         ...prev,
//         [current.category]: prev[current.category] + 1,
//       };
//     },
//     {
//       '1': 0,
//       '2': 0,
//       '3': 0,
//       '4': 0,
//     },
//   );

//   res.json(result);
// }

// export default {
//   'POST /api/webapi/fpc-v1/appliance/ip-label': createIpLabel,
//   'GET /api/webapi/fpc-v1/appliance/ip-label/statistics': queryLabelStat,
//   'PUT /api/webapi/fpc-v1/appliance/ip-label/:labelName': updateIpLabel,
//   'GET /api/webapi/fpc-v1/appliance/ip-label/:labelName': getLabelDetail,
//   'DELETE /api/webapi/fpc-v1/appliance/ip-label/:labelName': deleteIpLabel,
//   'GET /api/webapi/fpc-v1/appliance/ip-label': queryLabelList,
// };
