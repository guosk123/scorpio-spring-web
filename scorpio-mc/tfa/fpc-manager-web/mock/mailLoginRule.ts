import type { Request, Response } from 'express';
import { Random } from 'mockjs';

export interface IMailLoginRule {
  id: string;
  mailAddress: string;
  countryId: string;
  provinceId: string;
  cityId?: string;
  startTime: string;
  endTime: string;
  action: '0' | '1';
  period?: string;
  state: '0' | '1';
}

const resCount = 15;

const mockData: IMailLoginRule[] = [];

const getRules = (req: Request, res: Response) => {
  for (let i = 0; i < resCount; i++) {
    mockData.push({
      id: Random.id(),
      mailAddress: Random.email(),
      countryId: '1',
      provinceId: '1',
      startTime: '00:00:00',
      endTime: '06:00:00',
      action: '0',
      state: '0',
      period: '1,2,3',
    });
  }

  res.json({
    content: mockData,
    totalElements: resCount,
  });
};

const getRuleDetail = (req: Request, res: Response) => {
  res.json(mockData[0]);
};

export default {
  'GET /api/webapi/fpc-v1/appliance/mail-rules': getRules,
  'GET /api/webapi/fpc-v1/appliance/mail-rule/:id': getRuleDetail,
};
