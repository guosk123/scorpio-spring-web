// import { Request, Response } from 'express';

// const baseLineSettingData = [
//   {
//     sourceType: 'network',
//     sourceId: 'mWjkRXkBlGvi2k_JwDgv',
//     category: 'bandwidth',
//     weightingModel: 'MIN',
//     windowingModel: '1m_of_day,5m_of_day,hour_of_day',
//     windowingCount: '5',
//   },
//   {
//     sourceType: 'network',
//     sourceId: 'mWjkRXkBlGvi2k_JwDgv',
//     category: 'flow',
//     weightingModel: 'MIN',
//     windowingModel: '1m_of_day,5m_of_day,hour_of_day',
//     windowingCount: '5',
//   },
//   {
//     sourceType: 'network',
//     sourceId: 'mWjkRXkBlGvi2k_JwDgv',
//     category: 'packet',
//     weightingModel: 'MIN',
//     windowingModel: '1m_of_day,5m_of_day,hour_of_day',
//     windowingCount: '5',
//   },
//   {
//     sourceType: 'network',
//     sourceId: 'mWjkRXkBlGvi2k_JwDgv',
//     category: 'responseLatency',
//     weightingModel: 'MIN',
//     windowingModel: '1m_of_day,5m_of_day,hour_of_day',
//     windowingCount: '5',
//   },
// ];

// const getBaselineSettings = (req: Request, res: Response) => {
//   res.json(baseLineSettingData);
// };

// const putBaselineSettings = (req: Request, res: Response) => {
//   res.setHeader('Access-Control-Allow-Origin', '*');
//   res.json({
//     success: true,
//   });
//   // res.end('ok');
// };

// export default {
//   'GET /api/webapi/fpc-v1/appliance/baseline-settings': getBaselineSettings,
//   'POST /api/webapi/fpc-v1/appliance/baseline-settings': putBaselineSettings,
// };
