import { Response, Request } from 'umi';

const data = [
  { key: '100032213', count: 142998157 },
  { key: '100008', count: 67594310 },
  { key: '1001', count: 189441 },
  { key: '1002', count: 189441 },
  { key: '197001011', count: 45401 },
  { key: '12345', count: 40004 },
  { key: '510004953', count: 11696 },
  { key: '510004882', count: 10861 },
  { key: '510003085', count: 10378 },
  { key: '510008145', count: 4788 },
];

const queryData = (req: Request, res: Response) => {
  return res.json(data);
};

export default {
  'GET /api/webapi/fpc-cms-v1/suricata/alert-messages/statistics': queryData,
};
