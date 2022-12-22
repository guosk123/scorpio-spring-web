import { Request, Response } from 'express';

const createSegment = (req: Request, res: Response) => {
  res.json({
    m1: {
      rtpLossPacketsRate: '4.0%',
      rtpLossPackets: 4,
      jitterMax: 240285,
      jitterMean: 24185,
      rtpTotalPackets: 10,
    },
    m2: {
      rtpLossPacketsRate: '10.0%',
      rtpLossPackets: 98,
      jitterMax: 22285,
      jitterMean: 2433185,
      rtpTotalPackets: 2,
    },
    // Dgu3v4MBw0EtPNmW5iv_: {
    //   rtpLossPacketsRate: '0.0%',
    //   rtpLossPackets: 0,
    //   jitterMax: 240241185,
    //   jitterMean: 240241185,
    //   rtpTotalPackets: 6,
    // },
    // m3: {
    //   rtpLossPacketsRate: '16.0%',
    //   rtpLossPackets: 998,
    //   jitterMax: 22285,
    //   jitterMean: 2433185,
    //   rtpTotalPackets: 9,
    // },
    // m4: {
    //   rtpLossPacketsRate: '10.0%',
    //   rtpLossPackets: 90,
    //   jitterMax: 2275,
    //   jitterMean: 243305,
    //   rtpTotalPackets: 9,
    // },
  });
};

const createHist = (req: Request, res: Response) => {
  res.json([
    {
      rtpLossPacketsRate: 0.01,
      rtpLossPackets: 0,
      jitterMax: 240241185,
      jitterMean: 240241185,
      rtpTotalPackets: 6,
      reportTime: '2022-10-17 17:00:00',
    },
    {
      rtpLossPacketsRate: 0.11,
      rtpLossPackets: 1,
      jitterMax: 24022185,
      jitterMean: 240243185,
      rtpTotalPackets: 4,
      reportTime: '2022-10-17 18:00:00',
    },
    {
      rtpLossPacketsRate: 0.4,
      rtpLossPackets: 0,
      jitterMax: 2402385,
      jitterMean: 24285,
      rtpTotalPackets: 1,
      reportTime: '2022-10-17 19:00:00',
    },
    {
      rtpLossPacketsRate: 0.31,
      rtpLossPackets: 3,
      jitterMax: 231185,
      jitterMean: 2403185,
      rtpTotalPackets: 4,
      reportTime: '2022-10-17 21:00:00',
    },
    {
      rtpLossPacketsRate: 0.24,
      rtpLossPackets: 5,
      jitterMax: 23134,
      jitterMean: 244185,
      rtpTotalPackets: 5,
      reportTime: '2022-10-17 22:00:00',
    },
    {
      rtpLossPacketsRate: 0.56,
      rtpLossPackets: 5,
      jitterMax: 2313335,
      jitterMean: 24031485,
      rtpTotalPackets: 3,
      reportTime: '2022-10-17 23:00:00',
    },
    {
      rtpLossPacketsRate: 0.10,
      rtpLossPackets: 33,
      jitterMax: 231183335,
      jitterMean: 240313385,
      rtpTotalPackets: 43,
      reportTime: '2022-10-18 00:00:00',
    },
  ]);
};

export default {
  // 'GET /api/webapi/fpc-v1/metadata/network-segmentation': createSegment,
  // 'GET /api/webapi/fpc-v1/metadata/network-segmentation/as-histogram': createHist,
};
