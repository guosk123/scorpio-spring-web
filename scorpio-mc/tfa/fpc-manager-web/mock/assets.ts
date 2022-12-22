import { Request, Response } from 'express';
const data = {
  content: [
    {
      ipAddress: '10.0.0.1',
      deviceType: '',
      port: '18',
      firstTime: '2022-10-26T02:58Z',
      timestamp: '2022-09-23T05:46:03Z',
      alarm: 0,
    },
    {
      ipAddress: '10.0.0.2',
      deviceType: '',
      port: '18',
      firstTime: '2022-10-26T02:58Z',
      timestamp: '2022-09-23T09:46:03Z',
      alarm: 3,
    },
  ],
};

const alarmData = {
  content: [
    {
      alarmTime: '2022-10-27T05:45:00.107+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T05:42:00.110+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T05:39:00.129+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T05:36:00.109+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T05:33:00.125+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T05:30:00.113+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T05:27:00.140+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T05:24:00.135+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T05:21:00.092+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T05:18:00.154+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T05:15:00.111+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T05:12:00.102+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T05:09:00.124+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T05:06:00.100+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T05:03:00.115+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T05:00:00.130+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T04:57:00.097+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T04:54:00.124+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T04:51:00.138+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
    {
      alarmTime: '2022-10-27T04:48:00.111+0000',
      ipAddress: '10.0.0.56',
      baseline: '0',
      type: '1',
      current: '1',
    },
  ],
};

const baselineData = {
  content: [
    {
      ipAddress: '10.0.0.1',
      type: ['1', '2'],
      baseline: [
        {
          deviceType: '1,2,3',
        },
        {
          port: '8000,443',
        },
      ],
      description: '这是一个基线!',
    },
  ],
};

const getData = (req: Request, res: Response) => {
  console.log(req.query);
  res.json(data);
};

const getBaselineData = (req: Request, res: Response) => {
  res.json(baselineData);
};

const getAlarmData = (req: Request, res: Response) => {
  res.json(alarmData);
};

const getExpireDays = (req: Request, res: Response) => {
  res.json({ usefulLife: 7 });
};
export default {
  'GET /api/webapi/fpc-v1/metric/asset-information': getData,
  'GET /api/webapi/fpc-v1/metric/asset-alarm': getAlarmData,
  'GET /api/webapi/fpc-v1/metric/asset-baseline': getBaselineData,
  'GET /api/webapi/fpc-v1/metric/asset-information/useful-life': getExpireDays,
};
