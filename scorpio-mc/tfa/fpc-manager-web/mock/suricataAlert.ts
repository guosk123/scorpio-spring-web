// import type { Request, Response } from 'express';
// import moment from 'moment';

// interface AlertEvent {
//   srcRole: 'offender' | 'victim';
//   destRole: 'offender' | 'victim';
//   sid: number;
//   timestamp: string;
//   msg: string;
// }

// const sids = [197001011, 197001012, 197001013, 197001014];

// const resCount = 15;

// const getAlertEvents = (req: Request, res: Response) => {
//   const { startTime } = req.query as { startTime: string };

//   const currentDate = moment(startTime).add(1, 'day').valueOf();

//   const mockData: AlertEvent[] = [];

//   for (let i = 0; i < resCount; i++) {
//     const offset = (Math.floor(resCount / 2) - i) * 60 * 1000;
//     if (offset === 0) continue;
//     const time = new Date(currentDate + offset).toISOString();
//     mockData.push({
//       srcRole: ['offender', 'victim', null][(i + 1) % 3] as AlertEvent['srcRole'],
//       destRole: ['offender', 'victim', null][i % 3] as AlertEvent['destRole'],
//       sid: sids[i % sids.length],
//       timestamp: time,
//       msg: 'test msg',
//     });
//   }

//   res.json(mockData);
// };

// export default {
//   'GET /api/webapi/fpc-v1/suricata/alert-messages/relation': getAlertEvents,
// };
